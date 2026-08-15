package br.com.gamescraft.menus;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * A armadura comprada na loja não se perde na morte.
 *
 * Sem isto, cada morte devolvia o couro do kit e apagava trinta diamantes de
 * peitoral — e quem morre uma vez perdia a partida inteira. É a mesma regra do
 * Bed Wars, e pelo mesmo motivo: a armadura é progresso do time, e progresso
 * não se apaga num golpe de sorte.
 *
 * Fica em arquivo, e não só na memória: o servidor reinicia com jogo em
 * andamento mais vezes do que se gostaria.
 */
final class Armaduras {

    private static final String[] LUGARES = { "cabeca", "peito", "pernas", "pes" };

    private final JavaPlugin plugin;
    private final File arquivo;
    private final YamlConfiguration guardadas;

    Armaduras(JavaPlugin plugin) {
        this.plugin = plugin;
        this.arquivo = new File(plugin.getDataFolder(), "armaduras.yml");
        this.guardadas = YamlConfiguration.loadConfiguration(arquivo);
    }

    /** Em qual das quatro gavetas aquela peça mora. */
    private String lugarDe(ItemStack peca) {
        String tipo = peca.getType().name();
        if (tipo.endsWith("_HELMET")) {
            return "cabeca";
        }
        if (tipo.endsWith("_CHESTPLATE")) {
            return "peito";
        }
        if (tipo.endsWith("_LEGGINGS")) {
            return "pernas";
        }
        if (tipo.endsWith("_BOOTS")) {
            return "pes";
        }
        return null;
    }

    void guardar(Player jogador, ItemStack peca) {
        String lugar = lugarDe(peca);
        if (lugar == null) {
            return;
        }
        guardadas.set(jogador.getUniqueId() + "." + lugar, peca);
        salvar();
    }

    /**
     * Veste o que a pessoa já comprou, por cima do que o kit acabou de dar.
     *
     * Roda depois do kit de propósito: assim quem nunca comprou nada continua
     * de couro, e quem comprou não perde nada.
     */
    void vestir(Player jogador) {
        PlayerInventory mochila = jogador.getInventory();
        for (String lugar : LUGARES) {
            ItemStack peca = guardadas.getItemStack(jogador.getUniqueId() + "." + lugar);
            if (peca == null) {
                continue;
            }
            switch (lugar) {
                case "cabeca" -> mochila.setHelmet(peca);
                case "peito" -> mochila.setChestplate(peca);
                case "pernas" -> mochila.setLeggings(peca);
                default -> mochila.setBoots(peca);
            }
        }
    }

    /** Apaga o que todo mundo comprou. Serve para recomeçar uma partida. */
    int limparTudo() {
        int quantos = guardadas.getKeys(false).size();
        for (String chave : guardadas.getKeys(false)) {
            guardadas.set(chave, null);
        }
        salvar();
        return quantos;
    }

    private void salvar() {
        try {
            guardadas.save(arquivo);
        } catch (IOException erro) {
            plugin.getLogger().warning("Nao consegui guardar as armaduras compradas: " + erro.getMessage());
        }
    }
}
