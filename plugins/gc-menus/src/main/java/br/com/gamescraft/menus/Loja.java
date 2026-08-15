package br.com.gamescraft.menus;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
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
 * A compra rápida é de cada um: clique com shift em qualquer item para pô-lo
 * ou tirá-lo de lá. Uma loja de oito abas é completa mas lenta, e no meio de
 * uma briga ninguém navega — a compra rápida é a resposta a isso, e só serve
 * se cada um puder montar a sua.
 */
final class Loja implements Listener {

    /** Onde os itens de cada página cabem: três fileiras de sete, sem as bordas. */
    private static final int[] FILEIRA_1 = { 19, 20, 21, 22, 23, 24, 25 };
    private static final int[] FILEIRA_2 = { 28, 29, 30, 31, 32, 33, 34 };
    private static final int[] FILEIRA_3 = { 37, 38, 39, 40, 41, 42, 43 };

    /** O que um item faz de especial além de existir no inventário. */
    private enum Truque {
        NENHUM, LA_DO_TIME, VESTIR, BOLA_DE_FOGO, TNT_AUTOMATICA, TORRE, INVISIBILIDADE
    }

    private record Oferta(String id, int slot, Material tipo, int quantidade, int preco,
            String nome, String razao, Truque truque) {

        Oferta(String id, int slot, Material tipo, int quantidade, int preco, String nome, String razao) {
            this(id, slot, tipo, quantidade, preco, nome, razao, Truque.NENHUM);
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
    private final Ferramentas ferramentas;
    private final String timeVermelho;
    private final Map<String, Aba> abas = new LinkedHashMap<>();
    private final Map<String, Oferta> porId = new LinkedHashMap<>();
    private final NamespacedKey marca;

    /** A compra rápida de cada jogador, por nome de item. */
    private final File arquivoRapida;
    private final YamlConfiguration rapidas;
    private final List<String> rapidaDeFabrica;

    Loja(JavaPlugin plugin, Torres torres, Areas areas, Armaduras armaduras, Ferramentas ferramentas) {
        this.plugin = plugin;
        this.torres = torres;
        this.areas = areas;
        this.armaduras = armaduras;
        this.ferramentas = ferramentas;
        this.timeVermelho = plugin.getConfig().getString("time-vermelho", "Vermelho");
        this.marca = new NamespacedKey(plugin, "especial");
        this.arquivoRapida = new File(plugin.getDataFolder(), "compra-rapida.yml");
        this.rapidas = YamlConfiguration.loadConfiguration(arquivoRapida);

        aba(new Aba(1, "blocos", Material.TERRACOTTA, "Blocos", List.of(
                new Oferta("la", FILEIRA_1[0], Material.WHITE_WOOL, 16, 1, "16 lãs do time", "Barata e rápida de pôr, mas queima", Truque.LA_DO_TIME),
                new Oferta("madeira", FILEIRA_1[1], Material.OAK_PLANKS, 16, 3, "16 madeiras", "Aguenta mais golpe que a lã"),
                new Oferta("pedra_do_fim", FILEIRA_1[2], Material.END_STONE, 12, 4, "12 pedras do fim", "Dura de quebrar: para o que tem de ficar de pé"),
                new Oferta("obsidiana", FILEIRA_1[3], Material.OBSIDIAN, 4, 12, "4 obsidianas", "Em volta do seu bloco, ganha muito tempo"),
                new Oferta("escada", FILEIRA_1[4], Material.LADDER, 16, 2, "16 escadas", "Subir sem virar alvo parado"))));

        aba(new Aba(2, "armas", Material.IRON_SWORD, "Armas", List.of(
                new Oferta("espada_pedra", FILEIRA_1[0], Material.STONE_SWORD, 1, 2, "Espada de pedra", "O primeiro passo depois da de madeira"),
                new Oferta("espada_ferro", FILEIRA_1[1], Material.IRON_SWORD, 1, 7, "Espada de ferro", "Mata de couro em quatro golpes"),
                new Oferta("espada_diamante", FILEIRA_1[2], Material.DIAMOND_SWORD, 1, 10, "Espada de diamante", "A melhor lâmina do jogo"),

                new Oferta("arco", FILEIRA_2[0], Material.BOW, 1, 7, "Arco", "Para tirar quem está em cima da torre"),
                new Oferta("flechas", FILEIRA_2[1], Material.ARROW, 16, 2, "16 flechas", "Sem elas o arco não serve de nada"),
                new Oferta("arco_forca", FILEIRA_2[2], Material.BOW, 1, 14, "Arco Força I", "Mais dano por flecha, para segurar de longe"))));

        aba(new Aba(3, "armadura", Material.IRON_CHESTPLATE, "Armadura", List.of(
                new Oferta("malha_capacete", FILEIRA_1[0], Material.CHAINMAIL_HELMET, 1, 3, "Capacete de malha", "Enfeite na cor do time", Truque.VESTIR),
                new Oferta("malha_peito", FILEIRA_1[1], Material.CHAINMAIL_CHESTPLATE, 1, 7, "Peitoral de malha", "Enfeite na cor do time", Truque.VESTIR),
                new Oferta("malha_calca", FILEIRA_1[2], Material.CHAINMAIL_LEGGINGS, 1, 6, "Calças de malha", "Enfeite na cor do time", Truque.VESTIR),
                new Oferta("malha_bota", FILEIRA_1[3], Material.CHAINMAIL_BOOTS, 1, 3, "Botas de malha", "Enfeite na cor do time", Truque.VESTIR),

                new Oferta("ferro_capacete", FILEIRA_2[0], Material.IRON_HELMET, 1, 6, "Capacete de ferro", "Enfeite na cor do time", Truque.VESTIR),
                new Oferta("ferro_peito", FILEIRA_2[1], Material.IRON_CHESTPLATE, 1, 15, "Peitoral de ferro", "Enfeite na cor do time", Truque.VESTIR),
                new Oferta("ferro_calca", FILEIRA_2[2], Material.IRON_LEGGINGS, 1, 12, "Calças de ferro", "Enfeite na cor do time", Truque.VESTIR),
                new Oferta("ferro_bota", FILEIRA_2[3], Material.IRON_BOOTS, 1, 5, "Botas de ferro", "Enfeite na cor do time", Truque.VESTIR),

                new Oferta("dima_peito", FILEIRA_3[0], Material.DIAMOND_CHESTPLATE, 1, 30, "Peitoral de diamante", "Enfeite na cor do time", Truque.VESTIR),
                new Oferta("dima_calca", FILEIRA_3[1], Material.DIAMOND_LEGGINGS, 1, 25, "Calças de diamante", "Enfeite na cor do time", Truque.VESTIR))));

        aba(new Aba(4, "ferramentas", Material.IRON_PICKAXE, "Ferramentas", List.of(
                new Oferta("picareta_pedra", FILEIRA_1[0], Material.STONE_PICKAXE, 1, 2, "Picareta de pedra", "Já quebra o bloco do inimigo, devagar"),
                new Oferta("picareta_ferro", FILEIRA_1[1], Material.IRON_PICKAXE, 1, 5, "Picareta de ferro", "A compra que faz a partida andar"),
                new Oferta("picareta_ouro", FILEIRA_1[2], Material.GOLDEN_PICKAXE, 1, 4, "Picareta de ouro", "Cava rápido, e vale pouco em briga"),
                new Oferta("picareta_dima", FILEIRA_1[3], Material.DIAMOND_PICKAXE, 1, 15, "Picareta de diamante", "Quebra obsidiana em tempo útil"),

                new Oferta("machado_pedra", FILEIRA_2[0], Material.STONE_AXE, 1, 3, "Machado de pedra", "Contra madeira, e contra gente"),
                new Oferta("machado_ferro", FILEIRA_2[1], Material.IRON_AXE, 1, 8, "Machado de ferro", "O maior dano por golpe deste preço"),
                new Oferta("machado_ouro", FILEIRA_2[2], Material.GOLDEN_AXE, 1, 6, "Machado de ouro", "Rápido, e some rápido também"),
                new Oferta("machado_dima", FILEIRA_2[3], Material.DIAMOND_AXE, 1, 18, "Machado de diamante", "Derruba armadura de ferro em três"),

                new Oferta("tesoura", FILEIRA_3[0], Material.SHEARS, 1, 3, "Tesoura", "Corta lã de defesa em um golpe"))));

        // Trinta segundos em todas: é o tempo de uma investida, não de uma
        // partida. Poção longa faria o time forte comprar e nunca mais soltar.
        aba(new Aba(5, "pocoes", Material.BREWING_STAND, "Poções", List.of(
                new Oferta("pocao_forca", FILEIRA_1[0], Material.POTION, 1, 8, "Força II (30s)", "O dobro de dano enquanto durar"),
                new Oferta("pocao_agilidade", FILEIRA_1[1], Material.POTION, 1, 6, "Agilidade II (30s)", "Para chegar antes na torre neutra"),
                new Oferta("pocao_invisivel", FILEIRA_1[2], Material.POTION, 1, 12, "Invisibilidade (30s)", "Some inteiro, com armadura e tudo", Truque.INVISIBILIDADE))));

        aba(new Aba(6, "comida", Material.COOKED_BEEF, "Comida", List.of(
                new Oferta("maca", FILEIRA_1[0], Material.GOLDEN_APPLE, 1, 4, "Maçã dourada", "Vida extra na hora de segurar a torre"),
                new Oferta("bife", FILEIRA_1[1], Material.COOKED_BEEF, 8, 2, "8 bifes", "Fome vazia não regenera vida"),
                new Oferta("cenoura", FILEIRA_1[2], Material.CARROT, 8, 1, "8 cenouras", "Barata, para não voltar ao castelo só por fome"))));

        aba(new Aba(7, "especiais", Material.TNT, "Especiais", List.of(
                new Oferta("bola_de_fogo", FILEIRA_1[0], Material.FIRE_CHARGE, 1, 8, "Bola de fogo", "Clique para atirar: vai longe e abre defesa", Truque.BOLA_DE_FOGO),
                new Oferta("tnt", FILEIRA_1[1], Material.TNT, 1, 10, "TNT automática", "Acende sozinha ao ser posta", Truque.TNT_AUTOMATICA),
                new Oferta("perola", FILEIRA_1[2], Material.ENDER_PEARL, 1, 6, "Pérola do Fim", "Entra no castelo inimigo por cima do muro"),
                new Oferta("golem", FILEIRA_1[3], Material.IRON_GOLEM_SPAWN_EGG, 1, 25, "Ovo de golem de ferro", "Guarda o castelo por 5 minutos"),

                new Oferta("agua", FILEIRA_2[0], Material.WATER_BUCKET, 1, 5, "Balde de água", "Apaga fogo e derruba quem está subindo"),
                new Oferta("lava", FILEIRA_2[1], Material.LAVA_BUCKET, 1, 10, "Balde de lava", "Fecha um caminho estreito sozinho"),
                new Oferta("torre", FILEIRA_2[2], Material.CHEST, 1, 20, "Torre compacta", "Levanta uma torre pronta, com escada e plataforma", Truque.TORRE))));

        // A compra rápida de fábrica: o que serve para quase toda saída do
        // castelo, na ordem em que se costuma precisar.
        this.rapidaDeFabrica = List.of("la", "espada_pedra", "malha_bota", "picareta_ferro",
                "arco", "flechas", "maca", "bola_de_fogo", "tnt", "perola", "agua", "obsidiana");
    }

    private void aba(Aba aba) {
        abas.put(aba.chave(), aba);
        for (Oferta oferta : aba.ofertas()) {
            porId.put(oferta.id(), oferta);
        }
    }

    // ---------------------------------------------------------------- abrir

    /** Clique com a barra de ouro na mão abre a loja. */
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
        abrir(evento.getPlayer(), null);
    }

    /** Aba nula é a compra rápida, que se monta na hora para cada jogador. */
    private void abrir(Player jogador, Aba aba) {
        String titulo = aba == null ? "Compra rápida" : aba.nome();
        Inventory bau = Bukkit.createInventory(new DonoDaLoja(aba), 54, ChatColor.DARK_GRAY + titulo);
        int carteira = diamantes(jogador);
        boolean vermelho = ehVermelho(jogador);

        bau.setItem(0, iconeDaRapida(aba == null));
        bau.setItem(9, aba == null
                ? vidro(Material.LIME_STAINED_GLASS_PANE, ChatColor.GREEN + "Compra rápida")
                : vidro(Material.GRAY_STAINED_GLASS_PANE, " "));
        for (Aba outra : abas.values()) {
            bau.setItem(outra.slot(), iconeDaAba(outra, outra == aba));
            bau.setItem(9 + outra.slot(), outra == aba
                    ? vidro(Material.LIME_STAINED_GLASS_PANE, ChatColor.GREEN + outra.nome())
                    : vidro(Material.GRAY_STAINED_GLASS_PANE, " "));
        }
        bau.setItem(8, carteiraNaTela(carteira));

        List<Oferta> mostrar = aba == null ? daRapida(jogador) : aba.ofertas();
        int lugar = 0;
        for (Oferta oferta : mostrar) {
            int slot = aba == null ? lugarDaRapida(lugar++) : oferta.slot();
            if (slot < 0) {
                continue;
            }
            bau.setItem(slot, etiquetar(montar(oferta, vermelho), oferta, carteira, aba == null));
        }
        jogador.openInventory(bau);
    }

    private int lugarDaRapida(int indice) {
        if (indice < FILEIRA_1.length) {
            return FILEIRA_1[indice];
        }
        if (indice < FILEIRA_1.length + FILEIRA_2.length) {
            return FILEIRA_2[indice - FILEIRA_1.length];
        }
        int terceira = indice - FILEIRA_1.length - FILEIRA_2.length;
        return terceira < FILEIRA_3.length ? FILEIRA_3[terceira] : -1;
    }

    // ------------------------------------------------------ compra rápida

    private List<String> idsDaRapida(Player jogador) {
        List<String> guardada = rapidas.getStringList(jogador.getUniqueId().toString());
        return guardada.isEmpty() && !rapidas.contains(jogador.getUniqueId().toString())
                ? new ArrayList<>(rapidaDeFabrica)
                : new ArrayList<>(guardada);
    }

    private List<Oferta> daRapida(Player jogador) {
        List<Oferta> saida = new ArrayList<>();
        for (String id : idsDaRapida(jogador)) {
            Oferta oferta = porId.get(id);
            if (oferta != null) {
                saida.add(oferta);
            }
        }
        return saida;
    }

    /**
     * Põe ou tira da compra rápida, conforme o item já esteja lá ou não.
     *
     * O mesmo clique faz as duas coisas porque é o mesmo gesto mental — "esse
     * eu quero à mão" e "esse não" —, e ter dois atalhos para lados opostos da
     * mesma decisão só dá o que decorar.
     */
    private void alternarNaRapida(Player jogador, Oferta oferta) {
        List<String> ids = idsDaRapida(jogador);
        if (ids.remove(oferta.id())) {
            jogador.sendActionBar(ChatColor.YELLOW + oferta.nome() + ChatColor.GRAY
                    + " saiu da compra rápida");
        } else {
            int cabem = FILEIRA_1.length + FILEIRA_2.length + FILEIRA_3.length;
            if (ids.size() >= cabem) {
                jogador.sendActionBar(ChatColor.RED + "A compra rápida está cheia ("
                        + cabem + " itens)");
                return;
            }
            ids.add(oferta.id());
            jogador.sendActionBar(ChatColor.GREEN + oferta.nome() + ChatColor.GRAY
                    + " entrou na compra rápida");
        }
        rapidas.set(jogador.getUniqueId().toString(), ids);
        try {
            rapidas.save(arquivoRapida);
        } catch (IOException erro) {
            plugin.getLogger().warning("Não consegui guardar a compra rápida: " + erro.getMessage());
        }
        jogador.playSound(jogador.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.6f);
    }

    // ------------------------------------------------------------- ícones

    private ItemStack iconeDaRapida(boolean aberta) {
        ItemStack icone = new ItemStack(Material.NETHER_STAR);
        ItemMeta dados = icone.getItemMeta();
        dados.setDisplayName((aberta ? ChatColor.GREEN : ChatColor.YELLOW) + "Compra rápida");
        dados.setLore(List.of(
                ChatColor.GRAY + "Os itens que você escolheu ter à mão",
                ChatColor.DARK_GRAY + "Shift + clique num item para pôr ou tirar"));
        icone.setItemMeta(dados);
        return icone;
    }

    private ItemStack iconeDaAba(Aba aba, boolean aberta) {
        ItemStack icone = new ItemStack(aba.icone());
        ItemMeta dados = icone.getItemMeta();
        dados.setDisplayName((aberta ? ChatColor.GREEN : ChatColor.YELLOW) + aba.nome());
        dados.setLore(List.of(aberta
                ? ChatColor.GRAY + "Você está aqui"
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
        dados.setDisplayName(ChatColor.AQUA + "Você tem " + carteira + " diamante" + (carteira == 1 ? "" : "s"));
        dados.setLore(List.of(ChatColor.GRAY + "Cada torre do seu time paga",
                ChatColor.GRAY + "1 diamante a cada 30 segundos"));
        moeda.setItemMeta(dados);
        return moeda;
    }

    // ----------------------------------------------------------- os itens

    /** O item como ele sai da loja, já com a cor e os poderes do time de quem compra. */
    private ItemStack montar(Oferta oferta, boolean vermelho) {
        ItemStack item = oferta.truque() == Truque.LA_DO_TIME
                ? new ItemStack(vermelho ? Material.RED_WOOL : Material.BLUE_WOOL, oferta.quantidade())
                : new ItemStack(oferta.tipo(), oferta.quantidade());

        if (oferta.truque() == Truque.VESTIR) {
            enfeitar(item, vermelho);
        }
        if ("arco_forca".equals(oferta.id())) {
            item.addUnsafeEnchantment(Enchantment.POWER, 1);
        }
        if (item.getType() == Material.POTION) {
            encher(item, oferta.id());
        }

        ItemMeta dados = item.getItemMeta();
        dados.setDisplayName(ChatColor.YELLOW + oferta.nome());
        // Ferramenta e armadura da loja não gastam. O desgaste só criava uma
        // segunda moeda invisível: a pessoa via a picareta sumindo e voltava ao
        // castelo no meio da disputa, sem que isso decidisse nada.
        if (naoGasta(item.getType())) {
            dados.setUnbreakable(true);
        }
        if (oferta.truque() != Truque.NENHUM && oferta.truque() != Truque.LA_DO_TIME
                && oferta.truque() != Truque.VESTIR) {
            dados.getPersistentDataContainer().set(marca, PersistentDataType.STRING,
                    oferta.truque().name());
        }
        item.setItemMeta(dados);
        return item;
    }

    private boolean naoGasta(Material tipo) {
        String nome = tipo.name();
        return nome.endsWith("_HELMET") || nome.endsWith("_CHESTPLATE")
                || nome.endsWith("_LEGGINGS") || nome.endsWith("_BOOTS")
                || nome.endsWith("_PICKAXE") || nome.endsWith("_AXE")
                || nome.endsWith("_SWORD") || nome.endsWith("_SHOVEL")
                || tipo == Material.SHEARS || tipo == Material.BOW || tipo == Material.SHIELD;
    }

    /**
     * Enfeite da cor do time na armadura.
     *
     * Redstone para o vermelho e lápis para o azul — os mesmos blocos que cada
     * time defende, então a cor da armadura diz de qual lado a pessoa é sem
     * precisar de plaquinha.
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
            plugin.getLogger().warning("Não consegui pôr o enfeite do time na armadura: " + erro);
        }
    }

    /**
     * As três poções, todas de trinta segundos.
     *
     * Sem cor escolhida à mão: o jogo tira a cor do próprio efeito, e aí ela
     * sai igual à da poção de sempre. A cor inventada deixava a garrafa com um
     * tom que não existe em lugar nenhum do Minecraft.
     */
    private void encher(ItemStack garrafa, String id) {
        if (!(garrafa.getItemMeta() instanceof PotionMeta dados)) {
            return;
        }
        if ("pocao_forca".equals(id)) {
            dados.addCustomEffect(new PotionEffect(PotionEffectType.STRENGTH, 30 * 20, 1), true);
        } else if ("pocao_agilidade".equals(id)) {
            dados.addCustomEffect(new PotionEffect(PotionEffectType.SPEED, 30 * 20, 1), true);
        } else {
            dados.addCustomEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 30 * 20, 0), true);
        }
        garrafa.setItemMeta(dados);
    }

    private ItemStack etiquetar(ItemStack item, Oferta oferta, int carteira, boolean naRapida) {
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
        linhas.add(ChatColor.DARK_GRAY + (naRapida
                ? "Shift + clique para tirar daqui"
                : "Shift + clique para pôr na compra rápida"));
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
        evento.setCancelled(true);
        if (!(evento.getWhoClicked() instanceof Player jogador)) {
            return;
        }
        if (evento.getRawSlot() == 0) {
            abrir(jogador, null);
            return;
        }
        for (Aba aba : abas.values()) {
            if (aba.slot() == evento.getRawSlot()) {
                abrir(jogador, aba);
                return;
            }
        }
        List<Oferta> mostradas = dono.aba == null ? daRapida(jogador) : dono.aba.ofertas();
        int lugar = 0;
        for (Oferta oferta : mostradas) {
            int slot = dono.aba == null ? lugarDaRapida(lugar++) : oferta.slot();
            if (slot != evento.getRawSlot()) {
                continue;
            }
            if (evento.isShiftClick()) {
                alternarNaRapida(jogador, oferta);
                abrir(jogador, dono.aba);
            } else {
                comprar(jogador, dono.aba, oferta);
            }
            return;
        }
    }

    private void comprar(Player jogador, Aba aba, Oferta oferta) {
        int carteira = diamantes(jogador);
        if (carteira < oferta.preco()) {
            int falta = oferta.preco() - carteira;
            jogador.sendActionBar(ChatColor.RED + "Faltam " + falta + " diamante"
                    + (falta > 1 ? "s" : "") + " para " + oferta.nome());
            jogador.playSound(jogador.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            return;
        }
        boolean vestir = oferta.truque() == Truque.VESTIR;
        if (!vestir && jogador.getInventory().firstEmpty() == -1) {
            jogador.sendActionBar(ChatColor.RED + "Inventário cheio.");
            jogador.playSound(jogador.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            return;
        }
        cobrar(jogador, oferta.preco());
        ItemStack comprado = montar(oferta, ehVermelho(jogador));
        if (vestir) {
            vestir(jogador, comprado);
        } else {
            ferramentas.guardar(jogador, comprado);
            jogador.getInventory().addItem(comprado);
        }
        // Barra de ação, e não chat: a compra interessa a quem comprou e a mais
        // ninguém, e no meio de uma briga o chat já está cheio.
        jogador.sendActionBar(ChatColor.GREEN + "Comprou " + oferta.nome() + ChatColor.GRAY
                + " por " + oferta.preco() + " diamante" + (oferta.preco() > 1 ? "s" : ""));
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
            plugin.getLogger().warning("Cobrança de " + preco + " diamantes ficou incompleta para "
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
