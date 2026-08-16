package br.com.gamescraft.menus;

import java.io.File;
import java.io.IOException;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Team;

/**
 * Duas regras da partida que não cabiam em nenhuma outra peça.
 *
 * Não se fabrica nada: tudo o que existe vem da loja ou do kit. Bancada
 * transformaria a madeira de dois diamantes numa espada de graça, e a lista de
 * preços — que é o que equilibra a disputa — deixaria de valer.
 *
 * E o baú é de quem o pôs: só o time dele abre. Sem isso, guardar diamante num
 * baú seria dar de presente ao primeiro que passasse, e o time que ataca
 * levaria também a poupança do que ficou defendendo.
 */
final class Regras implements Listener {

    private final JavaPlugin plugin;
    private final String timeVermelho;
    private final File arquivo;
    private final YamlConfiguration donos;

    Regras(JavaPlugin plugin) {
        this.plugin = plugin;
        this.timeVermelho = plugin.getConfig().getString("time-vermelho", "Vermelho");
        this.arquivo = new File(plugin.getDataFolder(), "baus.yml");
        this.donos = YamlConfiguration.loadConfiguration(arquivo);
    }

    // ---------------------------------------------------------- fabricação

    /**
     * Some com o resultado de qualquer receita.
     *
     * É o ponto mais alto do caminho: apaga a saída antes de a bancada mostrar
     * o item, então não há clique a barrar nem mensagem de erro para explicar —
     * a receita simplesmente não existe.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void aoMontarReceita(PrepareItemCraftEvent evento) {
        evento.getInventory().setResult(null);
    }

    // ---------------------------------------------------------------- baús

    private boolean ehBau(Material tipo) {
        return tipo == Material.CHEST || tipo == Material.TRAPPED_CHEST || tipo == Material.BARREL;
    }

    private String chave(Block bloco) {
        return bloco.getX() + "," + bloco.getY() + "," + bloco.getZ();
    }

    private String timeDe(Player jogador) {
        Team time = jogador.getScoreboard().getEntryTeam(jogador.getName());
        return time == null ? null : time.getName();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void aoPor(BlockPlaceEvent evento) {
        if (!ehBau(evento.getBlock().getType())) {
            return;
        }
        String time = timeDe(evento.getPlayer());
        if (time == null) {
            return;
        }
        donos.set(chave(evento.getBlock()), time);
        salvar();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void aoQuebrar(BlockBreakEvent evento) {
        if (!ehBau(evento.getBlock().getType())) {
            return;
        }
        if (donos.contains(chave(evento.getBlock()))) {
            donos.set(chave(evento.getBlock()), null);
            salvar();
        }
    }

    /**
     * Abrir: só o time do dono.
     *
     * Baú duplo conta as duas metades, porque quem põe a segunda pode ser do
     * outro time — e nesse caso a metade mais antiga é que manda.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void aoAbrir(PlayerInteractEvent evento) {
        if (evento.getAction() != Action.RIGHT_CLICK_BLOCK || evento.getClickedBlock() == null) {
            return;
        }
        Block bloco = evento.getClickedBlock();
        if (!ehBau(bloco.getType())) {
            return;
        }
        String dono = donoDe(bloco);
        if (dono == null) {
            return;
        }
        String meu = timeDe(evento.getPlayer());
        if (dono.equals(meu)) {
            return;
        }
        evento.setCancelled(true);
        evento.getPlayer().sendActionBar((timeVermelho.equals(dono) ? ChatColor.RED : ChatColor.BLUE)
                + "Esse baú é do time " + dono + ".");
    }

    /**
     * O time dono de um castelo, se o bloco estiver dentro de um.
     *
     * Vem do lugar, e não de quem pôs o baú: os baús dos castelos foram postos
     * pelo Julio, que é de um time só, e pelo registro de quem coloca os dois
     * castelos teriam o mesmo dono. Dentro do castelo vermelho o baú é do
     * vermelho, tenha sido posto por quem for.
     */
    private String donoPeloCastelo(Block bloco) {
        for (String linha : plugin.getConfig().getStringList("areas-construcao")) {
            String[] p = linha.split(",");
            if (p.length < 7) {
                continue;
            }
            int minx = Math.min(Integer.parseInt(p[0].trim()), Integer.parseInt(p[3].trim()));
            int maxx = Math.max(Integer.parseInt(p[0].trim()), Integer.parseInt(p[3].trim()));
            int miny = Math.min(Integer.parseInt(p[1].trim()), Integer.parseInt(p[4].trim()));
            int maxy = Math.max(Integer.parseInt(p[1].trim()), Integer.parseInt(p[4].trim()));
            int minz = Math.min(Integer.parseInt(p[2].trim()), Integer.parseInt(p[5].trim()));
            int maxz = Math.max(Integer.parseInt(p[2].trim()), Integer.parseInt(p[5].trim()));
            if (bloco.getX() < minx || bloco.getX() > maxx
                    || bloco.getY() < miny || bloco.getY() > maxy
                    || bloco.getZ() < minz || bloco.getZ() > maxz) {
                continue;
            }
            return p[6].trim().equalsIgnoreCase("vermelho")
                    ? timeVermelho
                    : plugin.getConfig().getString("time-azul", "Azul");
        }
        return null;
    }

    /** O dono do baú, contando a outra metade se ele for duplo. */
    private String donoDe(Block bloco) {
        String doCastelo = donoPeloCastelo(bloco);
        if (doCastelo != null) {
            return doCastelo;
        }
        String dono = donos.getString(chave(bloco));
        if (dono != null) {
            return dono;
        }
        if (!(bloco.getState() instanceof Chest bau)
                || !(bau.getInventory() instanceof DoubleChestInventory duplo)) {
            return null;
        }
        Block esquerda = ((Chest) duplo.getLeftSide().getHolder()).getBlock();
        Block direita = ((Chest) duplo.getRightSide().getHolder()).getBlock();
        String daEsquerda = donos.getString(chave(esquerda));
        return daEsquerda != null ? daEsquerda : donos.getString(chave(direita));
    }

    private void salvar() {
        try {
            donos.save(arquivo);
        } catch (IOException erro) {
            plugin.getLogger().warning("Não consegui guardar os donos dos baús: " + erro.getMessage());
        }
    }
}
