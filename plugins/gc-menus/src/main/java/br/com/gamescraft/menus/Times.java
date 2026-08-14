package br.com.gamescraft.menus;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Team;

/**
 * Divide quem entra entre os dois times, devolve cada um ao castelo do seu, e
 * paga o time pelas torres que ele tem.
 *
 * O time de cada jogador fica gravado em arquivo. Sortear de novo a cada entrada
 * deixaria alguém trocar de lado só saindo e voltando, e ainda bagunçaria o
 * placar no meio da partida.
 */
final class Times implements Listener {

    private static final int SEGUNDOS_DO_PAGAMENTO = 30;

    private final JavaPlugin plugin;
    private final Captura captura;
    private final File arquivo;
    private final YamlConfiguration guardados;
    private final String nomeVermelho;
    private final String nomeAzul;
    private final String nomeMundo;

    Times(JavaPlugin plugin, Captura captura) {
        this.plugin = plugin;
        this.captura = captura;
        this.arquivo = new File(plugin.getDataFolder(), "times.yml");
        this.guardados = YamlConfiguration.loadConfiguration(arquivo);
        this.nomeVermelho = plugin.getConfig().getString("time-vermelho", "Vermelho");
        this.nomeAzul = plugin.getConfig().getString("time-azul", "Azul");
        this.nomeMundo = plugin.getConfig().getString("mundo", "ilha");

        plugin.getServer().getScheduler().runTaskTimer(plugin, this::pagar,
                SEGUNDOS_DO_PAGAMENTO * 20L, SEGUNDOS_DO_PAGAMENTO * 20L);
    }

    @EventHandler
    public void aoEntrar(PlayerJoinEvent evento) {
        Player jogador = evento.getPlayer();
        String time = escolherTime(jogador.getUniqueId());
        entrarNoTime(jogador, time);
        Location casa = casaDo(time);
        if (casa != null) {
            jogador.teleport(casa);
        }
        darKit(jogador, time);
    }

    @EventHandler
    public void aoRenascer(PlayerRespawnEvent evento) {
        String time = guardados.getString(evento.getPlayer().getUniqueId().toString());
        if (time == null) {
            return;
        }
        Location casa = casaDo(time);
        if (casa != null) {
            evento.setRespawnLocation(casa);
        }
        // O kit vem no tique seguinte: no momento do respawn o inventario ainda
        // está sendo limpo pelo próprio jogo, e o que se dá aqui se perde.
        plugin.getServer().getScheduler().runTask(plugin, () -> darKit(evento.getPlayer(), time));
    }

    /** Time guardado, ou o menor dos dois para quem chega agora. */
    private String escolherTime(UUID quem) {
        String guardado = guardados.getString(quem.toString());
        if (guardado != null) {
            return guardado;
        }
        int vermelhos = 0;
        int azuis = 0;
        for (String chave : guardados.getKeys(false)) {
            if (nomeVermelho.equals(guardados.getString(chave))) {
                vermelhos++;
            } else {
                azuis++;
            }
        }
        String time = vermelhos <= azuis ? nomeVermelho : nomeAzul;
        guardados.set(quem.toString(), time);
        try {
            guardados.save(arquivo);
        } catch (IOException erro) {
            plugin.getLogger().warning("Nao consegui guardar o time: " + erro.getMessage());
        }
        return time;
    }

    private void entrarNoTime(Player jogador, String nome) {
        Team time = jogador.getScoreboard().getTeam(nome);
        if (time == null) {
            time = jogador.getScoreboard().registerNewTeam(nome);
            time.setColor(nome.equals(nomeVermelho) ? ChatColor.RED : ChatColor.BLUE);
        }
        time.addEntry(jogador.getName());
        jogador.sendMessage((nome.equals(nomeVermelho) ? ChatColor.RED : ChatColor.BLUE)
                + "Voce esta no time " + nome + ".");
    }

    /** O topo do sinalizador do castelo daquele time. */
    private Location casaDo(String time) {
        World mundo = plugin.getServer().getWorld(nomeMundo);
        if (mundo == null) {
            return null;
        }
        String cor = time.equals(nomeVermelho) ? "vermelho" : "azul";
        for (String linha : plugin.getConfig().getStringList("areas-construcao")) {
            String[] p = linha.split(",");
            if (p.length < 7 || !p[6].trim().equalsIgnoreCase(cor)) {
                continue;
            }
            int minx = Math.min(Integer.parseInt(p[0].trim()), Integer.parseInt(p[3].trim()));
            int maxx = Math.max(Integer.parseInt(p[0].trim()), Integer.parseInt(p[3].trim()));
            int miny = Math.min(Integer.parseInt(p[1].trim()), Integer.parseInt(p[4].trim()));
            int maxy = Math.max(Integer.parseInt(p[1].trim()), Integer.parseInt(p[4].trim()));
            int minz = Math.min(Integer.parseInt(p[2].trim()), Integer.parseInt(p[5].trim()));
            int maxz = Math.max(Integer.parseInt(p[2].trim()), Integer.parseInt(p[5].trim()));
            // Procura o sinalizador dentro da caixa do castelo. Nao da para usar
            // a lista da varredura: ela nao enxerga os sinalizadores de dentro
            // dos castelos, e essa e outra briga.
            for (int y = maxy; y >= miny; y--) {
                for (int x = minx; x <= maxx; x++) {
                    for (int z = minz; z <= maxz; z++) {
                        Block bloco = mundo.getBlockAt(x, y, z);
                        if (bloco.getType() == Material.BEACON) {
                            return new Location(mundo, x + 0.5, y + 1, z + 0.5);
                        }
                    }
                }
            }
        }
        return null;
    }

    /** Couro tingido da cor do time e espada de madeira. */
    private void darKit(Player jogador, String time) {
        Color cor = time.equals(nomeVermelho) ? Color.RED : Color.BLUE;
        jogador.getInventory().clear();
        jogador.getInventory().setHelmet(couro(Material.LEATHER_HELMET, cor));
        jogador.getInventory().setChestplate(couro(Material.LEATHER_CHESTPLATE, cor));
        jogador.getInventory().setLeggings(couro(Material.LEATHER_LEGGINGS, cor));
        jogador.getInventory().setBoots(couro(Material.LEATHER_BOOTS, cor));
        jogador.getInventory().addItem(new ItemStack(Material.WOODEN_SWORD));
    }

    private ItemStack couro(Material tipo, Color cor) {
        ItemStack peca = new ItemStack(tipo);
        LeatherArmorMeta dados = (LeatherArmorMeta) peca.getItemMeta();
        dados.setColor(cor);
        peca.setItemMeta(dados);
        return peca;
    }

    /**
     * Paga a cada time um diamante por torre que ele tem, a cada trinta
     * segundos. Quem tem mais torre ganha mais rápido — é o que faz valer a pena
     * sair do castelo.
     */
    private void pagar() {
        int doVermelho = captura.quantasDoTime(nomeVermelho);
        int doAzul = captura.quantasDoTime(nomeAzul);
        for (Player jogador : Bukkit.getOnlinePlayers()) {
            String time = guardados.getString(jogador.getUniqueId().toString());
            if (time == null) {
                continue;
            }
            int quantos = time.equals(nomeVermelho) ? doVermelho : doAzul;
            if (quantos <= 0) {
                continue;
            }
            jogador.getInventory().addItem(new ItemStack(Material.DIAMOND, quantos));
            jogador.sendActionBar(ChatColor.AQUA + "+" + quantos + " diamante"
                    + (quantos > 1 ? "s" : "") + " pelas torres do time");
        }
    }
}
