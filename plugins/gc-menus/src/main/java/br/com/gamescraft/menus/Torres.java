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
    private final Set<Torre> centrais = new LinkedHashSet<>();
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
        acharCentrais();
    }

    int quantas() {
        return torres.size();
    }

    /** As torres achadas, como pares x,z. */
    java.util.List<int[]> posicoes() {
        java.util.List<int[]> saida = new ArrayList<>();
        for (Torre torre : torres) {
            saida.add(new int[] { torre.x(), torre.z() });
        }
        return saida;
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
        acharCentrais();
        String recado = "Sinalizadores protegidos: " + torres.size();
        quemPediu.sendMessage(ChatColor.GREEN + recado);
        plugin.getLogger().info(recado);
    }

    /** Quanto a proteção desce abaixo do sinalizador, e quanto sobe acima dele. */
    private static final int ABAIXO = 5;
    private static final int ACIMA = 15;

    /** Altura do sinalizador de cada torre, achada uma vez e conferida ao usar. */
    private final java.util.Map<Torre, Integer> alturas = new java.util.HashMap<>();

    /**
     * A proteção é uma caixa em volta do sinalizador, e não uma coluna infinita.
     *
     * Ela ia do fundo do mundo até o teto, e isso não se via: quem cavava o chão
     * a três blocos de uma torre, ou passava numa caverna sessenta blocos abaixo
     * dela, batia num bloco que não quebrava sem nada explicar. Cinco abaixo e
     * quinze acima cobrem a torre inteira e devolvem o resto do mapa.
     */
    boolean protegido(Block bloco) {
        int x = bloco.getX();
        int z = bloco.getZ();
        for (Torre torre : torres) {
            int raio = raioDe(torre);
            if (Math.abs(x - torre.x()) > raio || Math.abs(z - torre.z()) > raio) {
                continue;
            }
            int base = alturaDoBeacon(bloco.getWorld(), torre);
            if (base < 0) {
                // Sem sinalizador ali — outro mundo, ou torre já derrubada.
                continue;
            }
            if (bloco.getY() >= base - ABAIXO && bloco.getY() <= base + ACIMA) {
                return true;
            }
        }
        return false;
    }

    /**
     * Onde está o sinalizador daquela torre.
     *
     * Guardado, mas conferido antes de cada uso: uma cópia do mapa acima já fez
     * a busca achar o sinalizador errado, e a altura errada sobreviveu na
     * memória mesmo depois de a cópia sumir.
     */
    private int alturaDoBeacon(org.bukkit.World mundo, Torre torre) {
        if (mundo == null) {
            return -1;
        }
        Integer guardada = alturas.get(torre);
        if (guardada != null && guardada >= 0
                && mundo.getBlockAt(torre.x(), guardada, torre.z()).getType() == Material.BEACON) {
            return guardada;
        }
        int teto = Math.min(plugin.getConfig().getInt("altura-maxima-da-torre", 150),
                mundo.getMaxHeight() - 1);
        for (int y = teto; y >= mundo.getMinHeight(); y--) {
            if (mundo.getBlockAt(torre.x(), y, torre.z()).getType() == Material.BEACON) {
                alturas.put(torre, y);
                return y;
            }
        }
        alturas.put(torre, -1);
        return -1;
    }

    /**
     * Três blocos para cada lado em quase tudo, e só a coluna do feixe no
     * sinalizador do meio de cada castelo.
     *
     * O do meio é o que marca o castelo, e é em volta dele que a defesa se
     * constrói: um quadrado de sete por sete intocável bem ali atrapalharia mais
     * do que protege. O que não pode é tapar o feixe dele.
     *
     * Os outros quatro do castelo valem como qualquer torre do campo — são
     * cantos de mapa, e ninguém constrói em cima deles.
     */
    private int raioDe(Torre torre) {
        return centrais.contains(torre) ? 0 : RAIO;
    }

    /**
     * Descobre, em cada castelo, qual sinalizador é o do meio: o mais perto do
     * centro da caixa.
     *
     * É calculado e não escrito à mão porque a lista de torres se refaz sozinha
     * pelo /torres, e uma lista à mão ficaria velha na primeira varredura.
     */
    private void acharCentrais() {
        centrais.clear();
        for (String linha : plugin.getConfig().getStringList("areas-construcao")) {
            String[] p = linha.split(",");
            if (p.length < 6) {
                continue;
            }
            int minx = Math.min(Integer.parseInt(p[0].trim()), Integer.parseInt(p[3].trim()));
            int maxx = Math.max(Integer.parseInt(p[0].trim()), Integer.parseInt(p[3].trim()));
            int minz = Math.min(Integer.parseInt(p[2].trim()), Integer.parseInt(p[5].trim()));
            int maxz = Math.max(Integer.parseInt(p[2].trim()), Integer.parseInt(p[5].trim()));
            int meiox = (minx + maxx) / 2;
            int meioz = (minz + maxz) / 2;
            Torre central = null;
            long menor = Long.MAX_VALUE;
            for (Torre torre : torres) {
                if (torre.x() < minx || torre.x() > maxx || torre.z() < minz || torre.z() > maxz) {
                    continue;
                }
                long dx = torre.x() - meiox;
                long dz = torre.z() - meioz;
                long dist = dx * dx + dz * dz;
                if (dist < menor) {
                    menor = dist;
                    central = torre;
                }
            }
            if (central != null) {
                centrais.add(central);
            }
        }
        plugin.getLogger().info("Sinalizadores do meio dos castelos: " + centrais.size());
    }

    /** Quem ligou o /obras passa; o resto, não. */
    /**
     * Quem passa por cima da proteção: só quem está em criativo.
     *
     * Antes era a marca do /obras, e ela vinha junto com ser operador — o que
     * fazia a proteção valer para uns e não para outros dentro da mesma partida.
     * Agora a regra é do modo de jogo, e vale igual para todos: em sobrevivência
     * ninguém quebra o que é do mapa, operador ou não; em criativo todo mundo
     * quebra, operador ou não.
     *
     * A vantagem é que dá para conferir a proteção sem tirar o próprio op: basta
     * entrar em sobrevivência e bater no bloco.
     */
    boolean podeMexer(Player jogador) {
        return jogador.getGameMode() == org.bukkit.GameMode.CREATIVE;
    }

    /**
     * Silêncio para quem joga, explicação para quem administra.
     *
     * O aviso a cada clique atrapalhava quem minerava ao lado de uma torre, e
     * por isso saiu. Mas sem ele não há como descobrir qual das três proteções
     * barrou um bloco — e a coluna da torre é invisível: três blocos para cada
     * lado do sinalizador, do fundo do mundo até o teto.
     */
    static void explicar(Player jogador, String motivo) {
        if (jogador.hasPermission("gcmenus.npcskin")) {
            jogador.sendActionBar(ChatColor.GRAY + motivo);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void aoQuebrar(BlockBreakEvent evento) {
        if (protegido(evento.getBlock()) && !podeMexer(evento.getPlayer())) {
            evento.setCancelled(true);
            explicar(evento.getPlayer(), "Coluna de torre protegida — 3 blocos para cada lado.");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void aoColocar(BlockPlaceEvent evento) {
        if (protegido(evento.getBlock()) && !podeMexer(evento.getPlayer())) {
            evento.setCancelled(true);
        }
    }

    /** Balde de água ou lava conta como colocar bloco. */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void aoEsvaziarBalde(PlayerBucketEmptyEvent evento) {
        if (protegido(evento.getBlock()) && !podeMexer(evento.getPlayer())) {
            evento.setCancelled(true);
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
