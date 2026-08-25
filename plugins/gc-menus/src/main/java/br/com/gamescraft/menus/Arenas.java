package br.com.gamescraft.menus;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * As arenas do MegaGames: cada uma vive num mundo próprio, que só existe
 * enquanto está em uso.
 *
 * O ciclo é o do Bed Wars: o mapa oficial fica guardado como zip, o mundo é
 * criado a partir dele quando a partida vai começar, e apagado quando ela
 * acaba. Nada de mapa velho ocupando memória e disco entre uma partida e outra
 * — e nada de sobra da partida anterior aparecendo na seguinte, porque não há
 * o que sobrar: o mundo é outro.
 *
 * Só uma arena fica aberta por vez durante a partida. As outras são fechadas,
 * porque manter três mapas carregados para jogar em um é pagar memória e tique
 * por mundo que ninguém está vendo.
 *
 * Fora de partida, ficam abertas as que tiverem gente dentro — é o caso de quem
 * está construindo com /setup.
 */
final class Arenas {

    /** Onde ficam os zips com o mapa oficial de cada arena. */
    private static final String MODELOS = "modelos";

    /** Uma arena: o mundo dela e o jogo que acontece ali. */
    record Arena(String mundo, String nome, String jogo) {
    }

    private final JavaPlugin plugin;
    private final Map<String, Arena> arenas = new LinkedHashMap<>();
    private final File pastaDoServidor;

    /** A arena da partida em andamento. Nula fora de partida. */
    private String emJogo;

    Arenas(JavaPlugin plugin) {
        this.plugin = plugin;
        this.pastaDoServidor = plugin.getDataFolder().getParentFile().getParentFile();

        for (String linha : plugin.getConfig().getStringList("arenas")) {
            String[] p = linha.split(",");
            if (p.length < 3) {
                continue;
            }
            Arena arena = new Arena(p[0].trim(), p[1].trim(), p[2].trim());
            arenas.put(arena.mundo(), arena);
        }
        plugin.getLogger().info("Arenas configuradas: " + arenas.size());

        // De dez em dez segundos, fecha arena vazia. Quem saiu de uma obra e
        // esqueceu o mundo aberto nao deveria deixar um mapa carregado a noite
        // inteira.
        plugin.getServer().getScheduler().runTaskTimer(plugin, this::fecharVazias, 200L, 200L);
    }

    List<Arena> todas() {
        return new ArrayList<>(arenas.values());
    }

    Arena de(String mundo) {
        return arenas.get(mundo);
    }

    void marcarEmJogo(String mundo) {
        this.emJogo = mundo;
    }

    // ------------------------------------------------------------- caminhos

    /**
     * Onde o Paper guarda os blocos de uma arena.
     *
     * Na 26.2 mundo secundário não tem pasta própria: ele vive como dimensão
     * dentro do mundo principal. O layout de pasta por mundo, que valeu por dez
     * anos, acabou — e foi por não saber disso que o código quase apagou a ilha.
     */
    private File pastaDoMundo(String mundo) {
        File antigo = new File(pastaDoServidor, mundo);
        if (antigo.isDirectory()) {
            return antigo;
        }
        World principal = Bukkit.getWorlds().isEmpty() ? null : Bukkit.getWorlds().get(0);
        String nomeDoPrincipal = principal == null ? "world" : principal.getName();
        return new File(pastaDoServidor, nomeDoPrincipal + "/dimensions/minecraft/" + mundo);
    }

    private File zipDoModelo(String mundo) {
        return new File(new File(plugin.getDataFolder(), MODELOS), mundo + ".zip");
    }

    boolean temModelo(String mundo) {
        return zipDoModelo(mundo).isFile();
    }

    // ------------------------------------------------------- abrir e fechar

