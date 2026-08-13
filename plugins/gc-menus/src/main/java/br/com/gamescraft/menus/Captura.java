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
            int alturaBeacon = alturas.computeIfAbsent(torre, t -> acharBeacon(mundo, t));
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

    /** As duas areas de castelo, em planta. */
    private boolean emCastelo(int x, int z) {
        for (String linha : plugin.getConfig().getStringList("areas-construcao")) {
            String[] p = linha.split(",");
            if (p.length != 6) {
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
     * Pinta o chao dos castelos com a cor do vidro que esta sobre o sinalizador
     * de cada um. E o Julio quem escolhe a cor, pondo o vidro; aqui so se copia.
     */
    void pintarCastelos(org.bukkit.command.CommandSender quemPediu) {
        World mundo = plugin.getServer().getWorld(nomeMundo);
        if (mundo == null) {
            return;
        }
        int quantos = 0;
        for (int[] pos : torres.posicoes()) {
            if (!emCastelo(pos[0], pos[1])) {
                continue;
            }
            Torre torre = new Torre(pos[0], pos[1]);
            int alturaBeacon = acharBeacon(mundo, torre);
            if (alturaBeacon < 0) {
                continue;
            }
            Material vidro = mundo.getBlockAt(torre.x(), alturaBeacon + 1, torre.z()).getType();
            Dono dono = switch (vidro) {
                case RED_STAINED_GLASS_PANE -> Dono.VERMELHO;
                case BLUE_STAINED_GLASS_PANE -> Dono.AZUL;
                default -> Dono.NEUTRO;
            };
            donos.put(torre, dono);
            pintar(mundo, torre, alturaBeacon, dono);
            quantos++;
        }
        quemPediu.sendMessage(ChatColor.GREEN + "Castelos repintados: " + quantos);
    }

    /**
     * Devolve ao neutro todas as torres menos as dos castelos.
     *
     * Varre a ilha uma vez só, em vez de repintar torre por torre: cada torre
     * repintada custa uma varredura de 120 por 120, e quarenta e seis delas
     * seriam a mesma conta feita quarenta e seis vezes.
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
        int alcance = plugin.getConfig().getInt("alcance-da-varredura", 288);
        quemPediu.sendMessage(ChatColor.GRAY + "Devolvendo o chao ao cinza...");
        new org.bukkit.scheduler.BukkitRunnable() {
            int x = -alcance;

            @Override
            public void run() {
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

    /** Procura o sinalizador na coluna da torre. */
    private int acharBeacon(World mundo, Torre torre) {
        for (int y = mundo.getMaxHeight() - 1; y >= mundo.getMinHeight(); y--) {
            if (mundo.getBlockAt(torre.x(), y, torre.z()).getType() == Material.BEACON) {
                return y;
            }
        }
        return -1;
    }

    /**
     * Pinta o feixe e o chão da área daquela torre.
     *
     * O feixe muda com um vidro colorido em cima do sinalizador — é assim que o
     * jogo colore beacon, não há outro jeito. O chão é repintado varrendo o que
     * está em volta e trocando só o concreto cuja torre mais próxima é esta: a
     * linha de fronteira é dividida entre as duas torres que ela separa.
     */
    private void pintar(World mundo, Torre torre, int alturaBeacon, Dono dono) {
        Block vidro = mundo.getBlockAt(torre.x(), alturaBeacon + 1, torre.z());
        vidro.setType(switch (dono) {
            case VERMELHO -> Material.RED_STAINED_GLASS_PANE;
            case AZUL -> Material.BLUE_STAINED_GLASS_PANE;
            case NEUTRO -> Material.AIR;
        });

        Material cor = switch (dono) {
            case VERMELHO -> Material.RED_CONCRETE;
            case AZUL -> Material.BLUE_CONCRETE;
            case NEUTRO -> Material.GRAY_CONCRETE;
        };
        int alcance = plugin.getConfig().getInt("alcance-da-pintura", 60);
        for (int x = torre.x() - alcance; x <= torre.x() + alcance; x++) {
            for (int z = torre.z() - alcance; z <= torre.z() + alcance; z++) {
                Block topo = mundo.getHighestBlockAt(x, z);
                if (!ehConcreto(topo.getType()) || topo.getType() == cor) {
                    continue;
                }
                if (maisPerto(x, z) != torre.hashCode()) {
                    continue;
                }
                topo.setType(cor, false);
            }
        }
    }

    private boolean ehConcreto(Material material) {
        return material == Material.GRAY_CONCRETE
                || material == Material.RED_CONCRETE
                || material == Material.BLUE_CONCRETE;
    }

    /** O hash da torre mais próxima daquele ponto, para comparar sem alocar. */
    private int maisPerto(int x, int z) {
        int melhor = 0;
        long menor = Long.MAX_VALUE;
        for (int[] pos : torres.posicoes()) {
            long dx = x - pos[0];
            long dz = z - pos[1];
            long dist = dx * dx + dz * dz;
            if (dist < menor) {
                menor = dist;
                melhor = new Torre(pos[0], pos[1]).hashCode();
            }
        }
        return melhor;
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
