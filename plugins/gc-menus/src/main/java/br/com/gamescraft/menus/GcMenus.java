package br.com.gamescraft.menus;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

/**
 * Abre o menu de baú dos bonecos do lobby, leva o jogador ao servidor escolhido
 * e mantém a placa de cada boneco mostrando quanta gente está jogando lá.
 *
 * O Minecraft não abre baú por comando — é a única peça do conjunto que o
 * datapack não dá conta, e é por isso que este plugin existe. O resto dos
 * bonecos (corpo, placa, caixa de clique) continua no datapack gamecraft-npcs.
 */
public final class GcMenus extends JavaPlugin implements Listener, PluginMessageListener {

    /** Marca que o datapack põe nas três peças de cada boneco. */
    private static final String PREFIXO = "gcnpc_";

    /**
     * Nome gravado no perfil do boneco. Não é o nome de quem emprestou a skin, e
     * isso é de propósito: com o nome de uma conta de verdade ali, o cliente de
     * quem chega depois vai buscar a skin daquela conta em vez de usar a textura
     * que mandamos — quem copiou via a skin certa e os outros viam a original.
     */
    private static final String NOME_DO_PERFIL = "GameCraftNPC";

    /** O canal por onde se fala com o proxy. O nome é histórico, do BungeeCord. */
    private static final String CANAL = "BungeeCord";

    private record Destino(int slot, String rotulo, String servidor) {
    }

    private record Menu(String nome, NamedTextColor cor, String titulo, String servidor,
            List<Destino> destinos) {
    }

    private final Map<String, Menu> menus = new LinkedHashMap<>();

    /** Quanta gente há em cada servidor, pela última resposta do proxy. */
    private final Map<String, Integer> jogadores = new HashMap<>();

    /** Quem barra o clique dentro das torres. */
    private Torres protecao;

    /** Quem cuida das areas de construcao. */
    private Areas areas;

    /** A disputa das torres. */
    private Captura captura;

    private static final class DonoDoMenu implements InventoryHolder {
        private final Menu menu;

        private DonoDoMenu(Menu menu) {
            this.menu = menu;
        }

        @Override
        public Inventory getInventory() {
            return null;
        }
    }

    @Override
    public void onEnable() {
        // Slot 13 é o centro exato de um baú de 27. Com dois destinos, 11 e 15
        // ficam simétricos em volta do meio.
        menus.put("bedwars", new Menu("BED WARS", NamedTextColor.AQUA,
                ChatColor.AQUA + "Bed Wars", "bedwars", List.of(
                        new Destino(11, ChatColor.AQUA + "Solo", "bedwars"),
                        new Destino(15, ChatColor.AQUA + "Duplas", "bedwars"))));
        menus.put("pilares", new Menu("PILARES DA FORTUNA", NamedTextColor.GOLD,
                ChatColor.GOLD + "Pilares da Fortuna", "pillars", List.of(
                        new Destino(13, ChatColor.GOLD + "Entrar numa arena", "pillars"))));
        menus.put("pvp", new Menu("PVP", NamedTextColor.RED,
                ChatColor.RED + "PvP", "pvp", List.of(
                        new Destino(13, ChatColor.RED + "Entrar na arena", "pvp"))));
        menus.put("build", new Menu("BUILD BATTLE", NamedTextColor.GREEN,
                ChatColor.GREEN + "Build Battle", "buildbattle", List.of(
                        new Destino(13, ChatColor.GREEN + "Entrar", "buildbattle"))));
        menus.put("longos", new Menu("MEGAGAMES", NamedTextColor.LIGHT_PURPLE,
                ChatColor.LIGHT_PURPLE + "MegaGames", "ctf", List.of(
                        new Destino(13, ChatColor.LIGHT_PURPLE + "Entrar", "ctf"))));
        menus.put("eventos", new Menu("EVENTOS", NamedTextColor.YELLOW,
                ChatColor.YELLOW + "Eventos", "eventos", List.of(
                        new Destino(13, ChatColor.YELLOW + "Entrar", "eventos"))));
        // O caminho de volta. Este boneco mora nos servidores de jogo, e não no
        // lobby — é o único que aponta para dentro.
        menus.put("lobby", new Menu("LOBBY", NamedTextColor.WHITE,
                ChatColor.WHITE + "Lobby", "lobby", List.of(
                        new Destino(13, ChatColor.GREEN + "Voltar ao lobby", "lobby"))));

        getServer().getPluginManager().registerEvents(this, this);
        // A lista de torres protegidas vem da config; onde nao houver lista, o
        // ouvinte fica ali sem nada para barrar.
        saveDefaultConfig();
        protecao = new Torres(this);
        getServer().getPluginManager().registerEvents(protecao, this);
        areas = new Areas(this, protecao);
        getServer().getPluginManager().registerEvents(areas, this);
        captura = new Captura(this, protecao);
        getServer().getPluginManager().registerEvents(new Times(this, captura), this);
        getServer().getMessenger().registerOutgoingPluginChannel(this, CANAL);
        getServer().getMessenger().registerIncomingPluginChannel(this, CANAL, this);

        // Uma pergunta ao proxy a cada 5 segundos. Mais rápido que isso é
        // conversa à toa: o número muda quando alguém entra ou sai, não a cada
        // tique.
        getServer().getScheduler().runTaskTimer(this, this::perguntarQuantos, 100L, 100L);
        getLogger().info("Menus dos bonecos prontos: " + menus.size());
    }

