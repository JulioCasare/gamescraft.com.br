package br.com.gamescraft.menus;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Team;

/**
 * Os três itens da loja que fazem algo que o jogo sozinho não faz: a bola de
 * fogo que se atira, a TNT que acende ao ser posta e a torre que se levanta
 * inteira de uma vez.
 *
 * Todos se reconhecem por uma marca gravada no item pela loja, e não pelo nome:
 * nome se copia numa bigorna, marca não.
 */
final class Especiais implements Listener {

    /** Altura do poço da torre, sem contar a plataforma que a cobre. */
    private static final int ALTURA = 6;

    private final JavaPlugin plugin;
    private final Torres torres;
    private final Areas areas;
    private final NamespacedKey marca;
    private final String timeVermelho;

    Especiais(JavaPlugin plugin, Torres torres, Areas areas, NamespacedKey marca) {
        this.plugin = plugin;
        this.torres = torres;
        this.areas = areas;
        this.marca = marca;
        this.timeVermelho = plugin.getConfig().getString("time-vermelho", "Vermelho");
    }

    private String truqueDe(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return null;
        }
        return item.getItemMeta().getPersistentDataContainer()
                .get(marca, PersistentDataType.STRING);
    }

    private void gastarUm(Player jogador, ItemStack item) {
        if (jogador.getGameMode() == org.bukkit.GameMode.CREATIVE) {
            return;
        }
        item.setAmount(item.getAmount() - 1);
    }

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
     * A bola de fogo sai da mira e não do peito, e sem gravidade: assim ela vai
     * para onde a pessoa está olhando, que é como se joga com ela.
     */
    private void atirar(Player jogador) {
        Fireball bola = jogador.launchProjectile(Fireball.class, jogador.getLocation().getDirection());
        bola.setIsIncendiary(true);
        bola.setYield(2.5f);
        bola.setShooter(jogador);
        jogador.getWorld().playSound(jogador.getLocation(), Sound.ENTITY_GHAST_SHOOT, 1f, 1f);
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
            jogador.sendActionBar(ChatColor.RED + "Nao da para levantar torre em cima de uma torre do mapa.");
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
                    postos += por(mundo, x + dx, y + altura, z + dz, la) ? 1 : 0;
                }
            }
            // A escada encosta na parede do norte, então olha para o sul.
            postos += por(mundo, x, y + altura, z, escada) ? 1 : 0;
        }
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                postos += por(mundo, x + dx, y + ALTURA, z + dz, la) ? 1 : 0;
            }
        }
        jogador.playSound(jogador.getLocation(), Sound.BLOCK_WOOL_PLACE, 1f, 1f);
        jogador.sendActionBar(ChatColor.GREEN + "Torre levantada: " + postos + " blocos.");
        return true;
    }

    private boolean por(World mundo, int x, int y, int z, Material tipo) {
        return por(mundo, x, y, z, tipo.createBlockData());
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

    /** Some com a etiqueta da loja quando o item vira bloco comum. */
    static void limparMarca(ItemStack item) {
        ItemMeta dados = item.getItemMeta();
        if (dados != null) {
            item.setItemMeta(dados);
        }
    }
}
