package br.com.gamescraft.menus;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * As áreas de construção do MegaGames.
 *
 * A regra pedida: dentro delas ninguém quebra o que já estava lá, mas todo mundo
 * pode colocar bloco — e derrubar o que colocou. As colunas das torres continuam
 * intocáveis por cima disso, e é a classe {@link Torres} que cuida delas.
 *
 * Por isso a lista do que foi colocado precisa existir: sem ela não há como
 * distinguir "bloco do mapa" de "bloco que alguém pôs agora", e a única escolha
 * seria travar tudo ou liberar tudo. A lista vai para um arquivo, senão um
 * reinício transformaria todo bloco colocado em bloco do mapa.
 */
final class Areas implements Listener {

    private record Caixa(int minx, int miny, int minz, int maxx, int maxy, int maxz) {
        boolean contem(Block bloco) {
            return bloco.getX() >= minx && bloco.getX() <= maxx
                    && bloco.getY() >= miny && bloco.getY() <= maxy
                    && bloco.getZ() >= minz && bloco.getZ() <= maxz;
        }
    }

    private final JavaPlugin plugin;
    private final Torres torres;
    private final List<Caixa> caixas = new ArrayList<>();
    private final Set<String> colocados = new HashSet<>();
    private final Set<String> emObras = new HashSet<>();
    private final File arquivo;
    private boolean sujo;

