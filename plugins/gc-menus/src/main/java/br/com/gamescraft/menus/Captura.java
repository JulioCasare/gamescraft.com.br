package br.com.gamescraft.menus;


import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Team;

/**
 * A disputa das torres: quem fica em cima delas toma o sinalizador para o seu
 * time, e o chão em volta muda de cor junto.
 *
 * Cinco segundos em cima tomam a torre, venha ela de neutra ou do outro time.
 * Passar pelo neutro no meio parecia mais justo, mas na prática obrigava quem
 * estava tomando a sair e voltar para completar.
 */
final class Captura {

    private enum Dono {
        NEUTRO, VERMELHO, AZUL
    }

    private record Torre(int x, int z) {
    }

    private final JavaPlugin plugin;
    private final Torres torres;
    private final Map<Torre, Dono> donos = new HashMap<>();
    private final Map<Torre, Integer> progresso = new HashMap<>();
    private final Map<Torre, Integer> alturas = new HashMap<>();

    private final String timeVermelho;
    private final String timeAzul;
    private final int segundos;
    private final int raio;
    private final String nomeMundo;

    Captura(JavaPlugin plugin, Torres torres) {
        this.plugin = plugin;
        this.torres = torres;
        this.timeVermelho = plugin.getConfig().getString("time-vermelho", "Vermelho");
        this.timeAzul = plugin.getConfig().getString("time-azul", "Azul");
        this.segundos = plugin.getConfig().getInt("segundos-para-tomar", 5);
        this.raio = plugin.getConfig().getInt("raio-de-captura", 6);
        this.nomeMundo = plugin.getConfig().getString("mundo", "ilha");
        // Uma volta por segundo: o tempo de tomada e contado em segundos, e
        // olhar mais vezes so gastaria conta para o mesmo resultado.
        plugin.getServer().getScheduler().runTaskTimer(plugin, this::passo, 20L, 20L);
    }

    private void passo() {
        World mundo = plugin.getServer().getWorld(nomeMundo);
        if (mundo == null || Bukkit.getOnlinePlayers().isEmpty()) {
            return;
        }
        for (int[] pos : torres.posicoes()) {
            // Castelo nao se toma: ele e a base do time, e a cor dele vem do
            // vidro que o Julio poe sobre o proprio sinalizador.
            if (emCastelo(pos[0], pos[1])) {
                continue;
            }
            Torre torre = new Torre(pos[0], pos[1]);
            int alturaBeacon = alturaDoBeacon(mundo, torre);
            if (alturaBeacon < 0) {
                continue;
            }
            Location centro = new Location(mundo, torre.x() + 0.5, alturaBeacon, torre.z() + 0.5);
            int vermelhos = 0;
            int azuis = 0;
            for (Player jogador : mundo.getPlayers()) {
                if (jogador.getLocation().distanceSquared(centro) > raio * raio) {
                    continue;
                }
                String time = timeDe(jogador);
                if (timeVermelho.equals(time)) {
                    vermelhos++;
                } else if (timeAzul.equals(time)) {
                    azuis++;
                }
            }
            // Com os dois times em cima, ninguem avanca: a torre fica trancada
            // ate um lado limpar o outro.
            if ((vermelhos > 0) == (azuis > 0)) {
                progresso.put(torre, 0);
                continue;
            }
            Dono quemEsta = vermelhos > 0 ? Dono.VERMELHO : Dono.AZUL;
            Dono dono = donos.getOrDefault(torre, Dono.NEUTRO);
            if (dono == quemEsta) {
                progresso.put(torre, 0);
                continue;
            }
            int conta = progresso.getOrDefault(torre, 0) + 1;
            if (conta < segundos) {
                progresso.put(torre, conta);
                continue;
            }
            progresso.put(torre, 0);
            // Toma direto, mesmo se a torre era do inimigo. A versao anterior
            // passava pelo neutro primeiro, e na pratica isso obrigava a sair e
            // voltar para completar a tomada.
            Dono novo = quemEsta;
            donos.put(torre, novo);
            pintar(mundo, torre, alturaBeacon, novo);
            anunciar(torre, novo, quemEsta);
        }
    }

    /** Quantas torres aquele time tem agora. Castelo nao conta: ele nao se toma. */
    int quantasDoTime(String nomeDoTime) {
        Dono procurado = nomeDoTime.equals(timeVermelho) ? Dono.VERMELHO : Dono.AZUL;
        int total = 0;
        for (int[] pos : torres.posicoes()) {
            if (emCastelo(pos[0], pos[1])) {
                continue;
            }
            if (donos.getOrDefault(new Torre(pos[0], pos[1]), Dono.NEUTRO) == procurado) {
                total++;
            }
        }
        return total;
    }

