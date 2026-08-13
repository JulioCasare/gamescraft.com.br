package br.com.gamescraft.menus;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Deixa as torres intocáveis: dentro da coluna de cada uma não se quebra nem se
 * coloca bloco, do fundo do mundo até o teto.
 *
 * Por que impedir e não consertar: a versão anterior guardava uma cópia de cada
 * torre e devolvia quando via diferença. Funcionava, mas só dentro da caixa
 * copiada — e copiar a coluna inteira seria quase um milhão de blocos por torre
 * para comparar a cada meio segundo. Barrar o clique custa uma subtração e vale
 * para qualquer altura.
 *
 * Quem acha as torres é o próprio plugin, varrendo o mapa atrás de
 * sinalizadores. Manter a lista à mão não funcionou: eu tinha 48 numa ilha com
 * 116, e as outras 68 ficavam desprotegidas sem ninguém perceber.
 *
 * A porta de saída é o mesmo /obras do datapack: quem está com a marca gc_obras
 * passa por cima da trava.
 */
final class Torres implements Listener {

    /** Quanto a proteção se estende para cada lado do sinalizador. */
    private static final int RAIO = 3;

    /** Chunks varridos por tique. Mais que isso trava o servidor na varredura. */
    private static final int CHUNKS_POR_TIQUE = 12;

    private record Torre(int x, int z) {
    }

    /** Uma caixa em planta: as áreas que ficam de fora da proteção por torre. */
    private record Excluida(int minx, int minz, int maxx, int maxz) {
        boolean contem(int x, int z) {
            return x >= minx && x <= maxx && z >= minz && z <= maxz;
        }
    }

    private final JavaPlugin plugin;
    private final Set<Torre> torres = new LinkedHashSet<>();
    private final List<Excluida> excluidas = new ArrayList<>();
    private boolean varrendo;

    Torres(JavaPlugin plugin) {
        this.plugin = plugin;
        for (String linha : plugin.getConfig().getStringList("areas-fora")) {
            String[] p = linha.split(",");
            if (p.length == 4) {
                excluidas.add(new Excluida(
                        Math.min(Integer.parseInt(p[0].trim()), Integer.parseInt(p[2].trim())),
                        Math.min(Integer.parseInt(p[1].trim()), Integer.parseInt(p[3].trim())),
                        Math.max(Integer.parseInt(p[0].trim()), Integer.parseInt(p[2].trim())),
                        Math.max(Integer.parseInt(p[1].trim()), Integer.parseInt(p[3].trim()))));
            }
        }
        for (String linha : plugin.getConfig().getStringList("torres")) {
            String[] p = linha.split(",");
            if (p.length == 2) {
                torres.add(new Torre(Integer.parseInt(p[0].trim()), Integer.parseInt(p[1].trim())));
            }
        }
        plugin.getLogger().info("Torres protegidas: " + torres.size()
                + " | areas fora da conta: " + excluidas.size());
    }

    int quantas() {
        return torres.size();
    }