    Areas(JavaPlugin plugin, Torres torres) {
        this.plugin = plugin;
        this.torres = torres;
        this.arquivo = new File(plugin.getDataFolder(), "colocados.yml");

        for (String linha : plugin.getConfig().getStringList("areas-construcao")) {
            String[] p = linha.split(",");
            // Seis numeros, e um setimo campo opcional com a cor do castelo. A
            // cor so interessa a pintura; aqui o que vale sao os cantos.
            if (p.length < 6) {
                continue;
            }
            int[] v = new int[6];
            for (int i = 0; i < 6; i++) {
                v[i] = Integer.parseInt(p[i].trim());
            }
            caixas.add(new Caixa(Math.min(v[0], v[3]), Math.min(v[1], v[4]), Math.min(v[2], v[5]),
                    Math.max(v[0], v[3]), Math.max(v[1], v[4]), Math.max(v[2], v[5])));
        }
        colocados.addAll(YamlConfiguration.loadConfiguration(arquivo).getStringList("blocos"));
        plugin.getLogger().info("Areas de construcao: " + caixas.size()
                + " | blocos colocados guardados: " + colocados.size());

        // Grava de trinta em trinta segundos, e nao a cada bloco: numa area de
        // construcao o povo coloca bloco as dezenas por segundo.
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, this::salvar, 600L, 600L);
        plugin.getServer().getScheduler().runTaskTimer(plugin, this::verObras, 40L, 20L);
    }

    /**
     * Desligar o /obras congela o mapa: tudo o que está de pé vira castelo.
     *
     * É o mesmo sentido que o /obras já tem no datapack — "acabei de editar,
     * este é o novo estado de referência". Sem isto, cada bloco posto durante
     * uma obra continuaria contando como defesa de jogador, e uma TNT levaria
     * junto a mobília que o Julio acabou de construir.
     */
    private void verObras() {
        Set<String> agora = new HashSet<>();
        for (Player jogador : plugin.getServer().getOnlinePlayers()) {
            if (torres.podeMexer(jogador)) {
                agora.add(jogador.getName());
            }
        }
        for (String quem : emObras) {
            if (agora.contains(quem)) {
                continue;
            }
            Player jogador = plugin.getServer().getPlayerExact(quem);
            int quantos = colocados.size();
            colocados.clear();
            sujo = true;
            if (jogador != null && quantos > 0) {
                jogador.sendMessage(ChatColor.GREEN + "Obras desligadas: " + quantos
                        + " blocos passaram a contar como mapa.");
            }
            break;
        }
        emObras.clear();
        emObras.addAll(agora);
    }

    private String chave(Block bloco) {
        return bloco.getX() + "," + bloco.getY() + "," + bloco.getZ();
    }

    /** Se aquele bloco foi posto por um jogador, e nao veio do mapa. */
    boolean foiColocado(Block bloco) {
        return colocados.contains(chave(bloco));
    }

    /**
     * Anota um bloco que o plugin pos no lugar do jogador — a torre compacta da
     * loja, por exemplo. Sem isto ele entraria na conta como bloco do mapa e
     * ficaria intocavel dentro de um castelo.
     */
    void registrarColocado(Block bloco) {
        if (!dentro(bloco)) {
            return;
        }
        colocados.add(chave(bloco));
        sujo = true;
    }

    private boolean dentro(Block bloco) {
        for (Caixa caixa : caixas) {
            if (caixa.contem(bloco)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Colocar: liberado na área, menos na coluna de uma torre. A coluna vence
     * porque ela é a única coisa que precisa ficar igual para a partida.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void aoColocar(BlockPlaceEvent evento) {
        Block bloco = evento.getBlock();
        if (!dentro(bloco)) {
            return;
        }
        if (torres.protegido(bloco) && !torres.podeMexer(evento.getPlayer())) {
            evento.setCancelled(true);
            return;
        }
        // Quem está de /obras está construindo o mapa, e não jogando: o que ele
        // põe é castelo, não defesa. Sem esta distinção o baú e a alavanca que
        // o Julio pôs entravam na lista do que pode ser quebrado e explodido.
        if (torres.podeMexer(evento.getPlayer())) {
            colocados.remove(chave(bloco));
            sujo = true;
            return;
        }
        colocados.add(chave(bloco));
        sujo = true;
    }

    /** Quebrar: só o que foi colocado depois. O bloco do mapa fica. */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void aoQuebrar(BlockBreakEvent evento) {
        Block bloco = evento.getBlock();
        if (!dentro(bloco)) {
            return;
        }
        // A redstone e o lapis sao a excecao: sao blocos do mapa, mas quebra-los
        // e como se ganha a partida. Quem decide se aquele golpe vale e a
        // Partida, que ja conferiu de quem e o bloco antes de deixar passar.
        if (bloco.getType() == Material.REDSTONE_BLOCK || bloco.getType() == Material.LAPIS_BLOCK) {
            return;
        }
        if (torres.podeMexer(evento.getPlayer())) {
            colocados.remove(chave(bloco));
            sujo = true;
            return;
        }
        if (colocados.remove(chave(bloco))) {
            sujo = true;
            return;
        }
        evento.setCancelled(true);
        Torres.explicar(evento.getPlayer(), "Bloco do mapa dentro do castelo.");
    }

    /**
     * Explosão não derruba castelo.
     *
     * A bola de fogo e a TNT abriam buraco na parede e no chão da base, e o
     * castelo não se conserta sozinho: em duas partidas ele virava ruína. O que
     * os jogadores puseram continua indo pelos ares — é defesa, e defesa se
     * quebra.
     */
    private boolean doMapaEmArea(Block bloco) {
        return dentro(bloco) && !foiColocado(bloco);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void aoExplodirEntidade(EntityExplodeEvent evento) {
        evento.blockList().removeIf(this::doMapaEmArea);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void aoExplodirBloco(BlockExplodeEvent evento) {
        evento.blockList().removeIf(this::doMapaEmArea);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void aoQueimar(BlockBurnEvent evento) {
        if (doMapaEmArea(evento.getBlock())) {
            evento.setCancelled(true);
        }
    }

    void salvar() {
        if (!sujo) {
            return;
        }
        sujo = false;
        YamlConfiguration dados = new YamlConfiguration();
        dados.set("blocos", new ArrayList<>(colocados));
        try {
            dados.save(arquivo);
        } catch (IOException erro) {
            plugin.getLogger().warning("Nao consegui guardar a lista de blocos colocados: "
                    + erro.getMessage());
        }
    }
}