    /**
     * Põe a arena no ar, sempre a partir do zip.
     *
     * O mundo que estiver no disco é jogado fora antes: ele é o resto da
     * partida anterior, ou de uma obra que não foi salva. Partida que começa
     * com sobra da anterior é partida injusta.
     */
    World abrir(String mundo, CommandSender aviso) {
        Arena arena = arenas.get(mundo);
        if (arena == null) {
            return null;
        }
        World jaAberta = Bukkit.getWorld(mundo);
        if (jaAberta != null) {
            return jaAberta;
        }
        long comeco = System.currentTimeMillis();
        File zip = zipDoModelo(mundo);
        if (zip.isFile()) {
            try {
                apagar(pastaDoMundo(mundo).toPath());
                descompactar(zip, pastaDoMundo(mundo));
            } catch (IOException erro) {
                plugin.getLogger().warning("Nao consegui abrir " + mundo + " do zip: " + erro);
                if (aviso != null) {
                    aviso.sendMessage(ChatColor.RED + "Falhou ao abrir " + mundo + ".");
                }
                return null;
            }
        } else if (aviso != null) {
            aviso.sendMessage(ChatColor.YELLOW + "A arena " + mundo + " ainda nao tem mapa salvo. "
                    + "Vou abrir uma vazia para voce construir — use /save quando terminar.");
        }

        WorldCreator criador = new WorldCreator(mundo);
        if (!zip.isFile() && !pastaDoMundo(mundo).isDirectory()) {
            criador.type(WorldType.NORMAL);
        }
        World world = criador.createWorld();
        if (world == null) {
            plugin.getLogger().warning("Arena " + mundo + " nao carregou.");
            return null;
        }
        world.setTime(6000);
        world.setStorm(false);
        world.setDifficulty(Difficulty.NORMAL);
        world.setAutoSave(false);
        plugin.getLogger().info("Arena " + mundo + " aberta em "
                + (System.currentTimeMillis() - comeco) + " ms.");
        return world;
    }

    /**
     * Tira a arena do ar. Com apagar, some também do disco.
     *
     * Apagar é o certo no fim da partida: o mundo daquela partida não serve para
     * mais nada, e o próximo nasce do zip. Sem apagar é o certo em obra, porque
     * o que a pessoa construiu e ainda não salvou está ali.
     */
    void fechar(String mundo, boolean apagar, Location paraOnde) {
        World world = Bukkit.getWorld(mundo);
        if (world != null) {
            for (Player jogador : new ArrayList<>(world.getPlayers())) {
                if (paraOnde != null) {
                    jogador.teleport(paraOnde);
                } else {
                    jogador.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
                }
            }
            if (!Bukkit.unloadWorld(world, !apagar)) {
                plugin.getLogger().warning("Nao consegui fechar " + mundo + ".");
                return;
            }
        }
        // Só apaga o que tem zip para voltar. Sem esta trava, uma arena que nunca
        // foi salva seria apagada no fim da primeira partida e não teria de onde
        // renascer — o mapa iria embora de vez. É o mesmo erro que já custou uma
        // área de castelo aqui: apagar confiando numa cópia que não existia.
        if (apagar && !temModelo(mundo)) {
            plugin.getLogger().warning("Arena " + mundo + " nao tem zip salvo: "
                    + "vou deixar a pasta onde esta em vez de apagar sem volta.");
            return;
        }
        if (apagar) {
            try {
                apagar(pastaDoMundo(mundo).toPath());
            } catch (IOException erro) {
                plugin.getLogger().warning("Nao consegui apagar a pasta de " + mundo + ": " + erro);
            }
        }
    }

    /** Fecha e apaga todas menos a que vai ser jogada. */
    void fecharOutras(String manter, Location paraOnde) {
        for (Arena arena : arenas.values()) {
            if (arena.mundo().equals(manter) || Bukkit.getWorld(arena.mundo()) == null) {
                continue;
            }
            fechar(arena.mundo(), true, paraOnde);
        }
    }

    /**
     * Fecha arena vazia. A da partida em andamento nunca, mesmo vazia — ela
     * esvazia por um instante toda vez que alguém morre e renasce.
     */
    private void fecharVazias() {
        for (Arena arena : arenas.values()) {
            if (arena.mundo().equals(emJogo)) {
                continue;
            }
            World world = Bukkit.getWorld(arena.mundo());
            if (world != null && world.getPlayers().isEmpty()) {
                // Sem apagar: pode ser obra em andamento e ainda nao salva.
                fechar(arena.mundo(), false, null);
                plugin.getLogger().info("Arena " + arena.mundo() + " fechada por estar vazia.");
            }
        }
    }

    // ------------------------------------------------------------- comandos

