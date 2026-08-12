package br.com.gamescraft.menus;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import com.destroystokyo.paper.profile.ProfileProperty;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Abre o menu de baú dos bonecos do lobby e leva o jogador ao servidor
 * escolhido.
 *
 * O Minecraft não abre baú por comando — é a única peça do conjunto que o
 * datapack não dá conta, e é só por isso que este plugin existe. Todo o resto
 * dos bonecos (corpo, nome, caixa de clique) continua no datapack gamecraft-npcs.
 */
public final class GcMenus extends JavaPlugin implements Listener {

    /** Marca que o datapack põe nas três peças de cada boneco. */
    private static final String PREFIXO = "gcnpc_";

    /**
     * Nome gravado no perfil do boneco. Não é o nome de quem emprestou a skin, e
     * isso é de propósito: com o nome de uma conta de verdade ali, o cliente de
     * quem chega depois vai buscar a skin daquela conta em vez de usar a textura
     * que mandamos — quem copiou via a skin certa e os outros viam a original.
     * Um nome que não existe força o cliente a usar a textura que veio junto.
     */
    private static final String NOME_DO_PERFIL = "GameCraftNPC";

    /** Um destino: o que aparece no slot e para onde o jogador vai. */
    private record Destino(int slot, String rotulo, String servidor) {
    }

    /** Um menu: o título do baú e os destinos dele. */
    private record Menu(String titulo, List<Destino> destinos) {
    }

    private final Map<String, Menu> menus = new LinkedHashMap<>();

    /** Marca de quem abriu um menu nosso, para não confundir com outro baú. */
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
        // deixam um espaço entre eles e ficam simétricos no meio.
        menus.put("bedwars", new Menu(ChatColor.AQUA + "Bed Wars", List.of(
                new Destino(11, ChatColor.AQUA + "Solo", "bedwars"),
                new Destino(15, ChatColor.AQUA + "Duplas", "bedwars"))));
        menus.put("pilares", new Menu(ChatColor.GOLD + "Pilares da Fortuna", List.of(
                new Destino(13, ChatColor.GOLD + "Entrar numa arena", "pillars"))));
        menus.put("pvp", new Menu(ChatColor.RED + "PvP", List.of(
                new Destino(13, ChatColor.RED + "Entrar na arena", "pvp"))));
        menus.put("build", new Menu(ChatColor.GREEN + "Build Battle", List.of(
                new Destino(13, ChatColor.GREEN + "Entrar", "buildbattle"))));
        menus.put("longos", new Menu(ChatColor.LIGHT_PURPLE + "Jogos Longos", List.of(
                new Destino(13, ChatColor.LIGHT_PURPLE + "Entrar", "ctf"))));
        menus.put("eventos", new Menu(ChatColor.YELLOW + "Eventos", List.of(
                new Destino(13, ChatColor.YELLOW + "Entrar", "eventos"))));

        getServer().getPluginManager().registerEvents(this, this);
        // Canal por onde se pede a troca de servidor ao proxy.
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        getLogger().info("Menus dos bonecos prontos: " + menus.size());
    }

    @Override
    public boolean onCommand(CommandSender quemMandou, Command comando, String rotulo, String[] argumentos) {
        if (!(quemMandou instanceof Player jogador)) {
            quemMandou.sendMessage("Esse comando precisa ser dado em jogo.");
            return true;
        }
        if (argumentos.length != 1) {
            jogador.sendMessage(ChatColor.RED + "Use: /npcskin <jogador>");
            return true;
        }
        Player modelo = Bukkit.getPlayerExact(argumentos[0]);
        if (modelo == null) {
            jogador.sendMessage(ChatColor.RED + "O jogador " + argumentos[0]
                    + " precisa estar online: a skin e copiada da que ele esta usando agora.");
            return true;
        }

        Entity boneco = bonecoMaisPerto(jogador);
        if (boneco == null) {
            jogador.sendMessage(ChatColor.RED + "Nenhum boneco por perto. Chegue mais perto de um.");
            return true;
        }

        // A textura vem do perfil de quem esta em jogo, e nao do nome. E isso que
        // congela a skin: se o modelo trocar a dele depois, o boneco continua com
        // a que foi copiada. Com o SkinsRestorer no ar, o perfil ja vem com a
        // skin que o /skin aplicou.
        ProfileProperty textura = null;
        for (ProfileProperty propriedade : modelo.getPlayerProfile().getProperties()) {
            if (propriedade.getName().equals("textures")) {
                textura = propriedade;
                break;
            }
        }
        if (textura == null) {
            jogador.sendMessage(ChatColor.RED + "Nao achei a textura de " + modelo.getName() + ".");
            return true;
        }

        StringBuilder nbt = new StringBuilder();
        nbt.append("data merge entity ").append(boneco.getUniqueId())
                .append(" {profile:{name:\"").append(NOME_DO_PERFIL)
                .append("\",properties:[{name:\"textures\",value:\"").append(textura.getValue()).append("\"");
        // A assinatura fica de fora de proposito: ela vale para a conta de onde
        // a textura saiu, e aqui o perfil e outro. Assinatura que nao confere e
        // pior que assinatura nenhuma — o cliente descarta a textura e volta a
        // buscar pela conta.
        nbt.append("}]}}");

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), nbt.toString());
        jogador.sendMessage(ChatColor.GREEN + "Boneco agora usa a skin de " + modelo.getName()
                + ChatColor.GRAY + " — e fica com ela mesmo que ele troque depois.");
        return true;
    }

    /** O mannequin mais proximo de quem deu o comando, ate 8 blocos. */
    private Entity bonecoMaisPerto(Player jogador) {
        Entity achado = null;
        double menor = Double.MAX_VALUE;
        for (Entity perto : jogador.getNearbyEntities(8, 8, 8)) {
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

    /** Clique direito no boneco ou na caixa de interação dele. */
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
                mandarPara(jogador, destino.servidor());
                return;
            }
        }
    }

    /**
     * Pede ao proxy que mova o jogador. O formato é o do BungeeCord, que o
     * Velocity entende: a palavra "Connect" seguida do nome do servidor.
     */
    private void mandarPara(Player jogador, String servidor) {
        byte[] dados = montarConnect(servidor);
        jogador.sendPluginMessage(this, "BungeeCord", dados);
    }

    private byte[] montarConnect(String servidor) {
        byte[] palavra = "Connect".getBytes(StandardCharsets.UTF_8);
        byte[] nome = servidor.getBytes(StandardCharsets.UTF_8);
        byte[] saida = new byte[2 + palavra.length + 2 + nome.length];
        int i = 0;
        saida[i++] = (byte) (palavra.length >> 8);
        saida[i++] = (byte) palavra.length;
        System.arraycopy(palavra, 0, saida, i, palavra.length);
        i += palavra.length;
        saida[i++] = (byte) (nome.length >> 8);
        saida[i++] = (byte) nome.length;
        System.arraycopy(nome, 0, saida, i, nome.length);
        return saida;
    }
}