    /**
     * Pergunta ao proxy quanta gente há em cada modo.
     *
     * A conversa com o proxy sai montada em cima de um jogador porque é assim
     * que o canal funciona: quem carrega a mensagem é a conexão de alguém. Sem
     * ninguém no lobby não há o que perguntar — e nem para quem mostrar.
     */
    private void perguntarQuantos() {
        Player carteiro = Bukkit.getOnlinePlayers().stream().findFirst().orElse(null);
        if (carteiro == null) {
            return;
        }
        for (Menu menu : menus.values()) {
            carteiro.sendPluginMessage(this, CANAL, montarPlayerCount(menu.servidor()));
        }
    }

    @Override
    public void onPluginMessageReceived(String canal, Player jogador, byte[] mensagem) {
        if (!canal.equals(CANAL)) {
            return;
        }
        try (DataInputStream leitura = new DataInputStream(new ByteArrayInputStream(mensagem))) {
            if (!leitura.readUTF().equals("PlayerCount")) {
                return;
            }
            String servidor = leitura.readUTF();
            int quantos = leitura.readInt();
            jogadores.put(servidor, quantos);
            // Escreve toda vez, e não só quando o número muda. Pular a escrita
            // parecia economia boa até aparecer o buraco: boneco criado depois
            // da última mudança nascia com a placa vazia e ficava assim, porque
            // o número "não tinha mudado". São seis placas a cada cinco
            // segundos — não é isso que pesa num servidor.
            atualizarPlacas(servidor, quantos);
        } catch (IOException erro) {
            getLogger().warning("Resposta do proxy veio quebrada: " + erro.getMessage());
        }
    }

    /** Reescreve a placa de todos os bonecos de um modo. */
    private void atualizarPlacas(String servidor, int quantos) {
        for (Map.Entry<String, Menu> entrada : menus.entrySet()) {
            Menu menu = entrada.getValue();
            if (!menu.servidor().equals(servidor)) {
                continue;
            }
            String marca = PREFIXO + entrada.getKey();
            // Só a placa de baixo, a pequena. O nome fica na de cima, que é
            // maior e não muda — são duas entidades porque o tamanho vale para a
            // placa inteira, e a contagem tinha de ser menor que o nome.
            Component texto = Component.text(quantos + " jogando", NamedTextColor.GRAY);
            for (World mundo : Bukkit.getWorlds()) {
                for (Entity entidade : mundo.getEntitiesByClass(TextDisplay.class)) {
                    if (entidade.getScoreboardTags().contains(marca)
                            && entidade.getScoreboardTags().contains("gcnpc_num")) {
                        ((TextDisplay) entidade).text(texto);
                    }
                }
            }
        }
    }

    private byte[] montarPlayerCount(String servidor) {
        return montarMensagem("PlayerCount", servidor);
    }

    private byte[] montarMensagem(String subcanal, String argumento) {
        byte[] a = subcanal.getBytes(StandardCharsets.UTF_8);
        byte[] b = argumento.getBytes(StandardCharsets.UTF_8);
        byte[] saida = new byte[2 + a.length + 2 + b.length];
        int i = 0;
        saida[i++] = (byte) (a.length >> 8);
        saida[i++] = (byte) a.length;
        System.arraycopy(a, 0, saida, i, a.length);
        i += a.length;
        saida[i++] = (byte) (b.length >> 8);
        saida[i++] = (byte) b.length;
        System.arraycopy(b, 0, saida, i, b.length);
        return saida;
    }

