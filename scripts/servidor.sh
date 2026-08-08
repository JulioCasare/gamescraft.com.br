#!/usr/bin/env bash
# Equivalente Linux de servidor.ps1, para uso no VPS.
# Uso:  ./scripts/servidor.sh status
#       ./scripts/servidor.sh ligar <modo>
#       ./scripts/servidor.sh desligar <modo>
# Modos: lobby bedwars pvp pillars buildbattle ctf eventos
#
# Trava: no maximo 6 servidores Minecraft ligados ao mesmo tempo. O setimo
# estoura a memoria (testado em 2026-08-07: corrompeu arquivos e derrubou tudo).

set -euo pipefail

modos=(lobby bedwars pvp pillars buildbattle ctf eventos)
maximo=6

repositorio="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
compose() {
    docker compose --env-file "$repositorio/infra/.env" -f "$repositorio/infra/compose.yaml" "$@"
}

ligados() {
    local nomes
    nomes="$(docker ps --format '{{.Names}}' 2>/dev/null || true)"
    for m in "${modos[@]}"; do
        grep -qx "game-craft-beta-$m-1" <<< "$nomes" && echo "$m"
    done
}

acao="${1:-status}"
modo="${2:-}"

if [[ "$acao" == "status" ]]; then
    echo "Servidores da rede Game Craft:"
    on="$(ligados)"
    total=0
    for m in "${modos[@]}"; do
        if grep -qx "$m" <<< "$on"; then
            printf '  %-12s LIGADO\n' "$m"; total=$((total+1))
        else
            printf '  %-12s desligado\n' "$m"
        fi
    done
    echo "Ligados: $total de ${#modos[@]} (maximo: $maximo)"
    exit 0
fi

if [[ "$acao" != "ligar" && "$acao" != "desligar" ]]; then
    echo "Acao desconhecida: $acao. Use: status, ligar <modo> ou desligar <modo>." >&2
    exit 1
fi

valido=""
for m in "${modos[@]}"; do [[ "$m" == "$modo" ]] && valido=1; done
if [[ -z "$valido" ]]; then
    echo "Informe um modo valido: ${modos[*]}" >&2
    exit 1
fi

on="$(ligados)"
qtd="$(grep -c . <<< "$on" || true)"

if [[ "$acao" == "ligar" ]]; then
    if grep -qx "$modo" <<< "$on"; then
        echo "O $modo ja esta ligado."
        exit 0
    fi
    if (( qtd >= maximo )); then
        echo "NAO DA: ja tem $qtd servidores ligados, o maximo que a memoria aguenta." >&2
        echo "Desligue um primeiro: ./scripts/servidor.sh desligar <modo>" >&2
        echo "Ligados agora: $(tr '\n' ' ' <<< "$on")" >&2
        exit 1
    fi
    echo "Ligando o $modo..."
    compose up -d "$modo"
    echo "Pronto. O boot leva 1-2 minutos."
else
    if ! grep -qx "$modo" <<< "$on"; then
        echo "O $modo ja esta desligado."
        exit 0
    fi
    [[ "$modo" == "lobby" ]] && echo "Aviso: sem o lobby, ninguem novo conecta."
    echo "Desligando o $modo (nada se perde)..."
    compose stop "$modo"
    echo "Pronto."
fi
