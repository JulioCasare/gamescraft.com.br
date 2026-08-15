package br.com.gamescraft.menus;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * A loja da barra de ouro: clique com ela na mão e compre com diamante.
 *
 * O diamante e a moeda porque ja e o que as torres pagam — quem tem mais torre
 * compra mais rapido, e e isso que faz valer a pena sair do castelo e disputar.
 *
 * Os precos seguem a renda: um diamante por torre a cada trinta segundos. Com
 * tres torres, uma espada de ferro sai em pouco mais de um minuto; a de diamante
 * custa uma partida inteira de vantagem. Ferramenta de quebrar bloco e barata de
 * proposito — sem ela ninguem chega no bloco do inimigo e a partida nao acaba.
 */
final class Loja implements Listener {

    private static final String TITULO = ChatColor.DARK_GREEN + "Loja  " + ChatColor.DARK_GRAY + "(paga em diamante)";

    /** Um item a venda: o que sai, quanto custa e o que dizer sobre ele. */
    private record Oferta(int slot, Material tipo, int quantidade, int preco, String nome, String razao) {
    }

    private static final class DonoDaLoja implements InventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }

    private final JavaPlugin plugin;
    private final Map<Integer, Oferta> ofertas = new LinkedHashMap<>();

    Loja(JavaPlugin plugin) {
        this.plugin = plugin;

        // Primeira fileira: o que se usa para brigar pela torre.
        por(new Oferta(0, Material.STONE_SWORD, 1, 2, "Espada de pedra", "O primeiro passo depois da de madeira"));
        por(new Oferta(1, Material.IRON_SWORD, 1, 7, "Espada de ferro", "Mata de couro em quatro golpes"));
        por(new Oferta(2, Material.DIAMOND_SWORD, 1, 22, "Espada de diamante", "Cara: vale umas dez torres de renda"));
        por(new Oferta(3, Material.IRON_AXE, 1, 9, "Machado de ferro", "Bate mais forte, mas demora a repetir"));
        por(new Oferta(4, Material.BOW, 1, 7, "Arco", "Para tirar quem esta em cima da torre"));
        por(new Oferta(5, Material.ARROW, 16, 2, "16 flechas", "Sem elas o arco nao serve de nada"));
        por(new Oferta(6, Material.SHIELD, 1, 5, "Escudo", "Segura flecha e o primeiro golpe"));
        por(new Oferta(7, Material.COOKED_BEEF, 8, 2, "8 bifes", "Fome vazia nao regenera vida"));

        // Segunda fileira: armadura, peca por peca.
        por(new Oferta(9, Material.CHAINMAIL_CHESTPLATE, 1, 7, "Peitoral de malha", "O dobro do couro pelo mesmo peso"));
        por(new Oferta(10, Material.IRON_HELMET, 1, 6, "Capacete de ferro", "Protege da flecha que vem de cima"));
        por(new Oferta(11, Material.IRON_CHESTPLATE, 1, 15, "Peitoral de ferro", "A peca que mais segura dano"));
        por(new Oferta(12, Material.IRON_LEGGINGS, 1, 12, "Calcas de ferro", "A segunda que mais segura"));
        por(new Oferta(13, Material.IRON_BOOTS, 1, 5, "Botas de ferro", "Baratas, e ainda amortecem a queda"));
        por(new Oferta(14, Material.DIAMOND_CHESTPLATE, 1, 32, "Peitoral de diamante", "So compensa com muitas torres"));

        // Terceira fileira: chegar no bloco do inimigo, e defender o seu.
        por(new Oferta(18, Material.IRON_PICKAXE, 1, 5, "Picareta de ferro", "Sem picareta nao se quebra o bloco inimigo"));
        por(new Oferta(19, Material.DIAMOND_PICKAXE, 1, 16, "Picareta de diamante", "Quebra em um terco do tempo"));
        por(new Oferta(20, Material.STONE, 32, 2, "32 pedras", "Para fazer caminho e tapar buraco"));
        por(new Oferta(21, Material.OBSIDIAN, 4, 14, "4 obsidianas", "Em volta do seu bloco, ganha muito tempo"));
        por(new Oferta(22, Material.LADDER, 16, 2, "16 escadas", "Subir na torre sem virar alvo parado"));

        // Quarta fileira: o que se guarda para a hora certa.
        por(new Oferta(27, Material.GOLDEN_APPLE, 1, 9, "Maca dourada", "Vida extra na hora de segurar a torre"));
        por(new Oferta(28, Material.ENDER_PEARL, 1, 11, "Perola do End", "Entra no castelo inimigo por cima do muro"));
        por(new Oferta(29, Material.TNT, 1, 18, "TNT", "Abre a defesa do bloco inimigo de uma vez"));
        por(new Oferta(30, Material.WATER_BUCKET, 1, 6, "Balde de agua", "Apaga fogo e derruba quem esta subindo"));
    }

    private void por(Oferta oferta) {
        ofertas.put(oferta.slot(), oferta);
    }

    /** Clique com a barra de ouro na mao abre a loja. */
    @EventHandler
    public void aoClicarComOuro(PlayerInteractEvent evento) {
        if (evento.getAction() != Action.RIGHT_CLICK_AIR && evento.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        ItemStack naMao = evento.getItem();
        if (naMao == null || naMao.getType() != Material.GOLD_INGOT) {
            return;
        }
        evento.setCancelled(true);
        abrir(evento.getPlayer());
    }

    private void abrir(Player jogador) {
        Inventory bau = Bukkit.createInventory(new DonoDaLoja(), 36, TITULO);
        int carteira = diamantes(jogador);
        for (Oferta oferta : ofertas.values()) {
            bau.setItem(oferta.slot(), montar(oferta, carteira));
        }
        jogador.openInventory(bau);
    }

    /**
     * A etiqueta do item mostra o preco e se da para pagar agora.
     *
     * Dizer "faltam N" e melhor que so pintar de vermelho: o jogador sabe quanto
     * tempo de torre ainda precisa segurar.
     */
    private ItemStack montar(Oferta oferta, int carteira) {
        ItemStack item = new ItemStack(oferta.tipo(), oferta.quantidade());
        ItemMeta dados = item.getItemMeta();
        dados.setDisplayName(ChatColor.YELLOW + oferta.nome());
        List<String> linhas = new ArrayList<>();
        linhas.add(ChatColor.AQUA + "" + oferta.preco() + " diamante" + (oferta.preco() > 1 ? "s" : ""));
        linhas.add(ChatColor.GRAY + oferta.razao());
        if (carteira >= oferta.preco()) {
            linhas.add(ChatColor.GREEN + "Clique para comprar");
        } else {
            linhas.add(ChatColor.RED + "Faltam " + (oferta.preco() - carteira));
        }
        dados.setLore(linhas);
        item.setItemMeta(dados);
        return item;
    }

    @EventHandler
    public void aoClicarNaLoja(InventoryClickEvent evento) {
        if (!(evento.getInventory().getHolder() instanceof DonoDaLoja)) {
            return;
        }
        // Cancela sempre, inclusive clique no proprio inventario: com a loja
        // aberta nao se arrasta item nenhum, ou daria para tirar a mercadoria.
        evento.setCancelled(true);
        if (!(evento.getWhoClicked() instanceof Player jogador)) {
            return;
        }
        Oferta oferta = ofertas.get(evento.getRawSlot());
        if (oferta == null) {
            return;
        }
        int carteira = diamantes(jogador);
        if (carteira < oferta.preco()) {
            jogador.sendMessage(ChatColor.RED + "Faltam " + (oferta.preco() - carteira)
                    + " diamante" + (oferta.preco() - carteira > 1 ? "s" : "") + " para " + oferta.nome() + ".");
            jogador.playSound(jogador.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            return;
        }
        // Espaco antes de cobrar: pagar e ver o item cair no chao seria pior que
        // nao comprar.
        if (jogador.getInventory().firstEmpty() == -1) {
            jogador.sendMessage(ChatColor.RED + "Inventario cheio.");
            jogador.playSound(jogador.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            return;
        }
        cobrar(jogador, oferta.preco());
        ItemStack comprado = new ItemStack(oferta.tipo(), oferta.quantidade());
        jogador.getInventory().addItem(comprado);
        jogador.sendMessage(ChatColor.GREEN + "Comprou " + oferta.nome() + ChatColor.GRAY
                + " por " + oferta.preco() + " diamante" + (oferta.preco() > 1 ? "s" : "") + ".");
        jogador.playSound(jogador.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.4f);
        // Reabre para as etiquetas mostrarem a carteira nova.
        abrir(jogador);
    }

    private int diamantes(Player jogador) {
        int total = 0;
        for (ItemStack item : jogador.getInventory().getStorageContents()) {
            if (item != null && item.getType() == Material.DIAMOND) {
                total += item.getAmount();
            }
        }
        return total;
    }

    private void cobrar(Player jogador, int preco) {
        int falta = preco;
        ItemStack[] conteudo = jogador.getInventory().getStorageContents();
        for (int i = 0; i < conteudo.length && falta > 0; i++) {
            ItemStack item = conteudo[i];
            if (item == null || item.getType() != Material.DIAMOND) {
                continue;
            }
            int tira = Math.min(falta, item.getAmount());
            item.setAmount(item.getAmount() - tira);
            falta -= tira;
            if (item.getAmount() <= 0) {
                conteudo[i] = null;
            }
        }
        jogador.getInventory().setStorageContents(conteudo);
        if (falta > 0) {
            plugin.getLogger().warning("Cobranca de " + preco + " diamantes ficou incompleta para "
                    + jogador.getName() + ": faltaram " + falta + ".");
        }
    }
}
