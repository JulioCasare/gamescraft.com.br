package br.com.gamescraft.menus;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Team;

/**
 * O ciclo de uma partida do MegaGames: espera, contagem, jogo e recomeço.
 *
 * Quem chega vai para o saguão e fica lá. Com oito pessoas o relógio começa a
 * correr; com trinta ele corre mais rápido, porque sala cheia esperando é sala
 * que se esvazia. Na largada os times são sorteados em partes iguais, e quem
 * chega no meio entra no lado que estiver perdendo em gente.
 *
 * A partida acaba quando alguém quebra o bloco do outro time — a redstone do
 * vermelho ou o lápis do azul. Aí todo mundo volta para o saguão e o mapa se
 * refaz sozinho: o /reset devolve os blocos e o /neutro devolve as torres.
 */
final class Partida implements Listener {

    private enum Fase {
        ESPERA, CONTAGEM, JOGO
    }

    /** Com menos que isto a partida não começa. */
    private static final int MINIMO = 8;

    /** A partir daqui a espera encolhe: sala cheia parada é sala que se esvazia. */
    private static final int SALA_CHEIA = 30;

    private static final int SEGUNDOS_NORMAIS = 30;
    private static final int SEGUNDOS_CORRIDOS = 10;

    private final JavaPlugin plugin;
    private final Times times;
    private final Captura captura;
    private final Armaduras armaduras;
    private final Ferramentas ferramentas;
    private final String nomeVermelho;
    private final String nomeAzul;
    private final File arquivo;
    private final YamlConfiguration guardado;

    private final Arenas arenas;
    private Fase fase = Fase.ESPERA;
    private int restante;

    /** Em qual mundo a partida esta acontecendo. */
    private String arenaAtual = "ilha";

    Partida(JavaPlugin plugin, Times times, Captura captura, Armaduras armaduras,
            Ferramentas ferramentas, Arenas arenas) {
        this.arenas = arenas;
        this.plugin = plugin;
        this.times = times;
        this.captura = captura;
        this.armaduras = armaduras;
        this.ferramentas = ferramentas;
        this.nomeVermelho = plugin.getConfig().getString("time-vermelho", "Vermelho");
        this.nomeAzul = plugin.getConfig().getString("time-azul", "Azul");
        this.arquivo = new File(plugin.getDataFolder(), "espera.yml");
        this.guardado = YamlConfiguration.loadConfiguration(arquivo);

        plugin.getServer().getScheduler().runTaskTimer(plugin, this::segundo, 20L, 20L);
    }

    // ------------------------------------------------------------- saguão

    /** O /espera: onde você está agora vira o saguão. */
    boolean definirEspera(CommandSender quemPediu) {
        if (!(quemPediu instanceof Player jogador)) {
            quemPediu.sendMessage("Esse comando precisa ser dado em jogo.");
            return true;
        }
        Location onde = jogador.getLocation();
        guardado.set("mundo", onde.getWorld().getName());
        guardado.set("x", onde.getX());
        guardado.set("y", onde.getY());
        guardado.set("z", onde.getZ());
        guardado.set("yaw", onde.getYaw());
        guardado.set("pitch", onde.getPitch());
        try {
            guardado.save(arquivo);
        } catch (IOException erro) {
            plugin.getLogger().warning("Nao consegui guardar o saguao: " + erro.getMessage());
        }
        jogador.sendMessage(ChatColor.GREEN + "Saguao marcado aqui. "
                + ChatColor.GRAY + "E para ca que todo mundo vem entre uma partida e outra.");
        return true;
    }

    /**
     * O /forcestart: larga a partida agora, com quem estiver online.
     *
     * Existe para testar. O gatilho normal precisa de oito pessoas, e uma rede
     * que ainda tem quatro jogadores nunca chegaria lá — sem este comando não
     * havia como ver o ciclo funcionando antes de o servidor encher.
     */
    boolean forcar(CommandSender quemPediu) {
        if (fase == Fase.JOGO) {
            quemPediu.sendMessage(ChatColor.RED + "Ja tem partida acontecendo.");
            return true;
        }
        int quantos = online().size();
        if (quantos < 2) {
            quemPediu.sendMessage(ChatColor.RED + "Precisa de pelo menos duas pessoas: "
                    + "com uma so nao ha dois times, e a partida acabaria sozinha.");
            return true;
        }
        anunciar(ChatColor.YELLOW + "Partida forcada por " + quemPediu.getName() + ".");
        comecar();
        return true;
    }

