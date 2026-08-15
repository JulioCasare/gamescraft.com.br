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
 * A loja abre numa capa com as categorias, e cada uma leva a uma pagina. Tudo
 * numa tela so passava de trinta itens e virava um paredao: com as ferramentas
 * completas nos tres materiais nao cabia mais escolher rapido, que e o que se
 * faz no meio de uma partida.
 *
 * Os precos seguem a renda: um diamante por torre a cada trinta segundos.
 * Ferramenta de pedra e troco, a de ferro sai em pouco mais de um minuto com
 * tres torres, e a de diamante custa uma partida inteira de vantagem. Picareta
 * e sempre a mais barata do seu material — sem ela ninguem chega no bloco do
 * inimigo e a partida nao acaba.
 */
final class Loja implements Listener {

    /** Onde fica o botao de voltar, em toda pagina: meio da ultima fileira. */
    private static final int VOLTAR = 31;

    private record Oferta(int slot, Material tipo, int quantidade, int preco, String nome, String razao) {
    }

    private record Categoria(int slot, String chave, Material icone, String nome, String razao,
            List<Oferta> ofertas) {
    }

    /** A pagina aberta. Nulo na capa, onde so se escolhe categoria. */
    private static final class DonoDaLoja implements InventoryHolder {
        private final Categoria pagina;

        private DonoDaLoja(Categoria pagina) {
            this.pagina = pagina;
        }

        @Override
        public Inventory getInventory() {
            return null;
        }
    }

    private final JavaPlugin plugin;
    private final Map<String, Categoria> categorias = new LinkedHashMap<>();

    Loja(JavaPlugin plugin) {
        this.plugin = plugin;

        // Ferramentas: os tres materiais completos, um por fileira. Pedra em
        // cima, diamante embaixo — le-se de cima para baixo como quem sobe de
        // vida.
        por(new Categoria(10, "ferramentas", Material.IRON_PICKAXE, "Ferramentas",
                "Espada, machado, picareta e pa nos tres materiais", List.of(
                        new Oferta(0, Material.STONE_SWORD, 1, 2, "Espada de pedra", "O primeiro passo depois da de madeira"),
                        new Oferta(1, Material.STONE_AXE, 1, 3, "Machado de pedra", "Bate mais que a espada, e demora a repetir"),
                        new Oferta(2, Material.STONE_PICKAXE, 1, 2, "Picareta de pedra", "Ja quebra o bloco do inimigo, devagar"),
                        new Oferta(3, Material.STONE_SHOVEL, 1, 1, "Pa de pedra", "Para abrir caminho pela terra"),

                        new Oferta(9, Material.IRON_SWORD, 1, 7, "Espada de ferro", "Mata de couro em quatro golpes"),
                        new Oferta(10, Material.IRON_AXE, 1, 9, "Machado de ferro", "O maior dano por golpe deste preco"),
                        new Oferta(11, Material.IRON_PICKAXE, 1, 5, "Picareta de ferro", "A compra que faz a partida andar"),
                        new Oferta(12, Material.IRON_SHOVEL, 1, 4, "Pa de ferro", "Terra e areia em um golpe"),

                        new Oferta(18, Material.DIAMOND_SWORD, 1, 22, "Espada de diamante", "Vale umas dez torres de renda"),
                        new Oferta(19, Material.DIAMOND_AXE, 1, 24, "Machado de diamante", "Derruba armadura de ferro em tres"),
                        new Oferta(20, Material.DIAMOND_PICKAXE, 1, 16, "Picareta de diamante", "Quebra obsidiana em tempo util"),
                        new Oferta(21, Material.DIAMOND_SHOVEL, 1, 12, "Pa de diamante", "Para quem ja tem tudo o resto"))));

        // Armadura: so peitoral e calca, que sao as pecas que seguram dano de
        // verdade. Capacete e bota juntos valem menos que a calca sozinha.
        por(new Categoria(12, "armadura", Material.IRON_CHESTPLATE, "Armadura",
                "Peitoral e calca em malha, ferro e diamante", List.of(
                        new Oferta(0, Material.CHAINMAIL_CHESTPLATE, 1, 7, "Peitoral de malha", "O dobro do couro pelo mesmo peso"),
                        new Oferta(1, Material.CHAINMAIL_LEGGINGS, 1, 6, "Calcas de malha", "Barata, e ja muda a conta do golpe"),

                        new Oferta(9, Material.IRON_CHESTPLATE, 1, 15, "Peitoral de ferro", "A peca que mais segura dano"),
                        new Oferta(10, Material.IRON_LEGGINGS, 1, 12, "Calcas de ferro", "A segunda que mais segura"),

                        new Oferta(18, Material.DIAMOND_CHESTPLATE, 1, 32, "Peitoral de diamante", "So compensa com muitas torres"),
                        new Oferta(19, Material.DIAMOND_LEGGINGS, 1, 26, "Calcas de diamante", "Com o peitoral, quase o dobro de vida util"))));

        por(new Categoria(14, "combate", Material.BOW, "Combate",
                "Arco, escudo e o que se guarda para a hora certa", List.of(
                        new Oferta(0, Material.BOW, 1, 7, "Arco", "Para tirar quem esta em cima da torre"),
                        new Oferta(1, Material.ARROW, 16, 2, "16 flechas", "Sem elas o arco nao serve de nada"),
                        new Oferta(2, Material.SHIELD, 1, 5, "Escudo", "Segura flecha e o primeiro golpe"),

                        new Oferta(9, Material.GOLDEN_APPLE, 1, 9, "Maca dourada", "Vida extra na hora de segurar a torre"),
                        new Oferta(10, Material.ENDER_PEARL, 1, 11, "Perola do End", "Entra no castelo inimigo por cima do muro"),
                        new Oferta(11, Material.TNT, 1, 18, "TNT", "Abre a defesa do bloco inimigo de uma vez"))));

        por(new Categoria(16, "blocos", Material.STONE, "Blocos e apoio",
                "Para chegar la, e para nao deixarem chegar", List.of(
                        new Oferta(0, Material.STONE, 32, 2, "32 pedras", "Caminho, ponte e buraco tapado"),
                        new Oferta(1, Material.OBSIDIAN, 4, 14, "4 obsidianas", "Em volta do seu bloco, ganha muito tempo"),
                        new Oferta(2, Material.LADDER, 16, 2, "16 escadas", "Subir na torre sem virar alvo parado"),

                        new Oferta(9, Material.WATER_BUCKET, 1, 6, "Balde de agua", "Apaga fogo e derruba quem esta subindo"),
                        new Oferta(10, Material.COOKED_BEEF, 8, 2, "8 bifes", "Fome vazia nao regenera vida"))));
    }

