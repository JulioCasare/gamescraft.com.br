package br.com.gamescraft.menus;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * As arenas do MegaGames: um mundo para cada, e reset trocando a pasta.
 *
 * O reset por cópia de blocos que existia antes levava mais de um minuto na
 * ilha inteira — são trinta e quatro milhões de blocos, e o /clone anda bloco a
 * bloco. Trocar a pasta do mundo leva um ou dois segundos, porque o disco copia
 * arquivo, não bloco. É o mesmo caminho que o BedWars1058 usa, e foi de olhar o
 * `Cache/duplas1.zip` dele que veio a ideia.
 *
 * O preço é o arranjo: o Bukkit recusa descarregar o mundo principal, e sem
 * descarregar não há como trocar a pasta debaixo dele. Por isso o mundo
 * principal deste servidor é a sala de votação, vazia, e as arenas são mundos
 * carregados por aqui.
 */
final class Arenas {

    /** Onde ficam as cópias intactas de cada arena. */
    private static final String MODELOS = "modelos";

    /** Uma arena: o mundo dela e o jogo que acontece ali. */
    record Arena(String mundo, String nome, String jogo) {
    }

    private final JavaPlugin plugin;
    private final Map<String, Arena> arenas = new LinkedHashMap<>();
    private final File pastaDoServidor;

    Arenas(JavaPlugin plugin) {
        this.plugin = plugin;
        // As pastas dos mundos ficam ao lado do plugins/, na raiz do servidor.
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
    }

    List<Arena> todas() {
        return new ArrayList<>(arenas.values());
    }

    Arena de(String mundo) {
        return arenas.get(mundo);
    }

    private File pastaDoMundo(String mundo) {
        return new File(pastaDoServidor, mundo);
    }

    private File pastaDoModelo(String mundo) {
        return new File(new File(plugin.getDataFolder(), MODELOS), mundo);
    }

    /**
     * Carrega as arenas que ainda não estão no ar.
     *
     * Mundo que não existe é criado na hora, com semente sorteada. É assim que
     * as duas arenas novas nascem sem eu precisar escolher terreno: elas ficam
     * de rascunho até alguém construir o mapa de verdade.
     */
    void carregarTodas() {
        for (Arena arena : arenas.values()) {
            if (Bukkit.getWorld(arena.mundo()) != null) {
                continue;
            }
            boolean nova = !pastaDoMundo(arena.mundo()).isDirectory();
            WorldCreator criador = new WorldCreator(arena.mundo());
            if (nova) {
                criador.type(WorldType.NORMAL);
            }
            World mundo = criador.createWorld();
            if (mundo == null) {
                plugin.getLogger().warning("Nao consegui carregar a arena " + arena.mundo() + ".");
                continue;
            }
            // Nada de ciclo do dia nem de monstro nas arenas: partida de
            // minigame nao deveria virar noite no meio.
            mundo.setTime(6000);
            mundo.setStorm(false);
            mundo.setDifficulty(org.bukkit.Difficulty.NORMAL);
            plugin.getLogger().info("Arena no ar: " + arena.mundo()
                    + (nova ? " (mundo novo, semente sorteada)" : ""));
        }
    }

    /**
     * Guarda a cópia intacta de uma arena. É dela que o reset vai puxar.
     *
     * Salva o mundo antes de copiar: o que está em memória e ainda não foi ao
     * disco não entraria na cópia, e o modelo nasceria diferente do que se vê.
     */
    boolean guardarModelo(String mundo, CommandSender quemPediu) {
        World world = Bukkit.getWorld(mundo);
        if (world == null) {
            quemPediu.sendMessage(ChatColor.RED + "A arena " + mundo + " nao esta carregada.");
            return false;
        }
        world.save();
        File origem = pastaDoMundo(mundo);
        File destino = pastaDoModelo(mundo);
        try {
            apagar(destino.toPath());
            copiar(origem.toPath(), destino.toPath());
        } catch (IOException erro) {
            quemPediu.sendMessage(ChatColor.RED + "Falhou ao guardar o modelo: " + erro.getMessage());
            plugin.getLogger().warning("Modelo de " + mundo + " falhou: " + erro);
            return false;
        }
        quemPediu.sendMessage(ChatColor.GREEN + "Modelo de " + mundo + " guardado.");
        return true;
    }

    /**
     * Devolve a arena ao estado do modelo: descarrega, troca a pasta, carrega.
     *
     * Quem estiver dentro é tirado antes — descarregar mundo com gente lá joga
     * todo mundo para o mundo principal de qualquer jeito, e é melhor que isso
     * aconteça de propósito e no lugar certo.
     */
    boolean resetar(String mundo, org.bukkit.Location paraOnde) {
        Arena arena = arenas.get(mundo);
        if (arena == null) {
            return false;
        }
        File modelo = pastaDoModelo(mundo);
        if (!modelo.isDirectory()) {
            plugin.getLogger().warning("Arena " + mundo + " sem modelo guardado: "
                    + "rode /modelo " + mundo + " uma vez com o mapa do jeito certo.");
            return false;
        }

        World world = Bukkit.getWorld(mundo);
        if (world != null) {
            for (Player jogador : new ArrayList<>(world.getPlayers())) {
                if (paraOnde != null) {
                    jogador.teleport(paraOnde);
                }
            }
            // Sem salvar: o que aconteceu na partida vai ser jogado fora agora.
            if (!Bukkit.unloadWorld(world, false)) {
                plugin.getLogger().warning("Nao consegui descarregar " + mundo + ".");
                return false;
            }
        }

        long comeco = System.currentTimeMillis();
        try {
            apagar(pastaDoMundo(mundo).toPath());
            copiar(modelo.toPath(), pastaDoMundo(mundo).toPath());
        } catch (IOException erro) {
            plugin.getLogger().warning("Troca da pasta de " + mundo + " falhou: " + erro);
            return false;
        }
        World novo = new WorldCreator(mundo).createWorld();
        if (novo == null) {
            plugin.getLogger().warning("Arena " + mundo + " nao voltou depois do reset.");
            return false;
        }
        novo.setTime(6000);
        novo.setStorm(false);
        plugin.getLogger().info("Arena " + mundo + " refeita em "
                + (System.currentTimeMillis() - comeco) + " ms.");
        return true;
    }

