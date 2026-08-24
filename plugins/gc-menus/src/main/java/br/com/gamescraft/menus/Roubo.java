package br.com.gamescraft.menus;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Team;

/**
 * O roubo do bloco inimigo: quebrar, carregar e levar até o seu.
 *
 * Antes a partida acabava no golpe: quem encostava a picareta na redstone do
 * vermelho ganhava ali mesmo. Quem chegasse primeiro ao castelo vazio vencia, e
 * defender não valia nada depois que o inimigo passasse pela porta.
 *
 * Agora o golpe só começa a jogada. O bloco cai no chão, alguém precisa pegá-lo,
 * atravessar o mapa inteiro com ele à vista de todos e subir no bloco do próprio
 * time. Entre uma coisa e outra há um caminho de volta para o time roubado
 * fazer: matar quem carrega, e encostar no bloco caído para levá-lo para casa.
 *
 * As regras que decidem para quem o bloco vai:
 *
 * <ul>
 *   <li>O time do bloco não quebra o próprio — não há como se sabotar.</li>
 *   <li>O inimigo quebra e o bloco cai sempre, com picareta ou com a mão.</li>
 *   <li>Morreu carregando, o bloco fica onde o corpo caiu, e fica lá: não some
 *       com o tempo, não queima e não é levado pela lava.</li>
 *   <li>Encostou nele alguém do time dono, volta para a base na hora.</li>
 *   <li>Encostou nele o inimigo, a corrida continua de onde parou.</li>
 * </ul>
 */
final class Roubo implements Listener {

    /**
     * Quantos blocos acima da base ainda contam como "em cima do seu bloco".
     *
     * O time tapa o próprio bloco com obsidiana para defendê-lo, e depois disso
     * ninguém mais pisa nele: pisa-se na defesa. Cinco blocos de folga fazem a
     * pilha inteira valer, e é a mesma folga que o nascimento já usa.
     */
    private static final int ALCANCE = 5;

    /** Onde o bloco de um time mora, e onde ele está agora. */
    private static final class Estado {
        private Location onde;
        private boolean emCasa = true;
        private Item noChao;
    }

    private final JavaPlugin plugin;
    private final Times times;
    private final Torres torres;
    private final String nomeVermelho;
    private final String nomeAzul;
    private final NamespacedKey marcaDoDono;
    private final Map<String, Estado> estado = new HashMap<>();
    private final Set<UUID> brilhando = new HashSet<>();

    private Partida partida;

    Roubo(JavaPlugin plugin, Times times, Torres torres) {
        this.plugin = plugin;
        this.times = times;
        this.torres = torres;
        this.nomeVermelho = plugin.getConfig().getString("time-vermelho", "Vermelho");
        this.nomeAzul = plugin.getConfig().getString("time-azul", "Azul");
        this.marcaDoDono = new NamespacedKey(plugin, "bloco_do_time");

        // Meio segundo entre uma volta e outra. Mais rápido não se vê, e o que
        // este relógio faz — procurar quem carrega, conferir se ele já chegou —
        // custa uma varredura de inventários a cada volta.
        plugin.getServer().getScheduler().runTaskTimer(plugin, this::relogio, 20L, 10L);
    }

    void ligarPartida(Partida partida) {
        this.partida = partida;
    }

    private List<String> donos() {
        return List.of(nomeVermelho, nomeAzul);
    }

    // ------------------------------------------------------------- partida

    /**
     * Acha os dois blocos e guarda onde eles estavam.
     *
     * A busca é feita agora, e a posição fica na memória: assim que o bloco é
     * roubado o lugar dele vira ar, e uma busca feita depois não acharia mais
     * nada — nem para devolver, nem para dizer onde o time nasce.
     */
    void novaPartida() {
        estado.clear();
        for (String dono : donos()) {
            Estado meu = new Estado();
            meu.onde = times.blocoDo(dono);
            if (meu.onde == null) {
                plugin.getLogger().warning("Nao achei o bloco do time " + dono
                        + ": a partida vai rodar sem alvo para esse lado.");
            }
            estado.put(dono, meu);
        }
    }

