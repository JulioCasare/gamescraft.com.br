#!/usr/bin/env bash
# Opera o servidor de eventos: aquele que voce mesmo conduz, tipo lava sobe.
#
# Todo evento roda sobre uma COPIA LIMPA do mapa. Lava, explosao e bloco
# quebrado destroem o mapa durante a partida; sem a copia, na semana seguinte
# voce abriria as ruinas do evento anterior.
#
# Os mapas-modelo ficam em infra/runtime/event-maps/<nome>/ e nunca sao tocados.
#
#   ./scripts/event.sh mapas
#   ./scripts/event.sh abrir --mapa lava-sobe --liberar bedwars --liberar pvp
#   ./scripts/event.sh fechar --liberar bedwars --liberar pvp

set -euo pipefail

repository_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
compose=(docker compose --env-file "$repository_root/infra/.env" -f "$repository_root/infra/compose.yaml")

runtime_root="$repository_root/infra/runtime"
maps_root="$runtime_root/event-maps"
event_data="$runtime_root/eventos"

# O lobby fica de fora de proposito: o proxy depende dele e o `try` do Velocity
# aponta para ele. Parar o lobby derruba a rede inteira.
daily_modes="bedwars pillars buildbattle ctf pvp"

usage() {
    cat >&2 <<'TEXT'
Uso: event.sh <abrir|fechar|mapas> [opcoes]

  mapas                  lista os mapas-modelo disponiveis
  abrir                  sobe o servidor de eventos
  fechar                 para o servidor de eventos

Opcoes:
  --mapa <nome>          reinicia o evento com uma copia limpa desse mapa
  --liberar <modo>       para (ao abrir) e devolve (ao fechar) um modo do dia
                         a dia, liberando RAM. Pode repetir.

Modos que podem ser liberados: bedwars, pillars, buildbattle, ctf, pvp
TEXT
    exit 1
}

is_daily_mode() {
    [[ " $daily_modes " == *" $1 "* ]]
}

[[ $# -ge 1 ]] || usage
action="$1"
shift

map=""
release=()

while [[ $# -gt 0 ]]; do
    case "$1" in
        --mapa)
            map="${2:?--mapa precisa do nome de um mapa}"
            shift 2
            ;;
        --liberar)
            release+=("${2:?--liberar precisa do nome de um modo}")
            shift 2
            ;;
        *)
            echo "Opcao desconhecida: $1" >&2
            usage
            ;;
    esac
done

for mode in "${release[@]+"${release[@]}"}"; do
    is_daily_mode "$mode" || { echo "Modo invalido para --liberar: $mode" >&2; usage; }
done

case "$action" in
    mapas)
        if [[ ! -d "$maps_root" ]] || [[ -z "$(ls -A "$maps_root" 2>/dev/null)" ]]; then
            echo "Nenhum mapa em $maps_root"
            echo
            echo "Coloque a pasta do mundo la dentro, com o nome do evento:"
            echo "  $maps_root/lava-sobe/   (contendo level.dat, region/, ...)"
            exit 0
        fi
        echo "Mapas disponiveis:"
        for entry in "$maps_root"/*/; do
            [[ -d "$entry" ]] || continue
            name="$(basename "$entry")"
            size="$(du -sh "$entry" 2>/dev/null | cut -f1)"
            printf '  %-24s %s\n' "$name" "$size"
        done
        ;;

    abrir)
        if [[ -n "$map" ]]; then
            source_map="$maps_root/$map"
            if [[ ! -d "$source_map" ]]; then
                echo "Mapa nao encontrado: $source_map" >&2
                echo "Veja os disponiveis com: ./scripts/event.sh mapas" >&2
                exit 1
            fi
            if [[ ! -f "$source_map/level.dat" ]]; then
                echo "Aviso: $source_map nao tem level.dat. Confirme que e a pasta do mundo." >&2
            fi

            # Copiar mundo com o servidor no ar corrompe os arquivos de regiao.
            echo "Parando o servidor de eventos antes de trocar o mapa..."
            "${compose[@]}" stop eventos >/dev/null 2>&1 || true

            echo "Restaurando copia limpa de '$map'..."
            mkdir -p "$event_data"
            rm -rf "$event_data/world" "$event_data/world_nether" "$event_data/world_the_end"
            cp -a "$source_map" "$event_data/world"
            chown -R 1000:1000 "$event_data/world" 2>/dev/null || true
        fi

        for mode in "${release[@]+"${release[@]}"}"; do
            echo "Parando $mode para liberar memoria..."
            "${compose[@]}" stop "$mode"
        done

        if [[ ${#release[@]} -eq 0 ]] && command -v free >/dev/null 2>&1; then
            available_mb="$(free -m | awk '/^Mem:/ {print $7}')"
            if [[ -n "$available_mb" && "$available_mb" -lt 2600 ]]; then
                echo "Aviso: apenas ${available_mb} MB disponiveis." >&2
                echo "O servidor de eventos pede ~2,6 GB reais. Use --liberar <modo>." >&2
            fi
        fi

        echo "Subindo o servidor de eventos..."
        "${compose[@]}" up -d eventos
        echo
        echo "Servidor de eventos no ar. Espere aparecer 'Done' antes de chamar o pessoal:"
        echo "  docker compose --env-file infra/.env -f infra/compose.yaml logs -f eventos"
        echo "No jogo, mande os jogadores para o servidor 'eventos' pelo lobby."
        ;;

    fechar)
        echo "Parando o servidor de eventos..."
        "${compose[@]}" stop eventos

        for mode in "${release[@]+"${release[@]}"}"; do
            echo "Devolvendo $mode..."
            "${compose[@]}" up -d "$mode"
        done

        echo
        echo "Evento encerrado."
        echo "O mapa usado continua em infra/runtime/eventos/world ate o proximo --mapa."
        echo "Os modelos em infra/runtime/event-maps/ nao foram alterados."
        ;;

    *)
        usage
        ;;
esac
