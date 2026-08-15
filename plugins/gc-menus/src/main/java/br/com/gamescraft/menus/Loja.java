package br.com.gamescraft.menus;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Team;

/**
 * A loja da barra de ouro, no formato do Bed Wars: uma fileira de abas em cima,
 * a compra rápida na primeira, e o resto separado por categoria.
 *
 * O diamante é a moeda porque já é o que as torres pagam — quem tem mais torre
 * compra mais rápido, e é isso que faz valer a pena sair do castelo.
 *
 * Três coisas mudam com o time de quem abre: a lã, que sai na cor do time; a
 * armadura, que vem com enfeite de redstone ou lápis-lazúli, os mesmos blocos
 * que cada time defende; e nada mais. O resto é igual para os dois lados.
 */
final class Loja implements Listener {

    /** Onde os itens de cada página cabem: três fileiras de sete, sem as bordas. */
    private static final int[] FILEIRA_1 = { 19, 20, 21, 22, 23, 24, 25 };
    private static final int[] FILEIRA_2 = { 28, 29, 30, 31, 32, 33, 34 };
    private static final int[] FILEIRA_3 = { 37, 38, 39, 40, 41, 42, 43 };

    /** O que um item faz de especial além de existir no inventário. */
    private enum Truque {
        NENHUM, LA_DO_TIME, VESTIR, BOLA_DE_FOGO, TNT_AUTOMATICA, TORRE
    }

    private record Oferta(int slot, Material tipo, int quantidade, int preco, String nome,
            String razao, Truque truque) {

        Oferta(int slot, Material tipo, int quantidade, int preco, String nome, String razao) {
            this(slot, tipo, quantidade, preco, nome, razao, Truque.NENHUM);
        }
    }

    private record Aba(int slot, String chave, Material icone, String nome, List<Oferta> ofertas) {
    }

    private static final class DonoDaLoja implements InventoryHolder {
        private final Aba aba;

        private DonoDaLoja(Aba aba) {
            this.aba = aba;
        }

        @Override
        public Inventory getInventory() {
            return null;
        }
    }

    private final JavaPlugin plugin;
    private final Torres torres;
    private final Areas areas;
    private final Armaduras armaduras;
    private final String timeVermelho;
    private final Map<String, Aba> abas = new LinkedHashMap<>();
    private final NamespacedKey marca;