    // ------------------------------------------------------------ comandos

    /**
     * O /setup: leva você à arena, em criativo, para construir.
     *
     * Sem lista de arena o comando não adivinha: dizer o nome errado e cair num
     * mundo que não existe seria pior que a mensagem de ajuda.
     */
    boolean setup(CommandSender quemPediu, String[] argumentos) {
        if (!(quemPediu instanceof Player jogador)) {
            quemPediu.sendMessage("Esse comando precisa ser dado em jogo.");
            return true;
        }
        if (argumentos.length != 1) {
            jogador.sendMessage(ChatColor.RED + "Use: /setup <arena>");
            for (Arena arena : arenas.values()) {
                jogador.sendMessage(ChatColor.GRAY + "  " + arena.mundo() + " — " + arena.nome());
            }
            return true;
        }
        Arena arena = arenas.get(argumentos[0]);
        if (arena == null) {
            jogador.sendMessage(ChatColor.RED + "Nao conheco a arena " + argumentos[0] + ".");
            return true;
        }
        World mundo = Bukkit.getWorld(arena.mundo());
        if (mundo == null) {
            jogador.sendMessage(ChatColor.RED + "A arena " + arena.mundo() + " nao esta carregada.");
            return true;
        }
        jogador.teleport(mundo.getSpawnLocation());
        jogador.setGameMode(org.bukkit.GameMode.CREATIVE);
        jogador.addScoreboardTag("gc_obras");
        jogador.sendMessage(ChatColor.GREEN + "Voce esta em " + arena.nome() + ".");
        jogador.sendMessage(ChatColor.GRAY + "Construa o mapa e depois use /save aqui dentro. "
                + "E o /save que decide para onde a arena volta ao fim de cada partida.");
        return true;
    }

    /** O /save: a arena onde você está vira o mapa oficial dela. */
    boolean salvarOndeEstou(CommandSender quemPediu) {
        if (!(quemPediu instanceof Player jogador)) {
            quemPediu.sendMessage("Esse comando precisa ser dado em jogo.");
            return true;
        }
        String mundo = jogador.getWorld().getName();
        if (!arenas.containsKey(mundo)) {
            jogador.sendMessage(ChatColor.RED + "Voce nao esta numa arena — esta em " + mundo + ".");
            return true;
        }
        if (guardarModelo(mundo, quemPediu)) {
            jogador.removeScoreboardTag("gc_obras");
            jogador.sendMessage(ChatColor.GRAY + "E para este estado que a arena volta "
                    + "no fim de cada partida.");
        }
        return true;
    }

    /** O /reset à mão. No fim da partida ele acontece sozinho. */
    boolean resetarOndeEstou(CommandSender quemPediu) {
        if (!(quemPediu instanceof Player jogador)) {
            quemPediu.sendMessage("Esse comando precisa ser dado em jogo.");
            return true;
        }
        String mundo = jogador.getWorld().getName();
        if (!arenas.containsKey(mundo)) {
            jogador.sendMessage(ChatColor.RED + "Voce nao esta numa arena — esta em " + mundo + ".");
            return true;
        }
        World principal = Bukkit.getWorlds().get(0);
        long comeco = System.currentTimeMillis();
        if (resetar(mundo, principal.getSpawnLocation())) {
            quemPediu.sendMessage(ChatColor.GREEN + "Arena " + mundo + " refeita em "
                    + (System.currentTimeMillis() - comeco) + " ms.");
        } else {
            quemPediu.sendMessage(ChatColor.RED + "Nao deu. Se a arena nunca foi salva, "
                    + "entre nela e rode /save primeiro.");
        }
        return true;
    }

    // ------------------------------------------------------------ arquivos

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

    private void copiar(Path origem, Path destino) throws IOException {
        Files.walkFileTree(origem, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path pasta, BasicFileAttributes atributos)
                    throws IOException {
                Files.createDirectories(destino.resolve(origem.relativize(pasta)));
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path arquivo, BasicFileAttributes atributos)
                    throws IOException {
                // O session.lock e do mundo que esta rodando, nao do mapa: copiar
                // ele faz o servidor achar que outra instancia abriu o mundo.
                if (arquivo.getFileName().toString().equals("session.lock")) {
                    return FileVisitResult.CONTINUE;
                }
                Files.copy(arquivo, destino.resolve(origem.relativize(arquivo)),
                        StandardCopyOption.REPLACE_EXISTING);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