    /**
     * Varre o mapa atrás de sinalizadores e refaz a lista.
     *
     * Vai de doze chunks por tique porque carregar mil e duzentos de uma vez
     * segura o servidor por segundos — e quem está jogando sente.
     */
    void varrer(CommandSender quemPediu) {
        if (varrendo) {
            quemPediu.sendMessage(ChatColor.YELLOW + "Ja tem uma varredura em andamento.");
            return;
        }
        String nomeMundo = plugin.getConfig().getString("mundo", "ilha");
        World mundo = plugin.getServer().getWorld(nomeMundo);
        if (mundo == null) {
            quemPediu.sendMessage(ChatColor.RED + "Nao achei o mundo " + nomeMundo + ".");
            return;
        }
        int alcance = plugin.getConfig().getInt("alcance-da-varredura", 288);
        int deChunk = -alcance >> 4;
        int ateChunk = alcance >> 4;
        varrendo = true;
        Set<Torre> achadas = new LinkedHashSet<>();
        quemPediu.sendMessage(ChatColor.GRAY + "Varrendo o mapa atras de sinalizadores...");

        new BukkitRunnable() {
            int cx = deChunk;
            int cz = deChunk;

            @Override
            public void run() {
                for (int feitos = 0; feitos < CHUNKS_POR_TIQUE; feitos++) {
                    if (cx > ateChunk) {
                        terminar(quemPediu, achadas);
                        cancel();
                        return;
                    }
                    Chunk chunk = mundo.getChunkAt(cx, cz);
                    for (BlockState estado : chunk.getTileEntities()) {
                        if (estado.getType() != Material.BEACON) {
                            continue;
                        }
                        int x = estado.getX();
                        int z = estado.getZ();
                        boolean fora = false;
                        for (Excluida area : excluidas) {
                            if (area.contem(x, z)) {
                                fora = true;
                                break;
                            }
                        }
                        if (!fora) {
                            achadas.add(new Torre(x, z));
                        }
                    }
                    cz++;
                    if (cz > ateChunk) {
                        cz = deChunk;
                        cx++;
                    }
                }
            }
        }.runTaskTimer(plugin, 1L, 1L);
    }

    private void terminar(CommandSender quemPediu, Set<Torre> achadas) {
        varrendo = false;
        torres.clear();
        torres.addAll(achadas);
        List<String> linhas = new ArrayList<>();
        for (Torre torre : torres) {
            linhas.add(torre.x() + "," + torre.z());
        }
        plugin.getConfig().set("torres", linhas);
        plugin.saveConfig();
        String recado = "Sinalizadores protegidos: " + torres.size();
        quemPediu.sendMessage(ChatColor.GREEN + recado);
        plugin.getLogger().info(recado);
    }

    /** A coluna vale para qualquer altura, então só x e z entram na conta. */
    boolean protegido(Block bloco) {
        int x = bloco.getX();
        int z = bloco.getZ();
        for (Torre torre : torres) {
            if (Math.abs(x - torre.x()) <= RAIO && Math.abs(z - torre.z()) <= RAIO) {
                return true;
            }
        }
        return false;
    }

    /** Quem ligou o /obras passa; o resto, não. */
    boolean podeMexer(Player jogador) {
        return jogador.getScoreboardTags().contains("gc_obras");
    }

    private void avisar(Player jogador) {
        jogador.sendActionBar(ChatColor.RED + "Torre protegida.");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void aoQuebrar(BlockBreakEvent evento) {
        if (protegido(evento.getBlock()) && !podeMexer(evento.getPlayer())) {
            evento.setCancelled(true);
            avisar(evento.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void aoColocar(BlockPlaceEvent evento) {
        if (protegido(evento.getBlock()) && !podeMexer(evento.getPlayer())) {
            evento.setCancelled(true);
            avisar(evento.getPlayer());
        }
    }

    /** Balde de água ou lava conta como colocar bloco. */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void aoEsvaziarBalde(PlayerBucketEmptyEvent evento) {
        if (protegido(evento.getBlock()) && !podeMexer(evento.getPlayer())) {
            evento.setCancelled(true);
            avisar(evento.getPlayer());
        }
    }

    /**
     * Explosão não tem dono na hora do estouro, então não há /obras que valha:
     * os blocos protegidos saem da lista do que vai pelos ares.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void aoExplodirEntidade(EntityExplodeEvent evento) {
        evento.blockList().removeIf(this::protegido);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void aoExplodirBloco(BlockExplodeEvent evento) {
        evento.blockList().removeIf(this::protegido);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void aoQueimar(BlockBurnEvent evento) {
        if (protegido(evento.getBlock())) {
            evento.setCancelled(true);
        }
    }

    /** Água e lava correndo de fora para dentro também mudam bloco. */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void aoEscorrer(BlockFromToEvent evento) {
        if (protegido(evento.getToBlock())) {
            evento.setCancelled(true);
        }
    }
}