    Loja(JavaPlugin plugin, Torres torres, Areas areas, Armaduras armaduras) {
        this.plugin = plugin;
        this.torres = torres;
        this.areas = areas;
        this.armaduras = armaduras;
        this.timeVermelho = plugin.getConfig().getString("time-vermelho", "Vermelho");
        this.marca = new NamespacedKey(plugin, "especial");

        aba(new Aba(0, "rapida", Material.NETHER_STAR, "Compra rapida", List.of(
                new Oferta(FILEIRA_1[0], Material.WHITE_WOOL, 16, 1, "16 las do time", "O bloco de sempre: barato e rapido", Truque.LA_DO_TIME),
                new Oferta(FILEIRA_1[1], Material.STONE_SWORD, 1, 2, "Espada de pedra", "O primeiro passo depois da de madeira"),
                new Oferta(FILEIRA_1[2], Material.CHAINMAIL_BOOTS, 1, 3, "Botas de malha", "A armadura mais barata que ja ajuda", Truque.VESTIR),
                new Oferta(FILEIRA_1[3], Material.IRON_PICKAXE, 1, 5, "Picareta de ferro", "A compra que faz a partida andar"),
                new Oferta(FILEIRA_1[4], Material.BOW, 1, 7, "Arco", "Para tirar quem esta em cima da torre"),
                new Oferta(FILEIRA_1[5], Material.ARROW, 16, 2, "16 flechas", "Sem elas o arco nao serve de nada"),
                new Oferta(FILEIRA_1[6], Material.GOLDEN_APPLE, 1, 8, "Maca dourada", "Vida extra na hora de segurar a torre"),

                new Oferta(FILEIRA_2[0], Material.FIRE_CHARGE, 1, 8, "Bola de fogo", "Clique para atirar: derruba ponte e quem esta nela", Truque.BOLA_DE_FOGO),
                new Oferta(FILEIRA_2[1], Material.TNT, 1, 10, "TNT automatica", "Acende sozinha ao ser posta", Truque.TNT_AUTOMATICA),
                new Oferta(FILEIRA_2[2], Material.ENDER_PEARL, 1, 12, "Perola do End", "Entra no castelo inimigo por cima do muro"),
                new Oferta(FILEIRA_2[3], Material.WATER_BUCKET, 1, 5, "Balde de agua", "Apaga fogo e derruba quem esta subindo"),
                new Oferta(FILEIRA_2[4], Material.CHEST, 1, 20, "Torre compacta", "Levanta uma torre pronta, com escada e plataforma", Truque.TORRE),
                new Oferta(FILEIRA_2[5], Material.COOKED_BEEF, 8, 2, "8 bifes", "Fome vazia nao regenera vida"),
                new Oferta(FILEIRA_2[6], Material.OBSIDIAN, 4, 12, "4 obsidianas", "Em volta do seu bloco, ganha muito tempo"))));

        aba(new Aba(1, "blocos", Material.TERRACOTTA, "Blocos", List.of(
                new Oferta(FILEIRA_1[0], Material.WHITE_WOOL, 16, 1, "16 las do time", "Barata e rapida de por, mas queima", Truque.LA_DO_TIME),
                new Oferta(FILEIRA_1[1], Material.OAK_PLANKS, 16, 3, "16 madeiras", "Aguenta mais golpe que a la, e nao pega fogo de longe"),
                new Oferta(FILEIRA_1[2], Material.END_STONE, 12, 4, "12 pedras do fim", "Dura de quebrar: para o que tem de ficar de pe"),
                new Oferta(FILEIRA_1[3], Material.OBSIDIAN, 4, 12, "4 obsidianas", "Em volta do seu bloco, ganha muito tempo"),
                new Oferta(FILEIRA_1[4], Material.LADDER, 16, 2, "16 escadas", "Subir sem virar alvo parado"))));

        aba(new Aba(2, "armas", Material.IRON_SWORD, "Armas", List.of(
                new Oferta(FILEIRA_1[0], Material.STONE_SWORD, 1, 2, "Espada de pedra", "O primeiro passo depois da de madeira"),
                new Oferta(FILEIRA_1[1], Material.IRON_SWORD, 1, 7, "Espada de ferro", "Mata de couro em quatro golpes"),
                new Oferta(FILEIRA_1[2], Material.DIAMOND_SWORD, 1, 20, "Espada de diamante", "Vale umas dez torres de renda"),

                new Oferta(FILEIRA_2[0], Material.BOW, 1, 7, "Arco", "Para tirar quem esta em cima da torre"),
                new Oferta(FILEIRA_2[1], Material.ARROW, 16, 2, "16 flechas", "Sem elas o arco nao serve de nada"),
                new Oferta(FILEIRA_2[2], Material.BOW, 1, 14, "Arco Forca I", "Mais dano por flecha, para segurar de longe"))));

        // Armadura por fileira de material, e ela veste na hora: quem compra
        // peitoral quer estar com ele agora, nao guardado na mochila.
        aba(new Aba(3, "armadura", Material.IRON_CHESTPLATE, "Armadura", List.of(
                new Oferta(FILEIRA_1[0], Material.CHAINMAIL_HELMET, 1, 3, "Capacete de malha", "Enfeite na cor do time", Truque.VESTIR),
                new Oferta(FILEIRA_1[1], Material.CHAINMAIL_CHESTPLATE, 1, 7, "Peitoral de malha", "Enfeite na cor do time", Truque.VESTIR),
                new Oferta(FILEIRA_1[2], Material.CHAINMAIL_LEGGINGS, 1, 6, "Calcas de malha", "Enfeite na cor do time", Truque.VESTIR),
                new Oferta(FILEIRA_1[3], Material.CHAINMAIL_BOOTS, 1, 3, "Botas de malha", "Enfeite na cor do time", Truque.VESTIR),

                new Oferta(FILEIRA_2[0], Material.IRON_HELMET, 1, 6, "Capacete de ferro", "Enfeite na cor do time", Truque.VESTIR),
                new Oferta(FILEIRA_2[1], Material.IRON_CHESTPLATE, 1, 15, "Peitoral de ferro", "Enfeite na cor do time", Truque.VESTIR),
                new Oferta(FILEIRA_2[2], Material.IRON_LEGGINGS, 1, 12, "Calcas de ferro", "Enfeite na cor do time", Truque.VESTIR),
                new Oferta(FILEIRA_2[3], Material.IRON_BOOTS, 1, 5, "Botas de ferro", "Enfeite na cor do time", Truque.VESTIR),

                new Oferta(FILEIRA_3[0], Material.DIAMOND_CHESTPLATE, 1, 30, "Peitoral de diamante", "Enfeite na cor do time", Truque.VESTIR),
                new Oferta(FILEIRA_3[1], Material.DIAMOND_LEGGINGS, 1, 25, "Calcas de diamante", "Enfeite na cor do time", Truque.VESTIR))));

        aba(new Aba(4, "ferramentas", Material.IRON_PICKAXE, "Ferramentas", List.of(
                new Oferta(FILEIRA_1[0], Material.STONE_PICKAXE, 1, 2, "Picareta de pedra", "Ja quebra o bloco do inimigo, devagar"),
                new Oferta(FILEIRA_1[1], Material.IRON_PICKAXE, 1, 5, "Picareta de ferro", "A compra que faz a partida andar"),
                new Oferta(FILEIRA_1[2], Material.GOLDEN_PICKAXE, 1, 4, "Picareta de ouro", "Rapidissima, e some rapido tambem"),
                new Oferta(FILEIRA_1[3], Material.DIAMOND_PICKAXE, 1, 15, "Picareta de diamante", "Quebra obsidiana em tempo util"),

                new Oferta(FILEIRA_2[0], Material.STONE_AXE, 1, 3, "Machado de pedra", "Contra madeira, e contra gente"),
                new Oferta(FILEIRA_2[1], Material.IRON_AXE, 1, 8, "Machado de ferro", "O maior dano por golpe deste preco"),
                new Oferta(FILEIRA_2[2], Material.GOLDEN_AXE, 1, 6, "Machado de ouro", "Rapido, e dura pouco"),
                new Oferta(FILEIRA_2[3], Material.DIAMOND_AXE, 1, 18, "Machado de diamante", "Derruba armadura de ferro em tres"),

                new Oferta(FILEIRA_3[0], Material.SHEARS, 1, 3, "Tesoura", "Corta la de defesa em um golpe"))));

        // Trinta segundos em todas: e o tempo de uma investida, nao de uma
        // partida. Poção longa faria o time forte comprar e nunca mais soltar.
        aba(new Aba(5, "pocoes", Material.BREWING_STAND, "Pocoes", List.of(
                new Oferta(FILEIRA_1[0], Material.POTION, 1, 8, "Forca II (30s)", "O dobro de dano enquanto durar"),
                new Oferta(FILEIRA_1[1], Material.POTION, 1, 6, "Agilidade II (30s)", "Para chegar antes na torre neutra"),
                new Oferta(FILEIRA_1[2], Material.POTION, 1, 12, "Invisibilidade (30s)", "Entra no castelo sem ser visto"))));

        aba(new Aba(6, "comida", Material.COOKED_BEEF, "Comida", List.of(
                new Oferta(FILEIRA_1[0], Material.GOLDEN_APPLE, 1, 8, "Maca dourada", "Vida extra na hora de segurar a torre"),
                new Oferta(FILEIRA_1[1], Material.COOKED_BEEF, 8, 2, "8 bifes", "Fome vazia nao regenera vida"),
                new Oferta(FILEIRA_1[2], Material.CARROT, 8, 1, "8 cenouras", "Barata, para nao voltar ao castelo so por fome"))));

        aba(new Aba(7, "especiais", Material.TNT, "Especiais", List.of(
                new Oferta(FILEIRA_1[0], Material.FIRE_CHARGE, 1, 8, "Bola de fogo", "Clique para atirar: derruba ponte e quem esta nela", Truque.BOLA_DE_FOGO),
                new Oferta(FILEIRA_1[1], Material.TNT, 1, 10, "TNT automatica", "Acende sozinha ao ser posta", Truque.TNT_AUTOMATICA),
                new Oferta(FILEIRA_1[2], Material.ENDER_PEARL, 1, 12, "Perola do End", "Entra no castelo inimigo por cima do muro"),
                new Oferta(FILEIRA_1[3], Material.IRON_GOLEM_SPAWN_EGG, 1, 25, "Ovo de golem de ferro", "Guarda o castelo enquanto voce ataca"),

                new Oferta(FILEIRA_2[0], Material.WATER_BUCKET, 1, 5, "Balde de agua", "Apaga fogo e derruba quem esta subindo"),
                new Oferta(FILEIRA_2[1], Material.LAVA_BUCKET, 1, 10, "Balde de lava", "Fecha um caminho estreito sozinho"),
                new Oferta(FILEIRA_2[2], Material.CHEST, 1, 20, "Torre compacta", "Levanta uma torre pronta, com escada e plataforma", Truque.TORRE))));
    }