    /** Devolve os dois blocos e apaga o brilho de quem estava carregando. */
    void zerar() {
        for (String dono : donos()) {
            Estado meu = estado.get(dono);
            if (meu == null) {
                continue;
            }
            if (meu.noChao != null && meu.noChao.isValid()) {
                meu.noChao.remove();
            }
            for (Player jogador : Bukkit.getOnlinePlayers()) {
                tirarDoInventario(jogador, dono);
            }
            porNoLugar(meu, dono);
        }
        for (UUID quem : brilhando) {
            Player jogador = Bukkit.getPlayer(quem);
            if (jogador != null) {
                jogador.setGlowing(false);
            }
        }
        brilhando.clear();
        estado.clear();
    }

    /** Onde fica o bloco daquele time, mesmo enquanto ele está roubado. */
    Location baseDo(String time) {
        Estado meu = estado.get(time);
        return meu == null ? null : meu.onde;
    }

    /**
     * A proteção das torres não vale para estes dois blocos.
     *
     * Quatro dos cinco sinalizadores de cada castelo protegem três blocos para
     * cada lado, e o bloco do time fica dentro dessa sombra. Sem esta exceção o
     * inimigo bateria nele a partida inteira sem nunca conseguir levá-lo.
     */
    boolean eBlocoDeTime(Block bloco) {
        return donoDaBase(bloco) != null;
    }

    // ---------------------------------------------------------------- item

    private ItemStack itemDo(String dono) {
        boolean vermelho = dono.equals(nomeVermelho);
        ItemStack item = new ItemStack(vermelho ? Material.REDSTONE_BLOCK : Material.LAPIS_BLOCK);
        ItemMeta dados = item.getItemMeta();
        dados.setDisplayName(cor(dono) + "Bloco do " + dono);
        dados.setLore(List.of(ChatColor.GRAY + "Suba no bloco do seu time",
                ChatColor.GRAY + "para pontuar"));
        dados.getPersistentDataContainer().set(marcaDoDono, PersistentDataType.STRING, dono);
        item.setItemMeta(dados);
        return item;
    }

