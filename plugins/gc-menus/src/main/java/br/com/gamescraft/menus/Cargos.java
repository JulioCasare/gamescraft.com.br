package br.com.gamescraft.menus;

import java.util.Locale;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import io.papermc.paper.event.player.AsyncChatEvent;

/**
 * A tag que vem antes do nome: Gerente, Rei, Cavaleiro, Escudeiro.
 *
 * Quem guarda de quem é cada cargo é o LuckPerms, e não este plugin. Não é
 * preguiça: o cargo tem de valer na rede inteira, e o LuckPerms já fala com o
 * banco compartilhado — dar o cargo no lobby e ele aparecer no Bed Wars é de
 * graça por esse caminho. E quando o VIP passar a durar 30 ou 90 dias, o prazo
 * também já existe lá, sem código novo.
 *
 * Aqui fica só o que o LuckPerms não faz: desenhar a tag no chat e na lista de
 * jogadores. Cada cargo dá uma permissão própria, e é por ela que este plugin
 * descobre quem é quem — sem depender da API do LuckPerms para nada.
 */
final class Cargos implements Listener {

    /**
     * Os quatro cargos, do mais alto para o mais baixo.
     *
     * A ordem é a da lista, e é ela que decide quem aparece quando alguém tem
     * mais de um: o primeiro que casar vence. Gerente na frente porque cargo de
     * quem trabalha na rede vale mais que cargo comprado — quem é Gerente e Rei
     * aparece como Gerente.
     */
    enum Cargo {
        GERENTE("Gerente", NamedTextColor.RED),
        REI("Rei", NamedTextColor.GOLD),
        CAVALEIRO("Cavaleiro", NamedTextColor.AQUA),
        ESCUDEIRO("Escudeiro", NamedTextColor.GREEN);

        private final String rotulo;
        private final NamedTextColor cor;

        Cargo(String rotulo, NamedTextColor cor) {
            this.rotulo = rotulo;
            this.cor = cor;
        }

        String rotulo() {
            return rotulo;
        }

        /** O nome do grupo no LuckPerms, e o fim da permissão. Tudo minúsculo. */
        String chave() {
            return name().toLowerCase(Locale.ROOT);
        }

        String permissao() {
            return "gcmenus.cargo." + chave();
        }

        Component etiqueta() {
            return Component.text("[" + rotulo + "] ", cor).decoration(TextDecoration.BOLD, false);
        }

        static Cargo porNome(String nome) {
            for (Cargo cargo : values()) {
                if (cargo.chave().equalsIgnoreCase(nome)) {
                    return cargo;
                }
            }
            return null;
        }
    }

    private final JavaPlugin plugin;

    /** Só existe no MegaGames. Nos outros servidores fica nulo, e a tag sempre aparece. */
    private Partida partida;

