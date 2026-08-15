package br.com.gamescraft.menus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Entrar numa arena do Bed Wars com o clique dado lá no lobby.
 *
 * Em multiarena o BedWars1058 deixa quem chega parado no saguão, esperando um
 * /bw join — e o jogador que já escolheu "Solo" no boneco não entende por que
 * precisa escolher de novo.
 *
 * O lobby manda a escolha pelo proxy antes de mandar o jogador. Quando ela
 * chega, este servidor entra na arena sozinho. Quando não chega — e ela não
 * chega se o Bed Wars estiver vazio, porque o proxy entrega essas mensagens
 * pela conexão de quem já está lá — abre-se um menu com as arenas, que é um
 * clique em vez de um comando decorado.
 */
final class EntradaBedwars implements Listener {

    /** Quanto tempo a escolha vale. Depois disso ela era de outra visita. */
    private static final long VALIDADE = 30_000L;

    private record Escolha(String arena, long quando) {
    }

    private record Arena(int slot, String nome, String rotulo, Material icone, String razao) {
    }

    private static final List<Arena> ARENAS = List.of(
            new Arena(11, "solo1", "Solo", Material.RED_BED, "Um contra um contra todos"),
            new Arena(15, "duplas1", "Duplas", Material.BLUE_BED, "De dois em dois"));

    private static final class DonoDoMenu implements InventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }

    private final JavaPlugin plugin;
    private final Map<String, Escolha> escolhas = new HashMap<>();

    EntradaBedwars(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /** Chamado quando a escolha feita no lobby chega pelo proxy. */
    void anotar(String jogador, String arena) {
        escolhas.put(jogador.toLowerCase(), new Escolha(arena, System.currentTimeMillis()));
    }

    @EventHandler
    public void aoEntrar(PlayerJoinEvent evento) {
        Player jogador = evento.getPlayer();
        Escolha escolha = escolhas.remove(jogador.getName().toLowerCase());
        boolean vale = escolha != null && System.currentTimeMillis() - escolha.quando() < VALIDADE;

        // Um segundo de espera: o BedWars1058 ainda está pondo o jogador no
        // saguão quando este evento corre, e mandar o join antes disso não pega.
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (!jogador.isOnline()) {
                return;
            }
            if (vale) {
                Bukkit.dispatchCommand(jogador, "bw join " + escolha.arena());
            } else {
                abrir(jogador);
            }
        }, 20L);
    }

    private void abrir(Player jogador) {
        Inventory bau = Bukkit.createInventory(new DonoDoMenu(), 27,
                ChatColor.AQUA + "Bed Wars — escolha a arena");
        for (Arena arena : ARENAS) {
            ItemStack item = new ItemStack(arena.icone());
            ItemMeta dados = item.getItemMeta();
            dados.setDisplayName(ChatColor.AQUA + arena.rotulo());
            dados.setLore(List.of(ChatColor.GRAY + arena.razao(),
                    ChatColor.GREEN + "Clique para entrar"));
            item.setItemMeta(dados);
            bau.setItem(arena.slot(), item);
        }
        jogador.openInventory(bau);
    }

    @EventHandler
    public void aoClicar(InventoryClickEvent evento) {
        if (!(evento.getInventory().getHolder() instanceof DonoDoMenu)) {
            return;
        }
        evento.setCancelled(true);
        if (!(evento.getWhoClicked() instanceof Player jogador)) {
            return;
        }
        for (Arena arena : ARENAS) {
            if (arena.slot() == evento.getRawSlot()) {
                jogador.closeInventory();
                Bukkit.dispatchCommand(jogador, "bw join " + arena.nome());
                return;
            }
        }
    }
}
