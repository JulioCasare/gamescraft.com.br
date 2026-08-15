package br.com.gamescraft.menus;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Team;

/**
 * Os itens da loja que fazem algo que o jogo sozinho não faz: a bola de fogo que
 * se atira, a TNT que acende ao ser posta, a torre que se levanta inteira, o
 * golem com prazo de validade e a invisibilidade que esconde também a armadura.
 *
 * Todos se reconhecem por uma marca gravada no item pela loja, e não pelo nome:
 * nome se copia numa bigorna, marca não.
 */
final class Especiais implements Listener {

    /** Altura do poço da torre, sem contar a plataforma que a cobre. */
    private static final int ALTURA = 6;

    /** Quanto tempo o golem comprado fica de pé. */
    private static final int SEGUNDOS_DO_GOLEM = 300;

    /** Quinze corações. O de fábrica tem cinquenta, e não morria nunca. */
    private static final double VIDA_DO_GOLEM = 30.0;

    private final JavaPlugin plugin;
    private final Torres torres;
    private final Areas areas;
    private final NamespacedKey marca;
    private final String timeVermelho;

    /** A armadura guardada de quem está invisível, para devolver depois. */
    private final Map<UUID, ItemStack[]> escondidas = new HashMap<>();

    Especiais(JavaPlugin plugin, Torres torres, Areas areas, NamespacedKey marca) {
        this.plugin = plugin;
        this.torres = torres;
        this.areas = areas;
        this.marca = marca;
        this.timeVermelho = plugin.getConfig().getString("time-vermelho", "Vermelho");

        // Quem some tem de reaparecer quando a poção acaba, e o fim de um efeito
        // não avisa ninguém: só olhando de tempos em tempos.
        plugin.getServer().getScheduler().runTaskTimer(plugin, this::conferirInvisiveis, 20L, 20L);
    }