    private void aba(Aba aba) {
        abas.put(aba.chave(), aba);
    }

    // ---------------------------------------------------------------- abrir

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
        abrir(evento.getPlayer(), abas.get("rapida"));
    }

    private void abrir(Player jogador, Aba aba) {
        Inventory bau = Bukkit.createInventory(new DonoDaLoja(aba), 54,
                ChatColor.DARK_GRAY + aba.nome());
        int carteira = diamantes(jogador);
        boolean vermelho = ehVermelho(jogador);

        for (Aba outra : abas.values()) {
            bau.setItem(outra.slot(), iconeDaAba(outra, outra == aba));
            // A fileira de baixo marca a aba aberta, como no Bed Wars.
            bau.setItem(9 + outra.slot(), outra == aba
                    ? vidro(Material.LIME_STAINED_GLASS_PANE, ChatColor.GREEN + outra.nome())
                    : vidro(Material.GRAY_STAINED_GLASS_PANE, " "));
        }
        bau.setItem(8, carteiraNaTela(carteira));

        for (Oferta oferta : aba.ofertas()) {
            bau.setItem(oferta.slot(), etiquetar(montar(oferta, vermelho), oferta, carteira));
        }
        jogador.openInventory(bau);
    }

    private ItemStack iconeDaAba(Aba aba, boolean aberta) {
        ItemStack icone = new ItemStack(aba.icone());
        ItemMeta dados = icone.getItemMeta();
        dados.setDisplayName((aberta ? ChatColor.GREEN : ChatColor.YELLOW) + aba.nome());
        dados.setLore(List.of(aberta
                ? ChatColor.GRAY + "Voce esta aqui"
                : ChatColor.GRAY + "Clique para abrir"));
        icone.setItemMeta(dados);
        return icone;
    }

    private ItemStack vidro(Material tipo, String nome) {
        ItemStack vidro = new ItemStack(tipo);
        ItemMeta dados = vidro.getItemMeta();
        dados.setDisplayName(nome);
        vidro.setItemMeta(dados);
        return vidro;
    }

    private ItemStack carteiraNaTela(int carteira) {
        ItemStack moeda = new ItemStack(Material.DIAMOND, Math.max(1, Math.min(64, carteira)));
        ItemMeta dados = moeda.getItemMeta();
        dados.setDisplayName(ChatColor.AQUA + "Voce tem " + carteira + " diamante" + (carteira == 1 ? "" : "s"));
        dados.setLore(List.of(ChatColor.GRAY + "Cada torre do seu time paga",
                ChatColor.GRAY + "1 diamante a cada 30 segundos"));
        moeda.setItemMeta(dados);
        return moeda;
    }

    // ----------------------------------------------------------- os itens

    /** O item como ele sai da loja, ja com a cor e os poderes do time de quem compra. */
    private ItemStack montar(Oferta oferta, boolean vermelho) {
        ItemStack item = switch (oferta.truque()) {
            case LA_DO_TIME -> new ItemStack(vermelho ? Material.RED_WOOL : Material.BLUE_WOOL,
                    oferta.quantidade());
            default -> new ItemStack(oferta.tipo(), oferta.quantidade());
        };

        if (oferta.truque() == Truque.VESTIR) {
            enfeitar(item, vermelho);
        }
        if (oferta.nome().startsWith("Arco Forca")) {
            item.addUnsafeEnchantment(Enchantment.POWER, 1);
        }
        if (item.getType() == Material.POTION) {
            encher(item, oferta.nome());
        }

        ItemMeta dados = item.getItemMeta();
        dados.setDisplayName(ChatColor.YELLOW + oferta.nome());
        if (oferta.truque() == Truque.BOLA_DE_FOGO || oferta.truque() == Truque.TNT_AUTOMATICA
                || oferta.truque() == Truque.TORRE) {
            dados.getPersistentDataContainer().set(marca, PersistentDataType.STRING,
                    oferta.truque().name());
        }
        item.setItemMeta(dados);
        return item;
    }

    /**
     * Enfeite da cor do time na armadura.
     *
     * Redstone para o vermelho e lápis para o azul — os mesmos blocos que cada
     * time defende, então a cor da armadura diz de qual lado a pessoa é sem
     * precisar de plaquinha.
     *
     * Se a versão do servidor tiver mexido no registro dos enfeites, a armadura
     * sai lisa em vez de a compra falhar: cor é enfeite, defesa é o que importa.
     */
    private void enfeitar(ItemStack peca, boolean vermelho) {
        try {
            if (!(peca.getItemMeta() instanceof ArmorMeta dados)) {
                return;
            }
            TrimMaterial material = Registry.TRIM_MATERIAL.get(
                    NamespacedKey.minecraft(vermelho ? "redstone" : "lapis"));
            TrimPattern padrao = Registry.TRIM_PATTERN.get(NamespacedKey.minecraft("sentry"));
            if (material == null || padrao == null) {
                return;
            }
            dados.setTrim(new ArmorTrim(material, padrao));
            peca.setItemMeta(dados);
        } catch (Throwable erro) {
            plugin.getLogger().warning("Nao consegui por o enfeite do time na armadura: " + erro);
        }
    }

    /** As três poções, todas de trinta segundos. */
    private void encher(ItemStack garrafa, String nome) {
        if (!(garrafa.getItemMeta() instanceof PotionMeta dados)) {
            return;
        }
        if (nome.startsWith("Forca")) {
            dados.addCustomEffect(new PotionEffect(PotionEffectType.STRENGTH, 30 * 20, 1), true);
            dados.setColor(Color.fromRGB(0x93, 0x24, 0x23));
        } else if (nome.startsWith("Agilidade")) {
            dados.addCustomEffect(new PotionEffect(PotionEffectType.SPEED, 30 * 20, 1), true);
            dados.setColor(Color.fromRGB(0x7C, 0xAF, 0xC6));
        } else {
            dados.addCustomEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 30 * 20, 0), true);
            dados.setColor(Color.fromRGB(0xF6, 0xF6, 0xF6));
        }
        garrafa.setItemMeta(dados);
    }

    private ItemStack etiquetar(ItemStack item, Oferta oferta, int carteira) {
        ItemMeta dados = item.getItemMeta();
        List<String> linhas = new ArrayList<>();
        linhas.add(ChatColor.AQUA + "" + oferta.preco() + " diamante" + (oferta.preco() > 1 ? "s" : ""));
        linhas.add(ChatColor.GRAY + oferta.razao());
        if (oferta.truque() == Truque.VESTIR) {
            linhas.add(ChatColor.DARK_GRAY + "Veste na hora, no lugar do couro");
        }
        linhas.add(carteira >= oferta.preco()
                ? ChatColor.GREEN + "Clique para comprar"
                : ChatColor.RED + "Faltam " + (oferta.preco() - carteira));
        dados.setLore(linhas);
        item.setItemMeta(dados);
        return item;
    }

    // ---------------------------------------------------------- a compra

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
        for (Aba aba : abas.values()) {
            if (aba.slot() == evento.getRawSlot()) {
                abrir(jogador, aba);
                return;
            }
        }
        for (Oferta oferta : dono.aba.ofertas()) {
            if (oferta.slot() == evento.getRawSlot()) {
                comprar(jogador, dono.aba, oferta);
                return;
            }
        }
    }

    private void comprar(Player jogador, Aba aba, Oferta oferta) {
        int carteira = diamantes(jogador);
        if (carteira < oferta.preco()) {
            int falta = oferta.preco() - carteira;
            jogador.sendMessage(ChatColor.RED + "Faltam " + falta + " diamante"
                    + (falta > 1 ? "s" : "") + " para " + oferta.nome() + ".");
            jogador.playSound(jogador.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            return;
        }
        boolean vestir = oferta.truque() == Truque.VESTIR;
        // Espaco antes de cobrar: pagar e ver o item cair no chao seria pior que
        // nao comprar. Armadura nao precisa de espaco — ela vai para o corpo.
        if (!vestir && jogador.getInventory().firstEmpty() == -1) {
            jogador.sendMessage(ChatColor.RED + "Inventario cheio.");
            jogador.playSound(jogador.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            return;
        }
        cobrar(jogador, oferta.preco());
        ItemStack comprado = montar(oferta, ehVermelho(jogador));
        if (vestir) {
            vestir(jogador, comprado);
        } else {
            jogador.getInventory().addItem(comprado);
        }
        jogador.sendMessage(ChatColor.GREEN + "Comprou " + oferta.nome() + ChatColor.GRAY
                + " por " + oferta.preco() + " diamante" + (oferta.preco() > 1 ? "s" : "") + ".");
        jogador.playSound(jogador.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.4f);
        abrir(jogador, aba);
    }

    /**
     * Armadura vai direto para o corpo, no lugar do couro do kit — e fica
     * anotada, para voltar depois de cada morte.
     */
    private void vestir(Player jogador, ItemStack peca) {
        armaduras.guardar(jogador, peca);
        String tipo = peca.getType().name();
        if (tipo.endsWith("_HELMET")) {
            jogador.getInventory().setHelmet(peca);
        } else if (tipo.endsWith("_CHESTPLATE")) {
            jogador.getInventory().setChestplate(peca);
        } else if (tipo.endsWith("_LEGGINGS")) {
            jogador.getInventory().setLeggings(peca);
        } else if (tipo.endsWith("_BOOTS")) {
            jogador.getInventory().setBoots(peca);
        }
    }

    private boolean ehVermelho(Player jogador) {
        Team time = jogador.getScoreboard().getEntryTeam(jogador.getName());
        return time != null && timeVermelho.equals(time.getName());
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

    NamespacedKey marca() {
        return marca;
    }

    Torres torres() {
        return torres;
    }

    Areas areas() {
        return areas;
    }
}
