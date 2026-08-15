package br.com.gamescraft.menus;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

/**
 * O concreto das fronteiras nao se quebra.
 *
 * Ele e o desenho do mapa: mostra de qual torre e cada pedaco de chao. Um buraco
 * numa linha nao se conserta sozinho, e depois de meia hora de partida o mapa
 * ficaria remendado — por isso a linha inteira e intocavel, e nao so a coluna
 * das torres.
 *
 * A excecao e o concreto que o proprio jogador colocou dentro de um castelo:
 * quem poe tem de poder tirar, senao um bloco mal colocado vira permanente.
 */
final class Concreto implements Listener {

    private final Torres torres;
    private final Areas areas;

    Concreto(Torres torres, Areas areas) {
        this.torres = torres;
        this.areas = areas;
    }

    private boolean doMapa(Block bloco) {
        Material tipo = bloco.getType();
        boolean concreto = tipo == Material.GRAY_CONCRETE
                || tipo == Material.RED_CONCRETE
                || tipo == Material.BLUE_CONCRETE;
        return concreto && !areas.foiColocado(bloco);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void aoQuebrar(BlockBreakEvent evento) {
        // Em silêncio: o bloco que não quebra já diz o que precisa ser dito.
        if (doMapa(evento.getBlock()) && !torres.podeMexer(evento.getPlayer())) {
            evento.setCancelled(true);
        }
    }

    // Explosao nao tem dono na hora do estouro: os blocos saem da lista do que
    // vai pelos ares, e nao ha /obras que passe por cima disso.
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void aoExplodirEntidade(EntityExplodeEvent evento) {
        evento.blockList().removeIf(this::doMapa);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void aoExplodirBloco(BlockExplodeEvent evento) {
        evento.blockList().removeIf(this::doMapa);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void aoQueimar(BlockBurnEvent evento) {
        if (doMapa(evento.getBlock())) {
            evento.setCancelled(true);
        }
    }
}