    @Override
    public boolean onCommand(CommandSender quemMandou, Command comando, String rotulo, String[] argumentos) {
        // A varredura vem antes da checagem de jogador: ela nao depende de
        // posicao, e poder rodar pelo console e o que permite refazer a lista
        // com o servidor vazio.
        if (comando.getName().equals("torres")) {
            protecao.varrer(quemMandou);
            return true;
        }
        if (comando.getName().equals("castelos")) {
            captura.pintarCastelos(quemMandou);
            return true;
        }
        if (comando.getName().equals("neutro")) {
            captura.zerar(quemMandou);
            return true;
        }
        if (!(quemMandou instanceof Player jogador)) {
            quemMandou.sendMessage("Esse comando precisa ser dado em jogo.");
            return true;
        }
        if (comando.getName().equals("npcitem")) {
            return darItem(jogador, argumentos);
        }
        if (argumentos.length != 1) {
            jogador.sendMessage(ChatColor.RED + "Use: /npcskin <jogador>");
            return true;
        }
        Entity boneco = bonecoMaisPerto(jogador);
        if (boneco == null) {
            jogador.sendMessage(ChatColor.RED + "Nenhum boneco por perto. Chegue mais perto de um.");
            return true;
        }

        // A skin vem sempre da Mojang, buscada na hora pelo nome da conta, e nao
        // do perfil de quem esta em jogo. Dois motivos: o servidor guarda o
        // perfil em cache, entao quem trocou de skin ha pouco continuava vindo
        // com a antiga; e o SkinsRestorer aplica a skin no proxy, de onde este
        // servidor nao enxerga — o perfil daqui traz a skin da conta, nao a que
        // o /skin pos.
        //
        // A ida a internet sai da thread principal: o servidor inteiro congela
        // enquanto ela nao volta.
        String nome = argumentos[0];
        jogador.sendMessage(ChatColor.GRAY + "Buscando a skin da conta " + nome + "...");
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            String textura;
            try {
                textura = Skins.buscar(nome);
            } catch (Exception erro) {
                textura = null;
                getLogger().warning("Busca de skin falhou: " + erro.getMessage());
            }
            String valor = textura;
            Bukkit.getScheduler().runTask(this, () -> {
                if (valor == null) {
                    jogador.sendMessage(ChatColor.RED + "Nao consegui a skin da conta " + nome + ".");
                    return;
                }
                aplicarSkin(jogador, boneco, nome, valor);
            });
        });
        return true;
    }

    /**
     * Põe na mão do boneco o item que o jogador está segurando.
     *
     * Existe porque a caixa de interação fica na frente do boneco e engole o
     * clique — sem ela o menu não abriria, mas com ela não há como equipar o
     * boneco à mão, como se faz numa armadura decorativa.
     */
    private boolean darItem(Player jogador, String[] argumentos) {
        Entity boneco = bonecoMaisPerto(jogador);
        if (boneco == null) {
            jogador.sendMessage(ChatColor.RED + "Nenhum boneco por perto. Chegue mais perto de um.");
            return true;
        }
        if (!(boneco instanceof LivingEntity corpo)) {
            return true;
        }
        boolean esquerda = argumentos.length > 0 && argumentos[0].equalsIgnoreCase("esquerda");
        ItemStack naMao = jogador.getInventory().getItemInMainHand();
        EntityEquipment equipamento = corpo.getEquipment();
        if (equipamento == null) {
            jogador.sendMessage(ChatColor.RED + "Esse boneco nao segura item.");
            return true;
        }
        if (esquerda) {
            equipamento.setItemInOffHand(naMao.clone());
        } else {
            equipamento.setItemInMainHand(naMao.clone());
        }
        String onde = esquerda ? "esquerda" : "direita";
        if (naMao.getType() == Material.AIR) {
            jogador.sendMessage(ChatColor.YELLOW + "Mao " + onde + " do boneco esvaziada.");
        } else {
            jogador.sendMessage(ChatColor.GREEN + "Boneco agora segura "
                    + naMao.getType().name().toLowerCase() + " na mao " + onde + ".");
        }
        return true;
    }

    /**
     * Grava a textura no boneco.
     *
     * O nome do perfil nao e o de quem emprestou a skin: com o nome de uma
     * conta de verdade ali, o cliente de quem chega depois vai buscar a skin
     * daquela conta em vez de usar a textura que mandamos.
     *
     * A assinatura fica de fora pelo mesmo motivo: ela vale para a conta de
     * origem, e assinatura que nao confere faz o cliente descartar a textura.
     */
    private void aplicarSkin(Player jogador, Entity boneco, String nome, String textura) {
        String comando = "data merge entity " + boneco.getUniqueId()
                + " {profile:{name:\"" + NOME_DO_PERFIL
                + "\",properties:[{name:\"textures\",value:\"" + textura + "\"}]}}";
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), comando);
        jogador.sendMessage(ChatColor.GREEN + "Boneco agora usa a skin da conta " + nome
                + ChatColor.GRAY + " — e fica com ela mesmo que a conta troque depois.");
    }

    /** O boneco mais próximo de quem deu o comando, até 8 blocos. */
    private Entity bonecoMaisPerto(Player jogador) {
        Entity achado = null;
        double menor = Double.MAX_VALUE;
        for (Entity perto : jogador.getNearbyEntities(8, 8, 8)) {
            // O tipo é comparado pelo nome porque a API contra a qual isto
            // compila não conhece MANNEQUIN, que só existe na 26.2.
            if (menuDe(perto) == null || !perto.getType().name().equals("MANNEQUIN")) {
                continue;
            }
            double distancia = perto.getLocation().distanceSquared(jogador.getLocation());
            if (distancia < menor) {
                menor = distancia;
                achado = perto;
            }
        }
        return achado;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
    public void aoClicar(PlayerInteractEntityEvent evento) {
        Menu menu = menuDe(evento.getRightClicked());
        if (menu == null) {
            return;
        }
        evento.setCancelled(true);
        abrir(evento.getPlayer(), menu);
    }

    /** Soco no boneco: abre o mesmo menu em vez de bater. */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
    public void aoBater(EntityDamageByEntityEvent evento) {
        if (!(evento.getDamager() instanceof Player jogador)) {
            return;
        }
        Menu menu = menuDe(evento.getEntity());
        if (menu == null) {
            return;
        }
        evento.setCancelled(true);
        abrir(jogador, menu);
    }

    /** Descobre de qual jogo é a peça clicada, pela marca do datapack. */
    private Menu menuDe(Entity entidade) {
        for (String marca : entidade.getScoreboardTags()) {
            if (marca.startsWith(PREFIXO)) {
                Menu menu = menus.get(marca.substring(PREFIXO.length()));
                if (menu != null) {
                    return menu;
                }
            }
        }
        return null;
    }

    private void abrir(Player jogador, Menu menu) {
        Inventory bau = Bukkit.createInventory(new DonoDoMenu(menu), 27, menu.titulo());
        for (Destino destino : menu.destinos()) {
            ItemStack olho = new ItemStack(Material.ENDER_EYE);
            ItemMeta dados = olho.getItemMeta();
            dados.setDisplayName(destino.rotulo());
            List<String> linhas = new ArrayList<>();
            linhas.add(ChatColor.GRAY + "Clique para entrar");
            Integer quantos = jogadores.get(destino.servidor());
            if (quantos != null) {
                linhas.add(ChatColor.DARK_GRAY + "" + quantos + " jogando agora");
            }
            dados.setLore(linhas);
            olho.setItemMeta(dados);
            bau.setItem(destino.slot(), olho);
        }
        jogador.openInventory(bau);
    }

    /** Clique dentro do menu: nada é pego, o slot só decide o destino. */
    @EventHandler
    public void aoClicarNoMenu(InventoryClickEvent evento) {
        if (!(evento.getInventory().getHolder() instanceof DonoDoMenu dono)) {
            return;
        }
        evento.setCancelled(true);
        if (!(evento.getWhoClicked() instanceof Player jogador)) {
            return;
        }
        for (Destino destino : dono.menu.destinos()) {
            if (destino.slot() == evento.getRawSlot()) {
                jogador.closeInventory();
                jogador.sendPluginMessage(this, CANAL, montarMensagem("Connect", destino.servidor()));
                return;
            }
        }
    }
}
