#!/usr/bin/env bash
# Troca o modo do slot rotativo.
#
# A rede opera com BedWars fixo e um segundo modo que muda de tempos em tempos.
# Isso mantem novidade sem dividir a populacao: dois modos cheios valem mais que
# cinco vazios. O limite nao e memoria, e CPU — cada Paper precisa da thread
# principal dele a cada 50 ms, e proxy, lobby e dois modos ja ocupam 4 vCPU.
#
#   ./scripts/rotate.sh              # o que esta no ar
#   ./scripts/rotate.sh pillars      # coloca Pillars no slot rotativo

set -euo pipefail

repository_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
compose=(docker compose --env-file "$repository_root/infra/.env" -f "$repository_root/infra/compose.yaml")

fixed_mode="bedwars"
rotating_modes="pvp pillars buildbattle ctf"

usage() {
    cat >&2 <<TEXT
Uso: rotate.sh [modo]

  sem argumento    mostra o que esta no ar
  <modo>           para o rotativo atual e sobe esse no lugar

Modo fixo: $fixed_mode
Modos rotativos: $rotating_modes

O lobby e o $fixed_mode nao entram na rotacao: o lobby e a porta de entrada da
rede e o $fixed_mode e a atracao principal, que precisa estar sempre no ar.
TEXT
    exit 1
}

if [[ $# -eq 0 ]]; then
    echo "Servicos no ar:"
    "${compose[@]}" ps --services --status running 2>/dev/null || "${compose[@]}" ps
    exit 0
fi

[[ $# -eq 1 ]] || usage
target="$1"

if [[ "$target" == "$fixed_mode" ]]; then
    echo "$fixed_mode e o modo fixo e ja deve estar no ar; ele nao entra na rotacao." >&2
    exit 1
fi

if [[ " $rotating_modes " != *" $target "* ]]; then
    echo "Modo invalido: $target" >&2
    usage
fi

# Parar os outros rotativos antes de subir o novo: em 4 vCPU dois minigames
# cheios ja ocupam os nucleos, e um terceiro faria o MSPT subir em todos.
# Parar servico ja parado nao faz nada, entao nao precisa detectar qual esta no ar.
for mode in $rotating_modes; do
    [[ "$mode" == "$target" ]] && continue
    "${compose[@]}" stop "$mode" >/dev/null 2>&1 || true
done

echo "Subindo $target no slot rotativo..."
"${compose[@]}" up -d "$target"

echo
echo "Slot rotativo agora e $target."
echo "Acompanhe o boot ate aparecer 'Done':"
echo "  docker compose --env-file infra/.env -f infra/compose.yaml logs -f $target"
echo
echo "O mundo e as estatisticas dos outros modos continuam salvos em infra/runtime/."