    /** O /setup: abre a arena e leva você lá, em criativo. */
    boolean setup(CommandSender quemPediu, String[] argumentos) {
        if (!(quemPediu instanceof Player jogador)) {
            quemPediu.sendMessage("Esse comando precisa ser dado em jogo.");
            return true;
        }
        World principal = Bukkit.getWorlds().get(0);
        if (argumentos.length != 1) {
            jogador.sendMessage(ChatColor.RED + "Use: /setup <arena>");
            jogador.sendMessage(ChatColor.GRAY + "  saguao — a sala de votacao");
            for (Arena arena : arenas.values()) {
                jogador.sendMessage(ChatColor.GRAY + "  " + arena.mundo() + " — " + arena.nome()
                        + (temModelo(arena.mundo()) ? "" : ChatColor.DARK_GRAY + " (sem mapa salvo)"));
            }
            return true;
        }
        if (argumentos[0].equals(principal.getName()) || argumentos[0].equalsIgnoreCase("saguao")) {
            jogador.teleport(principal.getSpawnLocation());
            jogador.setGameMode(GameMode.CREATIVE);
            jogador.sendMessage(ChatColor.GREEN + "Voce esta no saguao, no nascimento do mundo.");
            return true;
        }
        Arena arena = arenas.get(argumentos[0]);
        if (arena == null) {
            jogador.sendMessage(ChatColor.RED + "Nao conheco a arena " + argumentos[0] + ".");
            return true;
        }
        World mundo = abrir(arena.mundo(), jogador);
        if (mundo == null) {
            jogador.sendMessage(ChatColor.RED + "Nao consegui abrir " + arena.mundo() + ".");
            return true;
        }
        jogador.teleport(mundo.getSpawnLocation());
        jogador.setGameMode(GameMode.CREATIVE);
        jogador.addScoreboardTag("gc_obras");
        jogador.sendMessage(ChatColor.GREEN + "Voce esta em " + arena.nome() + ".");
        jogador.sendMessage(ChatColor.GRAY + "Construa e use /save aqui dentro. "
                + "O que nao for salvo se perde quando a arena fechar.");
        return true;
    }

    /** O /save: a arena vira zip, e é desse zip que toda partida vai nascer. */
    /**
     * O /abrir: põe a arena no ar como ela está no disco, sem tocar no zip.
     *
     * O /setup também abre, mas antes apaga a pasta e descompacta o modelo — é o
     * certo para começar a construir de um estado conhecido, e é errado quando o
     * que está no disco vale mais que o zip. Foi o caso do conserto da ilha: o
     * mapa bom estava na pasta e o zip carregava o arquivo de região quebrado.
     *
     * E não pede jogador. O /setup teleporta, então precisa de gente; este só
     * carrega o mundo, e serve do console.
     */
    boolean abrirOndeEstou(CommandSender quemPediu, String[] argumentos) {
        if (argumentos.length != 1) {
            quemPediu.sendMessage("Use: /abrir <arena>");
            return true;
        }
        String mundo = argumentos[0];
        if (!arenas.containsKey(mundo)) {
            quemPediu.sendMessage(ChatColor.RED + "Nao e uma arena: " + mundo + ".");
            return true;
        }
        if (Bukkit.getWorld(mundo) != null) {
            quemPediu.sendMessage(ChatColor.YELLOW + "A arena " + mundo + " ja esta aberta.");
            return true;
        }
        World world = new WorldCreator(mundo).createWorld();
        if (world == null) {
            quemPediu.sendMessage(ChatColor.RED + "A arena " + mundo + " nao carregou.");
            return true;
        }
        world.setTime(6000);
        world.setStorm(false);
        world.setDifficulty(Difficulty.NORMAL);
        // Segura a arena no ar. O vigia fecha toda arena vazia em dez segundos,
        // e quem abre pelo console abre justamente para mexer sem ninguém dentro
        // — a arena sumia debaixo do trabalho antes de dar tempo de começar.
        marcarEmJogo(mundo);
        quemPediu.sendMessage(ChatColor.GREEN + "Arena " + mundo + " aberta como esta no disco. "
                + ChatColor.GRAY + "O zip nao foi tocado.");
        quemPediu.sendMessage(ChatColor.GRAY + "Ela fica no ar ate /fechar " + mundo
                + " ou ate o servidor reiniciar.");
        return true;
    }

    /** O /fechar: tira a arena do ar guardando o que está nela, e solta o vigia. */
    boolean fecharOndeEstou(CommandSender quemPediu, String[] argumentos) {
        if (argumentos.length != 1) {
            quemPediu.sendMessage("Use: /fechar <arena>");
            return true;
        }
        String mundo = argumentos[0];
        if (!arenas.containsKey(mundo)) {
            quemPediu.sendMessage(ChatColor.RED + "Nao e uma arena: " + mundo + ".");
            return true;
        }
        if (mundo.equals(emJogo)) {
            marcarEmJogo(null);
        }
        fechar(mundo, false, null);
        quemPediu.sendMessage(ChatColor.GREEN + "Arena " + mundo + " fechada. "
                + ChatColor.GRAY + "O que estava nela foi gravado.");
        return true;
    }