    /**
     * De que time é aquele item, se for um bloco de partida.
     *
     * A marca vai no item, e não no nome: o nome se copia numa bigorna, e a
     * marca não. Um bloco de redstone comum nunca vira alvo por parecer com um.
     */
    private String donoDo(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return null;
        }
        return item.getItemMeta().getPersistentDataContainer()
                .get(marcaDoDono, PersistentDataType.STRING);
    }

    /** De que time é aquele bloco no mapa, se for um dos dois da partida. */
    private String donoDaBase(Block bloco) {
        for (String dono : donos()) {
            Estado meu = estado.get(dono);
            if (meu == null || meu.onde == null) {
                continue;
            }
            if (meu.onde.getWorld() != null && !meu.onde.getWorld().equals(bloco.getWorld())) {
                continue;
            }
            if (meu.onde.getBlockX() == bloco.getX() && meu.onde.getBlockY() == bloco.getY()
                    && meu.onde.getBlockZ() == bloco.getZ()) {
                return dono;
            }
        }
        return null;
    }

    // -------------------------------------------------------------- quebra

    /**
     * Quebrar o bloco do inimigo tira ele da base e joga no chão.
     *
     * Cai sempre, com o que for. A redstone e o lápis só caem com picareta de
     * pedra para cima, e o kit começa com uma de madeira: quem atravessava o
     * mapa e quebrava o bloco via ele sumir no ar e voltava de mãos vazias.
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void aoQuebrar(BlockBreakEvent evento) {
        Block bloco = evento.getBlock();
        String dono = donoDaBase(bloco);
        if (dono == null) {
            return;
        }
        Player jogador = evento.getPlayer();
        if (partida == null || !partida.emJogo()) {
            // Fora de partida o bloco é mapa como qualquer outro: só quem está
            // construindo mexe nele.
            if (!torres.podeMexer(jogador)) {
                evento.setCancelled(true);
                jogador.sendActionBar(ChatColor.RED + "Esse e o bloco de um time.");
            }
            return;
        }
        String meu = timeDe(jogador);
        if (meu == null) {
            evento.setCancelled(true);
            return;
        }
        if (meu.equals(dono)) {
            evento.setCancelled(true);
            jogador.sendActionBar(ChatColor.RED + "Esse bloco e do seu time.");
            return;
        }
        Estado alvo = estado.get(dono);
        if (alvo == null || !alvo.emCasa) {
            return;
        }
        // Passa por cima do que os outros ouvintes diriam: este é o único bloco
        // do mapa que existe para ser quebrado.
        evento.setCancelled(false);
        evento.setDropItems(false);
        evento.setExpToDrop(0);
        alvo.emCasa = false;
        soltar(alvo, dono, bloco.getLocation().add(0.5, 0.5, 0.5));
        anunciar(cor(meu) + jogador.getName() + ChatColor.GRAY + " quebrou o bloco do time "
                + cor(dono) + dono + ChatColor.GRAY + ". Ele caiu no chao.");
    }

    /** Põe o bloco no chão marcado, brilhando, e sem prazo para sumir. */
    private void soltar(Estado alvo, String dono, Location onde) {
        if (onde.getWorld() == null) {
            return;
        }
        Item item = onde.getWorld().dropItem(onde, itemDo(dono));
        preparar(item);
        alvo.noChao = item;
    }

    /**
     * Um item caído some em cinco minutos, e some para valer na lava e no fogo.
     *
     * Qualquer um desses fins deixaria a partida sem o bloco que ela existe para
     * disputar. Ele fica onde caiu até alguém encostar nele.
     */
    private void preparar(Item item) {
        item.setPickupDelay(20);
        item.setInvulnerable(true);
        item.setPersistent(true);
        item.setGlowing(true);
        item.setTicksLived(1);
    }

    // -------------------------------------------------------------- pegada

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void aoPegar(EntityPickupItemEvent evento) {
        String dono = donoDo(evento.getItem().getItemStack());
        if (dono == null) {
            return;
        }
        Estado alvo = estado.get(dono);
        if (!(evento.getEntity() instanceof Player jogador)) {
            // Golem, aldeão, o que for: o bloco é coisa de gente.
            evento.setCancelled(true);
            return;
        }
        String meu = timeDe(jogador);
        if (meu == null) {
            evento.setCancelled(true);
            return;
        }
        if (dono.equals(meu)) {
            // O time do bloco encostou nele: volta para a base na hora, sem
            // passar pela mochila de ninguém. É o resgate.
            evento.setCancelled(true);
            evento.getItem().remove();
            if (alvo != null) {
                alvo.noChao = null;
                porNoLugar(alvo, dono);
            }
            anunciar(cor(dono) + jogador.getName() + ChatColor.GRAY
                    + " devolveu o bloco do time " + cor(dono) + dono + ChatColor.GRAY + ".");
            tocar(Sound.BLOCK_BEACON_DEACTIVATE);
            return;
        }
        if (alvo != null) {
            alvo.noChao = null;
        }
        anunciar(cor(meu) + jogador.getName() + ChatColor.GRAY + " esta com o bloco do time "
                + cor(dono) + dono + ChatColor.GRAY + ".");
        tocar(Sound.BLOCK_NOTE_BLOCK_PLING);
    }

    /** Largado de propósito: continua sendo o bloco da partida, e continua no chão. */
    @EventHandler(ignoreCancelled = true)
    public void aoLargar(PlayerDropItemEvent evento) {
        String dono = donoDo(evento.getItemDrop().getItemStack());
        if (dono == null) {
            return;
        }
        preparar(evento.getItemDrop());
        Estado alvo = estado.get(dono);
        if (alvo != null) {
            alvo.noChao = evento.getItemDrop();
        }
    }

    /**
     * Morrer carregando deixa o bloco no chão, onde o corpo caiu.
     *
     * Ele sai da lista de quedas do jogo e é solto por nós, porque o que o jogo
     * solta é um item comum: sumiria em cinco minutos, e a partida ficaria sem
     * bloco para disputar.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void aoMorrer(PlayerDeathEvent evento) {
        Player morto = evento.getEntity();
        for (String dono : donos()) {
            if (!tem(morto, dono)) {
                continue;
            }
            evento.getDrops().removeIf(item -> dono.equals(donoDo(item)));
            tirarDoInventario(morto, dono);
            Estado alvo = estado.get(dono);
            if (alvo != null) {
                soltar(alvo, dono, morto.getLocation());
            }
            anunciar(ChatColor.GRAY + "O bloco do time " + cor(dono) + dono + ChatColor.GRAY
                    + " caiu com " + morto.getName() + ".");
        }
    }

    /** Sair carregando vale o mesmo que morrer carregando: o bloco fica no mapa. */
    @EventHandler
    public void aoSair(PlayerQuitEvent evento) {
        Player jogador = evento.getPlayer();
        for (String dono : donos()) {
            if (!tem(jogador, dono)) {
                continue;
            }
            tirarDoInventario(jogador, dono);
            Estado alvo = estado.get(dono);
            if (alvo != null) {
                soltar(alvo, dono, jogador.getLocation());
            }
            anunciar(ChatColor.GRAY + "O bloco do time " + cor(dono) + dono + ChatColor.GRAY
                    + " ficou onde " + jogador.getName() + " saiu.");
        }
    }

    /** O bloco roubado não se coloca: ele só serve para ser levado. */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void aoColocar(BlockPlaceEvent evento) {
        if (donoDo(evento.getItemInHand()) == null) {
            return;
        }
        evento.setCancelled(true);
        evento.getPlayer().sendActionBar(ChatColor.RED
                + "O bloco roubado nao se coloca: leve ate o bloco do seu time.");
    }

    /**
     * E não entra em baú.
     *
     * Guardado, ele sairia do mapa sem sair da partida: ninguém carregando,
     * ninguém para matar, e nada no chão para resgatar. O relógio traria o bloco
     * de volta sozinho, mas depois de meio minuto de ninguém entender nada.
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void aoClicar(InventoryClickEvent evento) {
        if (donoDo(evento.getCurrentItem()) == null && donoDo(evento.getCursor()) == null) {
            return;
        }
        InventoryType onde = evento.getInventory().getType();
        if (onde == InventoryType.CRAFTING || onde == InventoryType.PLAYER) {
            return;
        }
        evento.setCancelled(true);
        if (evento.getWhoClicked() instanceof Player jogador) {
            jogador.sendActionBar(ChatColor.RED + "O bloco roubado nao entra em bau.");
        }
    }

    // ------------------------------------------------------------- relogio

    private void relogio() {
        if (partida == null || !partida.emJogo()) {
            return;
        }
        Set<UUID> agora = new HashSet<>();
        for (String dono : donos()) {
            Estado alvo = estado.get(dono);
            if (alvo == null || alvo.onde == null || alvo.emCasa) {
                continue;
            }
            Player carregador = acharCarregador(dono);
            if (carregador != null) {
                alvo.noChao = null;
                agora.add(carregador.getUniqueId());
                carregador.setGlowing(true);
                cuidarDoCarregador(carregador, dono, alvo);
                continue;
            }
            if (alvo.noChao != null && alvo.noChao.isValid()) {
                // Zerar a idade a cada volta é o que impede o item de sumir. O
                // prazo do jogo é de cinco minutos, e uma partida dura mais.
                alvo.noChao.setTicksLived(1);
                if (alvo.noChao.getLocation().getY() < alvo.noChao.getWorld().getMinHeight() + 1) {
                    alvo.noChao.remove();
                    alvo.noChao = null;
                    porNoLugar(alvo, dono);
                    anunciar(ChatColor.GRAY + "O bloco do time " + cor(dono) + dono
                            + ChatColor.GRAY + " caiu no vazio e voltou para a base.");
                }
                continue;
            }
            // Nem com alguém, nem no chão. Sobrou de algum caminho que ninguém
            // previu — um /clear, um baú, um mundo trocado. Volta para casa em
            // vez de deixar a partida sem alvo.
            alvo.noChao = null;
            porNoLugar(alvo, dono);
            anunciar(ChatColor.GRAY + "O bloco do time " + cor(dono) + dono + ChatColor.GRAY
                    + " sumiu do mapa e voltou para a base.");
        }
        for (UUID quem : brilhando) {
            if (agora.contains(quem)) {
                continue;
            }
            Player jogador = Bukkit.getPlayer(quem);
            if (jogador != null) {
                jogador.setGlowing(false);
            }
        }
        brilhando.clear();
        brilhando.addAll(agora);
    }

    /** Chegou no bloco do próprio time? Então pontuou. */
    private void cuidarDoCarregador(Player carregador, String dono, Estado alvo) {
        String meu = timeDe(carregador);
        if (meu == null || meu.equals(dono)) {
            // O time do bloco acabou com ele na mochila por algum caminho que
            // não passou pelo chão. Volta para casa, como se tivesse resgatado.
            tirarDoInventario(carregador, dono);
            porNoLugar(alvo, dono);
            anunciar(ChatColor.GRAY + "O bloco do time " + cor(dono) + dono + ChatColor.GRAY
                    + " voltou para a base.");
            return;
        }
        Estado meuBloco = estado.get(meu);
        if (meuBloco != null && !meuBloco.emCasa) {
            carregador.sendActionBar(ChatColor.RED
                    + "Seu bloco tambem foi roubado: recupere ele para poder pontuar.");
            return;
        }
        if (emCimaDoBloco(carregador, meu)) {
            tirarDoInventario(carregador, dono);
            carregador.setGlowing(false);
            porNoLugar(alvo, dono);
            if (partida != null) {
                partida.capturou(meu, carregador, dono);
            }
            return;
        }
        carregador.sendActionBar(ChatColor.GOLD + "Voce esta com o bloco do time " + dono
                + " - suba no bloco do seu time.");
    }

    /**
     * Em cima do próprio bloco, ou em cima do que o time construiu por cima dele.
     *
     * Só o mesmo x e z, e até cinco blocos acima: parado ao lado não vale, e
     * parado em cima da obsidiana que tapa o bloco vale, que é onde a pessoa
     * consegue ficar de pé.
     */
    private boolean emCimaDoBloco(Player jogador, String meu) {
        Estado meuBloco = estado.get(meu);
        if (meuBloco == null || meuBloco.onde == null || !meuBloco.emCasa) {
            return false;
        }
        Location onde = meuBloco.onde;
        if (onde.getWorld() == null || !onde.getWorld().equals(jogador.getWorld())) {
            return false;
        }
        Location pes = jogador.getLocation();
        if (pes.getBlockX() != onde.getBlockX() || pes.getBlockZ() != onde.getBlockZ()) {
            return false;
        }
        int subiu = pes.getBlockY() - onde.getBlockY();
        return subiu > 0 && subiu <= ALCANCE;
    }

    // -------------------------------------------------------------- apoios

    /** Repõe o bloco na base. */
    private void porNoLugar(Estado alvo, String dono) {
        alvo.emCasa = true;
        if (alvo.onde == null || alvo.onde.getWorld() == null) {
            return;
        }
        alvo.onde.getBlock().setType(dono.equals(nomeVermelho)
                ? Material.REDSTONE_BLOCK : Material.LAPIS_BLOCK);
    }

    private Player acharCarregador(String dono) {
        for (Player jogador : Bukkit.getOnlinePlayers()) {
            if (tem(jogador, dono)) {
                return jogador;
            }
        }
        return null;
    }

    private boolean tem(Player jogador, String dono) {
        for (ItemStack item : jogador.getInventory().getContents()) {
            if (dono.equals(donoDo(item))) {
                return true;
            }
        }
        return false;
    }

    private void tirarDoInventario(Player jogador, String dono) {
        ItemStack[] tudo = jogador.getInventory().getContents();
        for (int i = 0; i < tudo.length; i++) {
            if (dono.equals(donoDo(tudo[i]))) {
                jogador.getInventory().setItem(i, null);
            }
        }
    }

    private String timeDe(Player jogador) {
        Team time = jogador.getScoreboard().getEntryTeam(jogador.getName());
        return time == null ? null : time.getName();
    }

    private ChatColor cor(String time) {
        return time.equals(nomeVermelho) ? ChatColor.RED : ChatColor.BLUE;
    }

    private void anunciar(String recado) {
        Bukkit.broadcastMessage(recado);
    }

    private void tocar(Sound som) {
        for (Player jogador : Bukkit.getOnlinePlayers()) {
            jogador.playSound(jogador.getLocation(), som, 1f, 1f);
        }
    }
}