    private String truqueDe(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return null;
        }
        return item.getItemMeta().getPersistentDataContainer().get(marca, PersistentDataType.STRING);
    }

    private void gastarUm(Player jogador, ItemStack item) {
        if (jogador.getGameMode() == GameMode.CREATIVE) {
            return;
        }
        item.setAmount(item.getAmount() - 1);
    }

    // ------------------------------------------------------- bola de fogo

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void aoUsar(PlayerInteractEvent evento) {
        if (evento.getAction() != Action.RIGHT_CLICK_AIR && evento.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        ItemStack naMao = evento.getItem();
        String truque = truqueDe(naMao);
        if (truque == null) {
            return;
        }
        Player jogador = evento.getPlayer();
        if ("BOLA_DE_FOGO".equals(truque)) {
            evento.setCancelled(true);
            atirar(jogador);
            gastarUm(jogador, naMao);
        } else if ("TORRE".equals(truque) && evento.getClickedBlock() != null) {
            evento.setCancelled(true);
            if (levantarTorre(jogador, evento.getClickedBlock())) {
                gastarUm(jogador, naMao);
            }
        }
    }

    /**
     * A bola sai da mira, rápida e sem cair: é assim que ela chega longe.
     *
     * A de ghast anda devagar e a gente vê chegando; multiplicando a velocidade
     * ela atravessa o vão entre duas torres antes de o outro lado reagir, que é
     * o que faz dela uma arma de ataque e não de defesa.
     */
    private void atirar(Player jogador) {
        Location olho = jogador.getEyeLocation();
        Fireball bola = jogador.getWorld().spawn(olho.clone().add(olho.getDirection()), Fireball.class);
        bola.setShooter(jogador);
        bola.setDirection(olho.getDirection());
        bola.setVelocity(olho.getDirection().multiply(2.2));
        bola.setIsIncendiary(true);
        bola.setYield(3.0f);
        jogador.getWorld().playSound(jogador.getLocation(), Sound.ENTITY_GHAST_SHOOT, 1.4f, 1f);
    }

    /** TNT da loja acende sozinha, com pavio curto: a graça dela é não dar tempo. */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void aoPor(BlockPlaceEvent evento) {
        if (!"TNT_AUTOMATICA".equals(truqueDe(evento.getItemInHand()))) {
            return;
        }
        Block bloco = evento.getBlock();
        Location onde = bloco.getLocation().add(0.5, 0, 0.5);
        evento.setCancelled(true);
        bloco.setType(Material.AIR);
        TNTPrimed tnt = bloco.getWorld().spawn(onde, TNTPrimed.class);
        tnt.setFuseTicks(40);
        tnt.setSource(evento.getPlayer());
        gastarUm(evento.getPlayer(), evento.getItemInHand());
    }

    // -------------------------------------------------------------- golem

    /**
     * O golem comprado tem quinze corações e cinco minutos de vida.
     *
     * O de fábrica tem cinquenta corações e não morre nunca: um só deles trancava
     * um castelo pelo resto da partida, e a defesa deixava de custar atenção.
     */
    @EventHandler
    public void aoNascerGolem(CreatureSpawnEvent evento) {
        if (!(evento.getEntity() instanceof IronGolem golem)
                || evento.getSpawnReason() != CreatureSpawnEvent.SpawnReason.SPAWNER_EGG) {
            return;
        }
        golem.getAttribute(Attribute.MAX_HEALTH).setBaseValue(VIDA_DO_GOLEM);
        golem.setHealth(VIDA_DO_GOLEM);
        golem.setCustomName(ChatColor.GRAY + "Golem (5 min)");
        golem.setCustomNameVisible(true);
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (golem.isValid()) {
                golem.getWorld().spawnParticle(org.bukkit.Particle.CLOUD,
                        golem.getLocation().add(0, 1, 0), 30, 0.5, 1, 0.5, 0.02);
                golem.remove();
            }
        }, SEGUNDOS_DO_GOLEM * 20L);
    }

    // ------------------------------------------------------ invisibilidade

    /**
     * Sumir de verdade: a armadura sai do corpo enquanto a poção dura.
     *
     * O jogo esconde o corpo mas deixa a armadura no ar, e um peitoral flutuando
     * denuncia mais do que o corpo inteiro. Ela volta sozinha quando o efeito
     * acaba — ou no primeiro golpe levado, que é o preço de estar escondido:
     * quem é achado deixa de ser invisível.
     */
    @EventHandler
    public void aoBeber(PlayerItemConsumeEvent evento) {
        if (!"INVISIBILIDADE".equals(truqueDe(evento.getItem()))) {
            return;
        }
        Player jogador = evento.getPlayer();
        plugin.getServer().getScheduler().runTask(plugin, () -> esconderArmadura(jogador));
    }

    private void esconderArmadura(Player jogador) {
        if (escondidas.containsKey(jogador.getUniqueId())) {
            return;
        }
        ItemStack[] pecas = jogador.getInventory().getArmorContents();
        escondidas.put(jogador.getUniqueId(), pecas);
        jogador.getInventory().setArmorContents(new ItemStack[4]);
        jogador.sendActionBar(ChatColor.GRAY + "Invisível. Levar um golpe acaba com isso.");
    }

    /** Devolve a armadura de quem estava invisível. Nunca deixa a peça sumir. */
    private void devolverArmadura(Player jogador) {
        ItemStack[] pecas = escondidas.remove(jogador.getUniqueId());
        if (pecas == null) {
            return;
        }
        jogador.getInventory().setArmorContents(pecas);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void aoLevarDano(EntityDamageEvent evento) {
        if (!(evento.getEntity() instanceof Player jogador)
                || !escondidas.containsKey(jogador.getUniqueId())) {
            return;
        }
        jogador.removePotionEffect(PotionEffectType.INVISIBILITY);
        devolverArmadura(jogador);
        jogador.sendActionBar(ChatColor.YELLOW + "Você apareceu.");
    }

    private void conferirInvisiveis() {
        for (UUID quem : Map.copyOf(escondidas).keySet()) {
            Player jogador = plugin.getServer().getPlayer(quem);
            if (jogador == null || !jogador.isOnline()) {
                escondidas.remove(quem);
                continue;
            }
            if (!jogador.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                devolverArmadura(jogador);
            }
        }
    }

    /** Sair invisível não pode custar a armadura: ela volta antes de a sessão fechar. */
    @EventHandler
    public void aoSair(PlayerQuitEvent evento) {
        devolverArmadura(evento.getPlayer());
    }

    // -------------------------------------------------------------- torre

    /**
     * Levanta a torre inteira: poço oco de três por três com escada dentro, e
     * uma plataforma de cinco por cinco no topo.
     *
     * Só ocupa lugar vazio. Bloco que já existe fica onde está — a torre serve
     * para subir depressa numa disputa, não para engolir o que estava lá.
     */
    private boolean levantarTorre(Player jogador, Block base) {
        World mundo = base.getWorld();
        Material la = ehVermelho(jogador) ? Material.RED_WOOL : Material.BLUE_WOOL;
        int x = base.getX();
        int y = base.getY() + 1;
        int z = base.getZ();

        if (torres.protegido(base) && !torres.podeMexer(jogador)) {
            jogador.sendActionBar(ChatColor.RED + "Aqui não dá para levantar a torre.");
            return false;
        }

        BlockData escada = Material.LADDER.createBlockData();
        ((Directional) escada).setFacing(BlockFace.SOUTH);

        int postos = 0;
        for (int altura = 0; altura < ALTURA; altura++) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dz == 0) {
                        continue;
                    }
                    postos += por(mundo, x + dx, y + altura, z + dz, la.createBlockData()) ? 1 : 0;
                }
            }
            // A escada encosta na parede do norte, então olha para o sul.
            postos += por(mundo, x, y + altura, z, escada) ? 1 : 0;
        }
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                postos += por(mundo, x + dx, y + ALTURA, z + dz, la.createBlockData()) ? 1 : 0;
            }
        }
        jogador.playSound(jogador.getLocation(), Sound.BLOCK_WOOL_PLACE, 1f, 1f);
        jogador.sendActionBar(ChatColor.GREEN + "Torre levantada: " + postos + " blocos.");
        return true;
    }

    private boolean por(World mundo, int x, int y, int z, BlockData dado) {
        Block bloco = mundo.getBlockAt(x, y, z);
        if (!bloco.getType().isAir() && bloco.getType() != Material.WATER) {
            return false;
        }
        if (torres.protegido(bloco)) {
            return false;
        }
        bloco.setBlockData(dado, false);
        // Fica na lista de blocos postos por jogador: dentro de um castelo, o
        // que a torre levanta tem de poder ser quebrado como qualquer outro.
        areas.registrarColocado(bloco);
        return true;
    }

    private boolean ehVermelho(Player jogador) {
        Team time = jogador.getScoreboard().getEntryTeam(jogador.getName());
        return time != null && timeVermelho.equals(time.getName());
    }

    /** Usado só para não deixar entidade solta na varredura de depuração. */
    static boolean ehGolemComprado(Entity entidade) {
        return entidade instanceof IronGolem golem && golem.getCustomName() != null;
    }
}
