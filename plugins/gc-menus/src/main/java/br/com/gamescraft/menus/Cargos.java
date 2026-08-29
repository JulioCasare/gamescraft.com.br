package br.com.gamescraft.menus;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

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
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

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
 * Aqui fica só o que o LuckPerms não faz: desenhar a tag nos três lugares onde
 * um nome aparece — o chat, a lista de tab e o rótulo que flutua sobre a
 * cabeça. Cada cargo dá uma permissão própria, e é por ela que este plugin
 * descobre quem é quem, sem depender da API do LuckPerms para nada.
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

    /** O último cargo visto em cada um, para saber quando ele venceu. */
    private final Map<UUID, Cargo> ultimoReal = new HashMap<>();

    /** Se a última ronda pegou partida rolando. */
    private boolean ultimaFase;

    /** Só existe no MegaGames. Nos outros servidores fica nulo, e a tag sempre aparece. */
    private Partida partida;

    Cargos(JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getScheduler().runTaskTimer(plugin, this::vigiar, 100L, 100L);
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

    // ------------------------------------------- etiqueta em cima da cabeça

    /**
     * A etiqueta acima do nome só existe por time de placar.
     *
     * Não há outro caminho no Minecraft: o nome que flutua sobre a cabeça não
     * aceita texto solto, só o prefixo do time em que a pessoa está. A lista de
     * tab vem junto de graça — é o mesmo prefixo, desenhado nos dois lugares —
     * e por isso o nome na tab não é mais escrito à mão, senão a etiqueta
     * apareceria duas vezes lá.
     *
     * Os times são numerados na ordem dos cargos porque a tab ordena por nome de
     * time: gc0gerente vem antes de gc1rei, e a lista sai em ordem de cargo sem
     * ninguém pedir.
     */
    private String nomeDoTime(Cargo cargo) {
        return cargo == null ? "gc9sem" : "gc" + cargo.ordinal() + cargo.chave();
    }

    private boolean nossa(String nomeDoTime) {
        return nomeDoTime.startsWith("gc") && nomeDoTime.length() > 2
                && Character.isDigit(nomeDoTime.charAt(2));
    }

    private Team time(Cargo cargo) {
        Scoreboard placar = Bukkit.getScoreboardManager().getMainScoreboard();
        String nome = nomeDoTime(cargo);
        Team time = placar.getTeam(nome);
        if (time == null) {
            time = placar.registerNewTeam(nome);
        }
        time.prefix(cargo == null ? Component.empty() : cargo.etiqueta());
        return time;
    }

    @EventHandler
    public void aoEntrar(PlayerJoinEvent evento) {
        // No tique seguinte: quem decide time e modo de jogo também mexe no
        // jogador na entrada, e o último a escrever é o que fica.
        plugin.getServer().getScheduler().runTask(plugin, () -> pintar(evento.getPlayer()));
    }

    /**
     * Quem sai também sai do time.
     *
     * A entrada num time é só o nome escrito, e ela fica no scoreboard.dat mesmo
     * com a pessoa offline. Se o cargo dela vencer enquanto está fora, ninguém
     * limpa: a ronda só olha quem está online. Tirar na saída fecha esse buraco,
     * e não custa nada — quem volta é repintado na entrada de qualquer jeito.
     */
    @EventHandler
    public void aoSair(PlayerQuitEvent evento) {
        ultimoReal.remove(evento.getPlayer().getUniqueId());
        Scoreboard placar = Bukkit.getScoreboardManager().getMainScoreboard();
        Team atual = placar.getEntryTeam(evento.getPlayer().getName());
        if (atual != null && nossa(atual.getName())) {
            atual.removeEntry(evento.getPlayer().getName());
        }
    }

    /** Redesenha todo mundo. */
    void pintarTodos() {
        for (Player jogador : Bukkit.getOnlinePlayers()) {
            pintar(jogador);
        }
    }

    void pintar(Player jogador) {
        if (!jogador.isOnline()) {
            return;
        }
        // A tab volta a ser só o nome: quem desenha a etiqueta ali agora é o
        // prefixo do time.
        jogador.playerListName(Component.text(jogador.getName()));

        Scoreboard placar = Bukkit.getScoreboardManager().getMainScoreboard();
        Team atual = placar.getEntryTeam(jogador.getName());
        Cargo cargo = emPartida() ? null : doJogador(jogador);
        if (cargo == null) {
            // Sai só dos times que são nossos. Em partida quem manda no nome é
            // o time do jogo — tirar a pessoa de lá apagaria a cor do time dela
            // justo quando ela mais precisa ser reconhecida.
            if (atual != null && nossa(atual.getName())) {
                atual.removeEntry(jogador.getName());
            }
            return;
        }
        if (atual != null && !nossa(atual.getName())) {
            return;
        }
        time(cargo).addEntry(jogador.getName());
    }

    // --------------------------------------------------------- vencimento

    /**
     * Olha de cinco em cinco segundos se o cargo de alguém mudou.
     *
     * O cargo com prazo vence sozinho lá no LuckPerms, e ninguém avisa este
     * plugin. Sem esta ronda a etiqueta ficava na tela até a pessoa sair e
     * entrar — o cargo tinha acabado e continuava aparecendo, que é o pior dos
     * dois mundos: não vale mais nada e ninguém sabe disso.
     *
     * Só redesenha quem mudou. Perguntar a permissão de cada um é barato;
     * reescrever placar de todo mundo a cada cinco segundos não é.
     */
    private void vigiar() {
        boolean emJogo = emPartida();
        boolean virouFase = emJogo != ultimaFase;
        ultimaFase = emJogo;
        for (Player jogador : Bukkit.getOnlinePlayers()) {
            Cargo real = doJogador(jogador);
            Cargo antes = ultimoReal.get(jogador.getUniqueId());
            boolean mudou = real != antes;
            if (mudou) {
                ultimoReal.put(jogador.getUniqueId(), real);
            }
            if (mudou || virouFase) {
                pintar(jogador);
            }
            if (mudou && antes != null && real == null) {
                jogador.sendMessage(ChatColor.YELLOW + "Seu cargo " + antes.rotulo()
                        + " venceu.");
            }
        }
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
        if (argumentos.length < 2 || argumentos.length > 3) {
            ajuda(quemPediu);
            return true;
        }
        String alvo = argumentos[1];
        String prazo = argumentos.length == 3 ? argumentos[2] : null;
        if (prazo != null && !prazoValido(prazo)) {
            quemPediu.sendMessage(ChatColor.RED + "Prazo estranho: " + prazo);
            ajuda(quemPediu);
            return true;
        }
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
        if (tirando && prazo != null) {
            quemPediu.sendMessage(ChatColor.RED + "Remover nao leva prazo: tirar e agora.");
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

        // Tira os quatro antes de dar o novo, nas duas formas: o LuckPerms
        // guarda cargo com prazo e cargo sem prazo em lugares diferentes, e
        // `parent remove` não encosta no que tem prazo. Sem o removetemp, quem
        // ganhou 30 dias e depois comprou o vitalício ficaria com os dois, e o
        // vitalício sumiria junto com o prêmio no fim do mês.
        for (Cargo outro : Cargo.values()) {
            console("lp user " + quem + " parent remove " + outro.chave());
            console("lp user " + quem + " parent removetemp " + outro.chave());
        }
        if (cargo != null && prazo == null) {
            console("lp user " + quem + " parent add " + cargo.chave());
        } else if (cargo != null) {
            console("lp user " + quem + " parent addtemp " + cargo.chave() + " " + prazo);
        }

        if (cargo == null) {
            quemPediu.sendMessage(ChatColor.GREEN + "Cargo de " + alvo + " removido.");
        } else {
            String ate = prazo == null ? " (sem prazo)" : " por " + prazo;
            quemPediu.sendMessage(ChatColor.GREEN + alvo + " agora e "
                    + ChatColor.RESET + cargo.rotulo() + ChatColor.GREEN + ate + ".");
            if (online != null) {
                online.sendMessage(ChatColor.GREEN + "Voce recebeu o cargo "
                        + cargo.rotulo() + (prazo == null ? "!" : " por " + prazo + "!"));
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

    /**
     * Prazo no formato do LuckPerms: número e letra, podendo emendar.
     *
     * Confere antes de mandar porque isto vira comando de console: um espaço no
     * meio viraria um argumento a mais, e argumento a mais em `lp user` muda o
     * que o comando faz.
     */
    private boolean prazoValido(String prazo) {
        if (prazo.isEmpty() || prazo.length() > 20 || !Character.isDigit(prazo.charAt(0))) {
            return false;
        }
        boolean temLetra = false;
        for (char c : prazo.toCharArray()) {
            if (Character.isDigit(c)) {
                continue;
            }
            if (c >= 'a' && c <= 'z') {
                temLetra = true;
                continue;
            }
            return false;
        }
        return temLetra;
    }

    private void console(String comando) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), comando);
    }

    private void ajuda(CommandSender quemPediu) {
        quemPediu.sendMessage(ChatColor.GRAY + "Use: /vip <cargo> <jogador> [prazo]");
        StringBuilder lista = new StringBuilder();
        for (Cargo cargo : Cargo.values()) {
            lista.append(cargo.chave()).append(", ");
        }
        quemPediu.sendMessage(ChatColor.GRAY + "Cargos: " + lista + "remover");
        quemPediu.sendMessage(ChatColor.GRAY + "Sem prazo o cargo e para sempre.");
        // O 'm' e minuto e o 'mo' e mes — essa e a letra que engana, e cargo
        // dado por engano com um minuto de prazo some antes de alguem notar.
        quemPediu.sendMessage(ChatColor.GRAY + "Prazo: 30d dias, 12h horas, 30m minutos, "
                + "3mo meses, 2w semanas, 1y ano. Da para emendar: 1mo15d.");
    }
}