    Cargos(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    void ligarPartida(Partida partida) {
        this.partida = partida;
    }

    /**
     * Durante a partida ninguém tem tag.
     *
     * Dentro do jogo o nome serve para dizer de que time a pessoa é, e nada
     * mais: uma etiqueta dourada ao lado do nome disputa a atenção justo na hora
     * em que ela precisa ser lida rápido. E há um motivo prático — quem colore
     * nome em partida é o time, e duas coisas escrevendo no mesmo lugar brigam.
     */
    private boolean emPartida() {
        return partida != null && partida.emJogo();
    }

    /** O cargo mais alto da pessoa, ou nulo se ela não tem nenhum. */
    Cargo doJogador(Player jogador) {
        for (Cargo cargo : Cargo.values()) {
            if (jogador.hasPermission(cargo.permissao())) {
                return cargo;
            }
        }
        return null;
    }

    // ---------------------------------------------------------------- chat

    @EventHandler(priority = EventPriority.NORMAL)
    public void aoFalar(AsyncChatEvent evento) {
        Cargo cargo = emPartida() ? null : doJogador(evento.getPlayer());
        evento.renderer((quemFala, nome, mensagem, publico) -> {
            Component linha = cargo == null ? Component.empty() : cargo.etiqueta();
            return linha.append(nome)
                    .append(Component.text(": ", NamedTextColor.GRAY))
                    .append(mensagem);
        });
    }

    // -------------------------------------------------------- lista de tab

    @EventHandler
    public void aoEntrar(PlayerJoinEvent evento) {
        // No tique seguinte: quem decide time e modo de jogo também mexe no
        // jogador na entrada, e o último a escrever é o que fica.
        plugin.getServer().getScheduler().runTask(plugin, () -> pintar(evento.getPlayer()));
    }

    /** Redesenha a lista de jogadores de todo mundo. */
    void pintarTodos() {
        for (Player jogador : Bukkit.getOnlinePlayers()) {
            pintar(jogador);
        }
    }

    void pintar(Player jogador) {
        if (!jogador.isOnline()) {
            return;
        }
        Cargo cargo = emPartida() ? null : doJogador(jogador);
        if (cargo == null) {
            jogador.playerListName(Component.text(jogador.getName()));
            return;
        }
        jogador.playerListName(cargo.etiqueta().append(Component.text(jogador.getName())));
    }

    // ------------------------------------------------------------- comando

    /**
     * O /vip &lt;cargo&gt; &lt;pessoa&gt;.
     *
     * Um cargo por pessoa: dar Rei a quem já é Cavaleiro tira o Cavaleiro. Sem
     * isso alguém que subisse de nível ficaria com os dois grupos e herdaria a
     * soma das permissões — e um dia iria querer voltar ao Cavaleiro e não
     * conseguiria, porque o Rei continuaria lá por baixo.
     */
    boolean comando(CommandSender quemPediu, String[] argumentos) {
        if (argumentos.length != 2) {
            ajuda(quemPediu);
            return true;
        }
        String alvo = argumentos[1];
        if (!nomeValido(alvo)) {
            quemPediu.sendMessage(ChatColor.RED + "Isso nao parece um nome de jogador.");
            return true;
        }
        boolean tirando = argumentos[0].equalsIgnoreCase("remover")
                || argumentos[0].equalsIgnoreCase("nenhum");
        Cargo cargo = tirando ? null : Cargo.porNome(argumentos[0]);
        if (!tirando && cargo == null) {
            quemPediu.sendMessage(ChatColor.RED + "Cargo desconhecido: " + argumentos[0]);
            ajuda(quemPediu);
            return true;
        }

        Player online = Bukkit.getPlayerExact(alvo);
        // Pelo UUID, e não pelo nome.
        //
        // O LuckPerms só resolve nome de quem ele já viu, e num banco recém
        // criado isso é ninguém: dar cargo a alguém offline respondia "usuário
        // não encontrado" e não fazia nada. Com o UUID ele cria o registro na
        // hora, e o cargo já está lá quando a pessoa entrar.
        String quem = null;
        if (online != null) {
            quem = online.getUniqueId().toString();
        } else {
            org.bukkit.OfflinePlayer guardado = Bukkit.getOfflinePlayerIfCached(alvo);
            if (guardado != null) {
                quem = guardado.getUniqueId().toString();
            }
        }
        if (quem == null) {
            quemPediu.sendMessage(ChatColor.RED + "Nao conheco ninguem chamado " + alvo + ".");
            quemPediu.sendMessage(ChatColor.GRAY + "O cargo so pode ser dado a quem ja entrou "
                    + "na rede pelo menos uma vez.");
            return true;
        }

        // Tira os quatro antes de dar o novo. O LuckPerms nao reclama de tirar
        // grupo que a pessoa nao tem, entao nao ha o que conferir primeiro.
        for (Cargo outro : Cargo.values()) {
            console("lp user " + quem + " parent remove " + outro.chave());
        }
        if (cargo != null) {
            console("lp user " + quem + " parent add " + cargo.chave());
        }

        if (cargo == null) {
            quemPediu.sendMessage(ChatColor.GREEN + "Cargo de " + alvo + " removido.");
        } else {
            quemPediu.sendMessage(ChatColor.GREEN + alvo + " agora e "
                    + ChatColor.RESET + cargo.rotulo() + ChatColor.GREEN + ".");
            if (online != null) {
                online.sendMessage(ChatColor.GREEN + "Voce recebeu o cargo "
                        + cargo.rotulo() + "!");
            }
        }
        if (online != null) {
            // A permissao so chega no jogador depois que o LuckPerms termina de
            // aplicar. Meio segundo cobre isso sem precisar perguntar a ele.
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> pintar(online), 10L);
        } else {
            quemPediu.sendMessage(ChatColor.GRAY + "Ele esta offline: a tag aparece "
                    + "quando entrar, em qualquer servidor da rede.");
        }
        return true;
    }

    /**
     * Nome de jogador, e nada mais.
     *
     * O que vem daqui vai virar comando de console do LuckPerms. Um nome com
     * espaço viraria dois argumentos, e um argumento a mais em `lp user` muda o
     * que o comando faz.
     */
    private boolean nomeValido(String nome) {
        if (nome.length() > 16 || nome.isEmpty()) {
            return false;
        }
        for (char c : nome.toCharArray()) {
            if (!Character.isLetterOrDigit(c) && c != '_' && c != '.') {
                return false;
            }
        }
        return true;
    }

    private void console(String comando) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), comando);
    }

    private void ajuda(CommandSender quemPediu) {
        quemPediu.sendMessage(ChatColor.GRAY + "Use: /vip <cargo> <jogador>");
        StringBuilder lista = new StringBuilder();
        for (Cargo cargo : Cargo.values()) {
            lista.append(cargo.chave()).append(", ");
        }
        quemPediu.sendMessage(ChatColor.GRAY + "Cargos: " + lista + "remover");
    }
}