    /** As duas areas de castelo, em planta. */
    private boolean emCastelo(int x, int z) {
        for (String linha : plugin.getConfig().getStringList("areas-construcao")) {
            String[] p = linha.split(",");
            if (p.length < 6) {
                continue;
            }
            int minx = Math.min(Integer.parseInt(p[0].trim()), Integer.parseInt(p[3].trim()));
            int maxx = Math.max(Integer.parseInt(p[0].trim()), Integer.parseInt(p[3].trim()));
            int minz = Math.min(Integer.parseInt(p[2].trim()), Integer.parseInt(p[5].trim()));
            int maxz = Math.max(Integer.parseInt(p[2].trim()), Integer.parseInt(p[5].trim()));
            if (x >= minx && x <= maxx && z >= minz && z <= maxz) {
                return true;
            }
        }
        return false;
    }

    /**
     * Poe a vidraca da cor do time sobre os sinalizadores de cada castelo, e
     * marca essas torres como do dono.
     *
     * A cor vem do arquivo, e nao do sinalizador do centro como antes: um dos
     * castelos nao tem sinalizador nenhum, e nao ha de onde ler.
     *
     * O chao do castelo nao e mais pintado: o concreto e o desenho do mapa e
     * deixou de mudar de cor.
     */
    void pintarCastelos(org.bukkit.command.CommandSender quemPediu) {
        World mundo = plugin.getServer().getWorld(nomeMundo);
        if (mundo == null) {
            return;
        }
        int quantos = 0;
        for (String linha : plugin.getConfig().getStringList("areas-construcao")) {
            String[] p = linha.split(",");
            if (p.length < 7) {
                continue;
            }
            int minx = Math.min(Integer.parseInt(p[0].trim()), Integer.parseInt(p[3].trim()));
            int maxx = Math.max(Integer.parseInt(p[0].trim()), Integer.parseInt(p[3].trim()));
            int minz = Math.min(Integer.parseInt(p[2].trim()), Integer.parseInt(p[5].trim()));
            int maxz = Math.max(Integer.parseInt(p[2].trim()), Integer.parseInt(p[5].trim()));
            Dono dono = switch (p[6].trim().toLowerCase()) {
                case "vermelho" -> Dono.VERMELHO;
                case "azul" -> Dono.AZUL;
                default -> Dono.NEUTRO;
            };
            for (int[] pos : torres.posicoes()) {
                if (pos[0] < minx || pos[0] > maxx || pos[1] < minz || pos[1] > maxz) {
                    continue;
                }
                Torre torre = new Torre(pos[0], pos[1]);
                donos.put(torre, dono);
                int alturaBeacon = acharBeacon(mundo, torre);
                if (alturaBeacon >= 0) {
                    mundo.getBlockAt(torre.x(), alturaBeacon + 1, torre.z()).setType(corDoVidro(dono));
                }
            }
            quantos++;
        }
        quemPediu.sendMessage(ChatColor.GREEN + "Castelos pintados: " + quantos);
    }

    private Material corDoVidro(Dono dono) {
        return switch (dono) {
            case VERMELHO -> Material.RED_STAINED_GLASS_PANE;
            case AZUL -> Material.BLUE_STAINED_GLASS_PANE;
            case NEUTRO -> Material.AIR;
        };
    }

    /**
     * Devolve ao neutro todas as torres menos as dos castelos, e o chao ao
     * cinza.
     *
     * A varredura do chao ficou aqui mesmo depois de a captura parar de pintar:
     * e o que desfaz de uma vez as manchas vermelhas e azuis que as tomadas
     * antigas deixaram. Fora deste comando, concreto nao muda mais de cor.
     */
    void zerar(org.bukkit.command.CommandSender quemPediu) {
        World mundo = plugin.getServer().getWorld(nomeMundo);
        if (mundo == null) {
            return;
        }
        int contagem = 0;
        for (int[] pos : torres.posicoes()) {
            if (emCastelo(pos[0], pos[1])) {
                continue;
            }
            Torre torre = new Torre(pos[0], pos[1]);
            donos.put(torre, Dono.NEUTRO);
            progresso.put(torre, 0);
            int alturaBeacon = acharBeacon(mundo, torre);
            if (alturaBeacon >= 0) {
                mundo.getBlockAt(torre.x(), alturaBeacon + 1, torre.z()).setType(Material.AIR);
            }
            contagem++;
        }
        int limpos = contagem;
        varrerOChao(quemPediu, mundo, limpos);
    }

    /**
     * Esquece de quem era cada torre, sem encostar no mundo.
     *
     * É o que o fim de partida precisa. O /neutro completo varre o mapa inteiro
     * para despintar o chão, e no fim da partida esse mapa está sendo apagado no
     * mesmo instante — a varredura escrevia blocos num mundo que já estava
     * saindo do ar, e o trabalho era jogado fora de qualquer jeito, porque a
     * próxima partida nasce do zip.
     */
    void zerarMemoria() {
        for (int[] pos : torres.posicoes()) {
            if (emCastelo(pos[0], pos[1])) {
                continue;
            }
            Torre torre = new Torre(pos[0], pos[1]);
            donos.put(torre, Dono.NEUTRO);
            progresso.put(torre, 0);
        }
    }

