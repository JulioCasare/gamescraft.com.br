#!/usr/bin/env bash
# Abre e fecha um modo para evento semanal.
#
# Em servidor pequeno a RAM nao sobra: ligar mais um Paper sem liberar espaco
# faz o kernel matar um servidor no meio da partida. Por isso --liberar para
# um modo do dia a dia enquanto o evento roda, e o devolve no fechamento.
#
#   ./scripts/event.sh abrir buildbattle --liberar pvp
#   ./scripts/event.sh fechar buildbattle --liberar pvp

set -euo pipefail

repository_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
compose=(docker compose --env-file "$repository_root/infra/.env" -f "$repository_root/infra/compose.yaml")

# O lobby fica de fora de proposito: o proxy depende dele e o `try` do Velocity
# aponta para ele. Parar o lobby derruba a rede inteira.
modes_available="bedwars pillars buildbattle ctf pvp"

usage() {
    cat >&2 <<'TEXT'
Uso: event.sh <abrir|fechar> <modo> [--liberar <modo>]

  abrir    sobe o modo do evento
  fechar   para o modo do evento

  --liberar <modo>   para (ao abrir) e devolve (ao fechar) um modo do dia a dia,
                     liberando RAM para o evento

Modos: bedwars, pillars, buildbattle, ctf, pvp
TEXT
    exit 1
}

is_valid_mode() {
    [[ " $modes_available " == *" $1 "* ]]
}

[[ $# -ge 2 ]] || usage
action="$1"
mode="$2"
shift 2

release=""
while [[ $# -gt 0 ]]; do
    case "$1" in
        --liberar)
            release="${2:?--liberar precisa do nome de um modo}"
            shift 2
            ;;
        *)
            echo "Opcao desconhecida: $1" >&2
            usage
            ;;
    esac
done

is_valid_mode "$mode" || { echo "Modo desconhecido: $mode" >&2; usage; }
if [[ -n "$release" ]]; then
    is_valid_mode "$release" || { echo "Modo desconhecido: $release" >&2; usage; }
    [[ "$release" != "$mode" ]] || { echo "O modo liberado nao pode ser o proprio evento." >&2; exit 1; }
fi

case "$action" in
    abrir)
        if [[ -n "$release" ]]; then
            echo "Parando $release para liberar memoria..."
            "${compose[@]}" stop "$release"
        elif command -v free >/dev/null 2>&1; then
            available_mb="$(free -m | awk '/^Mem:/ {print $7}')"
            if [[ -n "$available_mb" && "$available_mb" -lt 2000 ]]; then
                echo "Aviso: apenas ${available_mb} MB disponiveis." >&2
                echo "Um Paper novo pede ~1,3 GB reais. Use --liberar <modo> para abrir espaco." >&2
            fi
        fi

        echo "Subindo $mode..."
        "${compose[@]}" up -d "$mode"
        echo
        echo "Evento aberto em $mode."
        echo "Acompanhe o boot ate aparecer 'Done':"
        echo "  docker compose --env-file infra/.env -f infra/compose.yaml logs -f $mode"
        ;;

    fechar)
        echo "Parando $mode..."
        "${compose[@]}" stop "$mode"

        if [[ -n "$release" ]]; then
            echo "Devolvendo $release..."
            "${compose[@]}" up -d "$release"
        fi

        echo
        echo "Evento encerrado. O mundo e as estatisticas continuam em infra/runtime/$mode."
        ;;

    *)
        usage
        ;;
esac