    boolean salvarOndeEstou(CommandSender quemPediu, String[] argumentos) {
        String mundo;
        if (argumentos.length == 1) {
            mundo = argumentos[0];
        } else if (quemPediu instanceof Player jogador) {
            mundo = jogador.getWorld().getName();
        } else {
            quemPediu.sendMessage("Do console, use: /save <arena>");
            return true;
        }
        if (!arenas.containsKey(mundo)) {
            quemPediu.sendMessage(ChatColor.RED + "Nao e uma arena: " + mundo + ".");
            return true;
        }
        World world = Bukkit.getWorld(mundo);
        if (world == null) {
            quemPediu.sendMessage(ChatColor.RED + "A arena " + mundo + " nao esta aberta.");
            return true;
        }
        long comeco = System.currentTimeMillis();
        world.save();
        File zip = zipDoModelo(mundo);
        zip.getParentFile().mkdirs();
        try {
            compactar(pastaDoMundo(mundo), zip);
        } catch (IOException erro) {
            quemPediu.sendMessage(ChatColor.RED + "Falhou ao salvar: " + erro.getMessage());
            plugin.getLogger().warning("Zip de " + mundo + " falhou: " + erro);
            return true;
        }
        quemPediu.sendMessage(ChatColor.GREEN + "Arena " + mundo + " salva em "
                + (System.currentTimeMillis() - comeco) + " ms (" + (zip.length() / 1024) + " KB).");
        quemPediu.sendMessage(ChatColor.GRAY + "E deste estado que toda partida vai nascer.");
        if (quemPediu instanceof Player jogador) {
            jogador.removeScoreboardTag("gc_obras");
        }
        return true;
    }

    /** O /reset à mão: fecha, apaga e abre do zip. */
    boolean resetarOndeEstou(CommandSender quemPediu, String[] argumentos) {
        String mundo;
        if (argumentos.length == 1) {
            mundo = argumentos[0];
        } else if (quemPediu instanceof Player jogador) {
            mundo = jogador.getWorld().getName();
        } else {
            quemPediu.sendMessage("Do console, use: /reset <arena>");
            return true;
        }
        if (!arenas.containsKey(mundo)) {
            quemPediu.sendMessage(ChatColor.RED + "Nao e uma arena: " + mundo + ".");
            return true;
        }
        if (!temModelo(mundo)) {
            quemPediu.sendMessage(ChatColor.RED + "A arena " + mundo + " nunca foi salva. "
                    + "Entre nela e rode /save primeiro.");
            return true;
        }
        Location saguao = Bukkit.getWorlds().get(0).getSpawnLocation();
        long comeco = System.currentTimeMillis();
        fechar(mundo, true, saguao);
        World novo = abrir(mundo, quemPediu);
        quemPediu.sendMessage(novo == null
                ? ChatColor.RED + "Nao deu."
                : ChatColor.GREEN + "Arena " + mundo + " refeita em "
                        + (System.currentTimeMillis() - comeco) + " ms.");
        return true;
    }

    // ---------------------------------------------------------- zip e disco

    private void compactar(File pasta, File zip) throws IOException {
        Path raiz = pasta.toPath();
        try (ZipOutputStream saida = new ZipOutputStream(new FileOutputStream(zip))) {
            Files.walkFileTree(raiz, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path arquivo, BasicFileAttributes atributos)
                        throws IOException {
                    // O session.lock e do mundo rodando, nao do mapa: guardado no
                    // zip, ele faz o servidor achar que outra instancia abriu o
                    // mundo quando for descompactado.
                    if (arquivo.getFileName().toString().equals("session.lock")) {
                        return FileVisitResult.CONTINUE;
                    }
                    saida.putNextEntry(new ZipEntry(raiz.relativize(arquivo).toString()
                            .replace('\\', '/')));
                    try (InputStream entrada = new FileInputStream(arquivo.toFile())) {
                        entrada.transferTo(saida);
                    }
                    saida.closeEntry();
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }

    private void descompactar(File zip, File pasta) throws IOException {
        Path destino = pasta.toPath();
        Files.createDirectories(destino);
        try (ZipInputStream entrada = new ZipInputStream(new FileInputStream(zip))) {
            ZipEntry item;
            while ((item = entrada.getNextEntry()) != null) {
                Path alvo = destino.resolve(item.getName()).normalize();
                // Um zip pode trazer caminho para fora da pasta. Nao vem de
                // fora aqui, mas conferir custa uma linha.
                if (!alvo.startsWith(destino)) {
                    continue;
                }
                if (item.isDirectory()) {
                    Files.createDirectories(alvo);
                } else {
                    Files.createDirectories(alvo.getParent());
                    try (OutputStream saida = new FileOutputStream(alvo.toFile())) {
                        entrada.transferTo(saida);
                    }
                }
                entrada.closeEntry();
            }
        }
    }

    private void apagar(Path alvo) throws IOException {
        if (!Files.exists(alvo)) {
            return;
        }
        Files.walkFileTree(alvo, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path arquivo, BasicFileAttributes atributos)
                    throws IOException {
                Files.deleteIfExists(arquivo);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path pasta, IOException erro)
                    throws IOException {
                Files.deleteIfExists(pasta);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
