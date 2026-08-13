package br.com.gamescraft.menus;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Busca a textura de skin de uma conta direto na Mojang.
 *
 * Não usa o perfil que o servidor já tem por dois motivos, os dois descobertos
 * na prática: o servidor guarda perfil em cache, e quem trocou de skin há pouco
 * continuava vindo com a antiga; e o SkinsRestorer aplica a skin no proxy, de
 * onde o servidor de trás não enxerga.
 *
 * A leitura do JSON é por expressão regular de propósito. As duas respostas têm
 * três campos cada, e uma biblioteca a mais só para isso seria peso sem troco.
 */
final class Skins {

    private static final Pattern ID = Pattern.compile("\"id\"\\s*:\\s*\"([0-9a-fA-F]{32})\"");
    private static final Pattern TEXTURA = Pattern.compile(
            "\"name\"\\s*:\\s*\"textures\"\\s*,\\s*\"value\"\\s*:\\s*\"([A-Za-z0-9+/=]+)\"");

    private static final HttpClient CLIENTE = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private Skins() {
    }

    /** O valor base64 da textura, ou null se a conta não existe ou não tem skin. */
    static String buscar(String conta) throws Exception {
        String uuid = pegar("https://api.mojang.com/users/profiles/minecraft/" + conta, ID);
        if (uuid == null) {
            return null;
        }
        // unsigned=false pede a resposta assinada; e dela que sai o campo de
        // textura completo.
        return pegar("https://sessionserver.mojang.com/session/minecraft/profile/"
                + uuid + "?unsigned=false", TEXTURA);
    }

    private static String pegar(String endereco, Pattern padrao) throws Exception {
        HttpRequest pedido = HttpRequest.newBuilder(URI.create(endereco))
                .timeout(Duration.ofSeconds(10))
                .header("Cache-Control", "no-cache")
                .GET()
                .build();
        HttpResponse<String> resposta = CLIENTE.send(pedido, HttpResponse.BodyHandlers.ofString());
        if (resposta.statusCode() != 200) {
            return null;
        }
        Matcher achou = padrao.matcher(resposta.body());
        return achou.find() ? achou.group(1) : null;
    }
}
