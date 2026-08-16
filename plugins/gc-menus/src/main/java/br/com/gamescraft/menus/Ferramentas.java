package br.com.gamescraft.menus;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * A picareta, o machado e a tesoura ficam com o jogador — mas cada morte cobra
 * um material.
 *
 * A permanência é o que faz a compra valer: ferramenta que some na primeira
 * morte é dinheiro jogado fora, e sem picareta ninguém chega no bloco do
 * inimigo. O degrau para trás é o que impede que ela seja um presente eterno —
 * morrer custa alguma coisa, só não custa tudo.
 *
 * A escada segue o preço da loja, que é onde a força de cada material já está
 * medida: o ouro fica entre a pedra e o ferro, porque cava rápido e dura pouco.
 * A madeira é o chão, e é o que o kit já dá de graça.
 */
final class Ferramentas {

    private static final List<Material> PICARETAS = List.of(
            Material.WOODEN_PICKAXE, Material.STONE_PICKAXE, Material.GOLDEN_PICKAXE,
            Material.IRON_PICKAXE, Material.DIAMOND_PICKAXE);

    private static final List<Material> MACHADOS = List.of(
            Material.WOODEN_AXE, Material.STONE_AXE, Material.GOLDEN_AXE,
            Material.IRON_AXE, Material.DIAMOND_AXE);

    private final JavaPlugin plugin;
    private final File arquivo;
    private final YamlConfiguration guardadas;

    Ferramentas(JavaPlugin plugin) {
        this.plugin = plugin;
        this.arquivo = new File(plugin.getDataFolder(), "ferramentas.yml");
        this.guardadas = YamlConfiguration.loadConfiguration(arquivo);
    }

    private List<Material> escadaDe(Material tipo) {
        if (PICARETAS.contains(tipo)) {
            return PICARETAS;
        }
        if (MACHADOS.contains(tipo)) {
            return MACHADOS;
        }
        return null;
    }

    private String gavetaDe(Material tipo) {
        if (PICARETAS.contains(tipo)) {
            return "picareta";
        }
        if (MACHADOS.contains(tipo)) {
            return "machado";
        }
        return tipo == Material.SHEARS ? "tesoura" : null;
    }

    /** Guarda a compra. Comprar pior que o que ja se tem nao rebaixa nada. */
    void guardar(Player jogador, ItemStack ferramenta) {
        Material tipo = ferramenta.getType();
        String gaveta = gavetaDe(tipo);
        if (gaveta == null) {
            return;
        }
        String chave = jogador.getUniqueId() + "." + gaveta;
        List<Material> escada = escadaDe(tipo);
        if (escada != null) {
            Material atual = lerMaterial(chave);
            if (atual != null && escada.indexOf(atual) >= escada.indexOf(tipo)) {
                return;
            }
        }
        guardadas.set(chave, tipo.name());
        salvar();
    }

    /** Devolve o que a pessoa tem direito, depois do kit. */
    void dar(Player jogador) {
        Material picareta = lerMaterial(jogador.getUniqueId() + ".picareta");
        if (picareta != null) {
            // O kit ja poe a de madeira no lugar 1; esta troca por cima.
            jogador.getInventory().setItem(1, semDesgaste(new ItemStack(picareta)));
        }
        Material machado = lerMaterial(jogador.getUniqueId() + ".machado");
        if (machado != null) {
            jogador.getInventory().addItem(semDesgaste(new ItemStack(machado)));
        }
        if (lerMaterial(jogador.getUniqueId() + ".tesoura") != null) {
            jogador.getInventory().addItem(semDesgaste(new ItemStack(Material.SHEARS)));
        }
    }

    /**
     * Marca o item como indestrutivel.
     *
     * Vale para tudo o que se equipa neste jogo, e nao so para o que sai da
     * loja: a ferramenta que volta depois da morte e o kit de entrada sao itens
     * novos, e sem esta passagem nasciam gastaveis. O desgaste so criava uma
     * segunda moeda invisivel — a pessoa voltava ao castelo no meio da disputa
     * por causa de uma barrinha, e isso nao decidia nada.
     */
    static ItemStack semDesgaste(ItemStack item) {
        if (item == null || item.getType().getMaxDurability() <= 0) {
            return item;
        }
        ItemMeta dados = item.getItemMeta();
        if (dados != null) {
            dados.setUnbreakable(true);
            item.setItemMeta(dados);
        }
        return item;
    }

    /**
     * Cada morte tira um degrau da picareta e do machado.
     *
     * A tesoura nao tem material abaixo dela, entao fica — e nao ha por que
     * tirar do jogador a unica coisa que corta la depressa.
     *
     * Chegando na madeira, para: dali para baixo nao existe ferramenta, e o kit
     * ja daria a de madeira de qualquer jeito.
     */
    String rebaixar(Player jogador) {
        String contou = "";
        contou += descer(jogador, "picareta", PICARETAS, "picareta");
        contou += descer(jogador, "machado", MACHADOS, "machado");
        return contou;
    }

    private String descer(Player jogador, String gaveta, List<Material> escada, String rotulo) {
        String chave = jogador.getUniqueId() + "." + gaveta;
        Material atual = lerMaterial(chave);
        if (atual == null) {
            return "";
        }
        int degrau = escada.indexOf(atual);
        if (degrau <= 0) {
            return "";
        }
        Material abaixo = escada.get(degrau - 1);
        if (abaixo == escada.get(0)) {
            // Voltou para a madeira, que o kit ja da: nao ha o que guardar.
            guardadas.set(chave, null);
        } else {
            guardadas.set(chave, abaixo.name());
        }
        salvar();
        return " " + rotulo + " -> " + nomeCurto(abaixo);
    }

    private String nomeCurto(Material tipo) {
        String nome = tipo.name();
        if (nome.startsWith("WOODEN_")) {
            return "madeira";
        }
        if (nome.startsWith("STONE_")) {
            return "pedra";
        }
        if (nome.startsWith("GOLDEN_")) {
            return "ouro";
        }
        if (nome.startsWith("IRON_")) {
            return "ferro";
        }
        return "diamante";
    }

    private Material lerMaterial(String chave) {
        String nome = guardadas.getString(chave);
        return nome == null ? null : Material.matchMaterial(nome);
    }

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
            plugin.getLogger().warning("Nao consegui guardar as ferramentas compradas: " + erro.getMessage());
        }
    }
}
