package br.com.gamescraft.menus;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
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
    private final File arquivo;
    private boolean sujo;

    Areas(JavaPlugin plugin, Torres torres) {
        this.plugin = plugin;
        this.torres = torres;
        this.arquivo = new File(plugin.getDataFolder(), "colocados.yml");

        for (String linha : plugin.getConfig().getStringList("areas-construcao")) {
            String[] p = linha.split(",");
            if (p.length != 6) {
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
    }

    private String chave(Block bloco) {
        return bloco.getX() + "," + bloco.getY() + "," + bloco.getZ();
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
            evento.getPlayer().sendActionBar(ChatColor.RED + "Nao da para construir em cima da torre.");
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
        evento.getPlayer().sendActionBar(ChatColor.RED + "Esse bloco e do mapa.");
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