    private void varrerOChao(org.bukkit.command.CommandSender quemPediu, World mundo, int limpos) {
        int alcance = plugin.getConfig().getInt("alcance-da-varredura", 288);
        quemPediu.sendMessage(ChatColor.GRAY + "Devolvendo o chao ao cinza...");
        new org.bukkit.scheduler.BukkitRunnable() {
            int x = -alcance;

            @Override
            public void run() {
                // O mundo ainda é o mesmo de quando isto começou?
                //
                // Esta varredura dura uns vinte tiques e escreve blocos o tempo
                // todo. Se a arena for fechada ou trocada no meio — fim de
                // partida, /setup, servidor desligando — o que sobra é uma
                // referência para um mundo morto: cada chamada estoura, e o que
                // estava em voo pega a gravação dos arquivos de região no meio.
                // Foi assim que o r.0.0.mca da ilha quebrou e voltou a quebrar
                // a cada partida, levando chunks junto de cada vez.
                if (plugin.getServer().getWorld(nomeMundo) != mundo) {
                    cancel();
                    return;
                }
                // Trinta fileiras por tique: a ilha inteira de uma vez segura o
                // servidor por segundos.
                for (int feitas = 0; feitas < 30; feitas++) {
                    if (x > alcance) {
                        quemPediu.sendMessage(ChatColor.GREEN + "Torres neutras: " + limpos);
                        cancel();
                        return;
                    }
                    for (int z = -alcance; z <= alcance; z++) {
                        Block topo = mundo.getHighestBlockAt(x, z);
                        Material tipo = topo.getType();
                        if (tipo != Material.RED_CONCRETE && tipo != Material.BLUE_CONCRETE) {
                            continue;
                        }
                        int[] dono = torreMaisPerto(x, z);
                        if (dono == null || emCastelo(dono[0], dono[1])) {
                            continue;
                        }
                        topo.setType(Material.GRAY_CONCRETE, false);
                    }
                    x++;
                }
            }
        }.runTaskTimer(plugin, 1L, 1L);
    }

    private int[] torreMaisPerto(int x, int z) {
        int[] melhor = null;
        long menor = Long.MAX_VALUE;
        for (int[] pos : torres.posicoes()) {
            long dx = x - pos[0];
            long dz = z - pos[1];
            long dist = dx * dx + dz * dz;
            if (dist < menor) {
                menor = dist;
                melhor = pos;
            }
        }
        return melhor;
    }

    private String timeDe(Player jogador) {
        Team time = jogador.getScoreboard().getEntryTeam(jogador.getName());
        return time == null ? null : time.getName();
    }

    /**
     * A altura guardada, conferida antes de ser usada.
     *
     * Guardar sem conferir ja custou caro: uma copia do mapa 150 blocos acima
     * fez a busca achar o sinalizador da copia, e a altura errada ficou na
     * memoria mesmo depois de a copia ser apagada. Uma leitura de bloco por
     * torre por segundo e barata, e o valor errado se corrige sozinho.
     */
    private int alturaDoBeacon(World mundo, Torre torre) {
        Integer guardada = alturas.get(torre);
        if (guardada != null && guardada >= 0
                && mundo.getBlockAt(torre.x(), guardada, torre.z()).getType() == Material.BEACON) {
            return guardada;
        }
        int achada = acharBeacon(mundo, torre);
        alturas.put(torre, achada);
        return achada;
    }

    /**
     * Procura o sinalizador na coluna da torre, de cima para baixo, comecando
     * abaixo de onde qualquer copia de seguranca possa estar: o backup do /save
     * mora bem mais alto, e sem esse teto a busca acharia o sinalizador dele.
     */
    private int acharBeacon(World mundo, Torre torre) {
        int teto = Math.min(plugin.getConfig().getInt("altura-maxima-da-torre", 150),
                mundo.getMaxHeight() - 1);
        for (int y = teto; y >= mundo.getMinHeight(); y--) {
            if (mundo.getBlockAt(torre.x(), y, torre.z()).getType() == Material.BEACON) {
                return y;
            }
        }
        return -1;
    }

    /**
     * Troca a cor do feixe da torre.
     *
     * O feixe muda com uma vidraça colorida em cima do sinalizador — é assim que
     * o jogo colore beacon, não há outro jeito.
     *
     * O chão não é mais repintado. Ele era varrido a cada tomada, sessenta
     * blocos para cada lado, e além de custar caro deixava o mapa manchado: o
     * concreto é o desenho das fronteiras, e desenho não deveria mudar de dono.
     * Quem tem a torre se lê no feixe.
     */
    private void pintar(World mundo, Torre torre, int alturaBeacon, Dono dono) {
        Block vidro = mundo.getBlockAt(torre.x(), alturaBeacon + 1, torre.z());
        vidro.setType(corDoVidro(dono));
    }

    private void anunciar(Torre torre, Dono novo, Dono quemTomou) {
        String onde = " (" + torre.x() + ", " + torre.z() + ")";
        String recado = switch (novo) {
            case VERMELHO -> ChatColor.RED + "O time vermelho tomou uma torre" + onde;
            case AZUL -> ChatColor.BLUE + "O time azul tomou uma torre" + onde;
            case NEUTRO -> (quemTomou == Dono.VERMELHO ? ChatColor.RED : ChatColor.BLUE)
                    + "Uma torre voltou a ser neutra" + onde;
        };
        Bukkit.broadcastMessage(recado);
    }
}