    Location saguao() {
        String nome = guardado.getString("mundo");
        if (nome == null) {
            return null;
        }
        World mundo = plugin.getServer().getWorld(nome);
        if (mundo == null) {
            return null;
        }
        return new Location(mundo, guardado.getDouble("x"), guardado.getDouble("y"),
                guardado.getDouble("z"), (float) guardado.getDouble("yaw"),
                (float) guardado.getDouble("pitch"));
    }

    /**
     * Manda para o saguão, de mãos vazias.
     *
     * Sem saguão marcado ninguém é teleportado: e melhor a pessoa ficar onde
     * esta do que ser jogada num lugar que o Julio nao escolheu.
     */
    private void paraOSaguao(Player jogador) {
        jogador.getInventory().clear();
        jogador.setGameMode(GameMode.ADVENTURE);
        jogador.setHealth(jogador.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).getValue());
        jogador.setFoodLevel(20);
        Location onde = saguao();
        if (onde != null) {
            jogador.teleport(onde);
        }
        Team antigo = jogador.getScoreboard().getEntryTeam(jogador.getName());
        if (antigo != null) {
            antigo.removeEntry(jogador.getName());
        }
    }

    // -------------------------------------------------------------- ciclo

    void aoEntrar(Player jogador) {
        if (fase == Fase.JOGO) {
            entrarNoMeio(jogador);
            return;
        }
        paraOSaguao(jogador);
        jogador.sendMessage(ChatColor.GRAY + "Esperando gente para comecar. "
                + "Faltam " + Math.max(0, MINIMO - online().size()) + " para a partida.");
    }

    private List<Player> online() {
        List<Player> gente = new ArrayList<>();
        for (Player jogador : Bukkit.getOnlinePlayers()) {
            // Quem esta de espectador ou de obras nao conta para a partida: um
            // deles e o Julio construindo, e ele nao deveria disparar o relogio.
            if (jogador.getGameMode() == GameMode.SPECTATOR
                    || jogador.getScoreboardTags().contains("gc_obras")) {
                continue;
            }
            gente.add(jogador);
        }
        return gente;
    }

    private void segundo() {
        int quantos = online().size();
        switch (fase) {
            case ESPERA -> {
                if (quantos >= MINIMO) {
                    fase = Fase.CONTAGEM;
                    restante = quantos >= SALA_CHEIA ? SEGUNDOS_CORRIDOS : SEGUNDOS_NORMAIS;
                    anunciar(ChatColor.YELLOW + "Gente suficiente. A partida comeca em "
                            + restante + " segundos.");
                }
            }
            case CONTAGEM -> {
                if (quantos < MINIMO) {
                    fase = Fase.ESPERA;
                    anunciar(ChatColor.GRAY + "Gente de menos. A contagem parou.");
                    return;
                }
                // A sala encheu no meio da contagem: o relogio encurta, mas nunca
                // aumenta — ninguem gosta de ver a espera crescer.
                if (quantos >= SALA_CHEIA && restante > SEGUNDOS_CORRIDOS) {
                    restante = SEGUNDOS_CORRIDOS;
                }
                restante--;
                if (restante == 20 || restante == 10 || restante == 5
                        || (restante <= 3 && restante > 0)) {
                    anunciar(ChatColor.YELLOW + "" + restante + "...");
                    for (Player jogador : online()) {
                        jogador.playSound(jogador.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1f, 1f);
                    }
                }
                if (restante <= 0) {
                    comecar();
                }
            }
            case JOGO -> {
            }
        }
    }

    /**
     * Sorteia os times em partes iguais e larga todo mundo no proprio castelo.
     *
     * O sorteio e refeito a cada partida, e nao herdado da anterior: dois times
     * que sempre jogam juntos viram um time forte e um fraco, e a segunda
     * partida ja comeca decidida.
     */
    private void comecar() {
        fase = Fase.JOGO;
        List<Player> gente = online();
        Collections.shuffle(gente);
        times.limparTimes();

        int i = 0;
        for (Player jogador : gente) {
            String time = (i++ % 2 == 0) ? nomeAzul : nomeVermelho;
            largar(jogador, time);
        }
        anunciar(ChatColor.GREEN + "Comecou! " + ChatColor.GRAY
                + "Quebre o bloco do outro time para vencer.");
    }

    /** Poe no time, leva ao castelo e entrega o kit. */
    private void largar(Player jogador, String time) {
        times.marcarTime(jogador, time);
        times.entrarNoTime(jogador, time);
        jogador.setGameMode(GameMode.ADVENTURE);
        Location casa = times.casaDo(time);
        if (casa != null) {
            jogador.teleport(casa);
        }
        times.darKit(jogador, time);
    }

    /** Quem chega com a partida rolando entra no lado que tem menos gente. */
    private void entrarNoMeio(Player jogador) {
        int azuis = 0;
        int vermelhos = 0;
        for (Player outro : online()) {
            if (outro == jogador) {
                continue;
            }
            Team time = outro.getScoreboard().getEntryTeam(outro.getName());
            if (time == null) {
                continue;
            }
            if (nomeVermelho.equals(time.getName())) {
                vermelhos++;
            } else if (nomeAzul.equals(time.getName())) {
                azuis++;
            }
        }
        String time = azuis <= vermelhos ? nomeAzul : nomeVermelho;
        largar(jogador, time);
        jogador.sendMessage(ChatColor.GRAY + "Partida em andamento: voce entrou no time que "
                + "estava com menos gente.");
    }

    // ------------------------------------------------------------ vitoria

    /**
     * Quebrar o bloco do outro time acaba a partida.
     *
     * O bloco e o mesmo que marca o nascimento — redstone no vermelho, lapis no
     * azul. Quebrar o proprio nao vale: seria perder de proposito, e alguem ia
     * tentar so para ver o que acontece.
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void aoQuebrar(BlockBreakEvent evento) {
        if (fase != Fase.JOGO) {
            return;
        }
        Block bloco = evento.getBlock();
        Material tipo = bloco.getType();
        if (tipo != Material.REDSTONE_BLOCK && tipo != Material.LAPIS_BLOCK) {
            return;
        }
        String dono = tipo == Material.REDSTONE_BLOCK ? nomeVermelho : nomeAzul;
        Team time = evento.getPlayer().getScoreboard().getEntryTeam(evento.getPlayer().getName());
        String meu = time == null ? null : time.getName();
        if (meu == null || meu.equals(dono)) {
            evento.setCancelled(true);
            evento.getPlayer().sendActionBar(ChatColor.RED + "Esse bloco e do seu time.");
            return;
        }
        // Deixa quebrar: o bloco do inimigo e a unica coisa do mapa que os
        // outros ouvintes nao devem barrar.
        evento.setCancelled(false);
        vitoria(meu, evento.getPlayer());
    }

    private void vitoria(String time, Player quem) {
        fase = Fase.ESPERA;
        ChatColor cor = time.equals(nomeVermelho) ? ChatColor.RED : ChatColor.BLUE;
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage(cor + "O time " + time + " venceu!");
        Bukkit.broadcastMessage(ChatColor.GRAY + quem.getName() + " quebrou o bloco inimigo.");
        Bukkit.broadcastMessage("");

        for (Player jogador : Bukkit.getOnlinePlayers()) {
            jogador.playSound(jogador.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
        }

        // Cinco segundos antes de recomecar: acabar a partida e teleportar no
        // mesmo instante nao deixa ninguem entender o que aconteceu.
        plugin.getServer().getScheduler().runTaskLater(plugin, this::recomecar, 100L);
    }

    /** Devolve todo mundo ao saguão e refaz o mapa. */
    private void recomecar() {
        for (Player jogador : Bukkit.getOnlinePlayers()) {
            paraOSaguao(jogador);
        }
        times.limparTimes();
        armaduras.limparTudo();
        ferramentas.limparTudo();
        captura.zerar(Bukkit.getConsoleSender());

        // Troca a pasta do mundo pela copia guardada. Antes isso era um /clone de
        // trinta e quatro milhoes de blocos, que levava mais de um minuto com todo
        // mundo parado no saguao esperando.
        long comeco = System.currentTimeMillis();
        boolean deu = arenas != null && arenas.resetar(arenaAtual, saguao());
        if (deu) {
            plugin.getLogger().info("Arena " + arenaAtual + " refeita em "
                    + (System.currentTimeMillis() - comeco) + " ms.");
        } else {
            plugin.getLogger().warning("Arena " + arenaAtual + " nao foi refeita: "
                    + "provavelmente ela nunca foi salva com /save.");
        }
        anunciar(ChatColor.GRAY + "Mapa refeito. A proxima partida comeca "
                + "quando houver " + MINIMO + " pessoas.");
    }

    private void anunciar(String recado) {
        Bukkit.broadcastMessage(recado);
    }
}