    private void por(Categoria categoria) {
        categorias.put(categoria.chave(), categoria);
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
        abrirCapa(evento.getPlayer());
    }

    private void abrirCapa(Player jogador) {
        Inventory bau = Bukkit.createInventory(new DonoDaLoja(null), 27,
                ChatColor.DARK_GREEN + "Loja  " + ChatColor.DARK_GRAY + "(paga em diamante)");
        int carteira = diamantes(jogador);
        for (Categoria categoria : categorias.values()) {
            ItemStack icone = new ItemStack(categoria.icone());
            ItemMeta dados = icone.getItemMeta();
            dados.setDisplayName(ChatColor.YELLOW + categoria.nome());
            dados.setLore(List.of(
                    ChatColor.GRAY + categoria.razao(),
                    ChatColor.DARK_GRAY + "" + categoria.ofertas().size() + " itens",
                    ChatColor.GREEN + "Clique para abrir"));
            icone.setItemMeta(dados);
            bau.setItem(categoria.slot(), icone);
        }
        bau.setItem(4, carteiraNaTela(carteira));
        jogador.openInventory(bau);
    }

    private void abrirPagina(Player jogador, Categoria categoria) {
        Inventory bau = Bukkit.createInventory(new DonoDaLoja(categoria), 36,
                ChatColor.DARK_GREEN + "Loja" + ChatColor.DARK_GRAY + " - " + ChatColor.DARK_GREEN + categoria.nome());
        int carteira = diamantes(jogador);
        for (Oferta oferta : categoria.ofertas()) {
            bau.setItem(oferta.slot(), montar(oferta, carteira));
        }
        bau.setItem(27, carteiraNaTela(carteira));
        ItemStack voltar = new ItemStack(Material.ARROW);
        ItemMeta dados = voltar.getItemMeta();
        dados.setDisplayName(ChatColor.YELLOW + "Voltar");
        voltar.setItemMeta(dados);
        bau.setItem(VOLTAR, voltar);
        jogador.openInventory(bau);
    }

    private ItemStack carteiraNaTela(int carteira) {
        ItemStack moeda = new ItemStack(Material.DIAMOND, Math.max(1, Math.min(64, carteira)));
        ItemMeta dados = moeda.getItemMeta();
        dados.setDisplayName(ChatColor.AQUA + "Voce tem " + carteira
                + " diamante" + (carteira == 1 ? "" : "s"));
        dados.setLore(List.of(ChatColor.GRAY + "Cada torre do seu time paga",
                ChatColor.GRAY + "1 diamante a cada 30 segundos"));
        moeda.setItemMeta(dados);
        return moeda;
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
        if (!(evento.getInventory().getHolder() instanceof DonoDaLoja dono)) {
            return;
        }
        // Cancela sempre, inclusive clique no proprio inventario: com a loja
        // aberta nao se arrasta item nenhum, ou daria para tirar a mercadoria.
        evento.setCancelled(true);
        if (!(evento.getWhoClicked() instanceof Player jogador)) {
            return;
        }
        if (dono.pagina == null) {
            for (Categoria categoria : categorias.values()) {
                if (categoria.slot() == evento.getRawSlot()) {
                    abrirPagina(jogador, categoria);
                    return;
                }
            }
            return;
        }
        if (evento.getRawSlot() == VOLTAR) {
            abrirCapa(jogador);
            return;
        }
        for (Oferta oferta : dono.pagina.ofertas()) {
            if (oferta.slot() == evento.getRawSlot()) {
                comprar(jogador, dono.pagina, oferta);
                return;
            }
        }
    }

    private void comprar(Player jogador, Categoria pagina, Oferta oferta) {
        int carteira = diamantes(jogador);
        if (carteira < oferta.preco()) {
            int falta = oferta.preco() - carteira;
            jogador.sendMessage(ChatColor.RED + "Faltam " + falta + " diamante"
                    + (falta > 1 ? "s" : "") + " para " + oferta.nome() + ".");
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
        jogador.getInventory().addItem(new ItemStack(oferta.tipo(), oferta.quantidade()));
        jogador.sendMessage(ChatColor.GREEN + "Comprou " + oferta.nome() + ChatColor.GRAY
                + " por " + oferta.preco() + " diamante" + (oferta.preco() > 1 ? "s" : "") + ".");
        jogador.playSound(jogador.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.4f);
        // Reabre a mesma pagina para as etiquetas mostrarem a carteira nova.
        abrirPagina(jogador, pagina);
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
