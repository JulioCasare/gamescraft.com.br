#!/bin/sh
# Avisa no Discord quando um servidor cai — e quando volta.
#
# Ele nao fala enquanto esta tudo bem. Canal que recebe mensagem de "tudo certo"
# a cada minuto vira ruido, e ruido a gente para de ler; quando o aviso de
# verdade chegar, ninguem vai estar olhando.
#
# A checagem e o estado de saude do proprio Docker, e nao um ping: o container
# pode estar de pe com o Minecraft travado dentro, e o healthcheck do itzg
# percebe isso.

SERVIDORES="proxy lobby pvp bedwars ctf eventos amongus database"
ESPERA=60
ONDE=/tmp/estado

if [ -z "$DISCORD_BOT_TOKEN" ] || [ "$DISCORD_BOT_TOKEN" = "COLE_O_TOKEN_AQUI" ]; then
  echo "Falta o DISCORD_BOT_TOKEN no infra/.env. Sem ele nao ha o que fazer."
  exit 1
fi
if [ -z "$DISCORD_CANAL_STATUS" ] || [ "$DISCORD_CANAL_STATUS" = "COLE_O_ID_DO_CANAL_AQUI" ]; then
  echo "Falta o DISCORD_CANAL_STATUS no infra/.env."
  exit 1
fi

command -v curl >/dev/null 2>&1 || apk add --no-cache curl >/dev/null 2>&1

mkdir -p "$ONDE"

falar() {
  # Sem escapar nada: todo texto daqui e escrito neste arquivo, e nenhum tem
  # aspas ou barra invertida. Escapar com sed dava erro no busybox e derrubava
  # o vigia logo no arranque.
  curl -s -o /dev/null -X POST \
    -H "Authorization: Bot $DISCORD_BOT_TOKEN" \
    -H "Content-Type: application/json" \
    -d "{\"content\":\"$1\"}" \
    "https://discord.com/api/v10/channels/$DISCORD_CANAL_STATUS/messages"
}

estado_de() {
  nome="game-craft-beta-$1-1"
  # Sem container: fora do ar. Com container mas sem healthcheck: vale o
  # running. Com healthcheck: vale a saude.
  saude=$(docker inspect -f '{{if .State.Health}}{{.State.Health.Status}}{{else}}{{if .State.Running}}healthy{{else}}down{{end}}{{end}}' "$nome" 2>/dev/null)
  case "$saude" in
    healthy) echo "no_ar" ;;
    starting) echo "subindo" ;;
    "") echo "fora" ;;
    *) echo "fora" ;;
  esac
}

falar "🟢 Vigia ligado. Aviso aqui quando um servidor cair ou voltar."

while true; do
  for s in $SERVIDORES; do
    agora=$(estado_de "$s")
    arquivo="$ONDE/$s"
    antes=$(cat "$arquivo" 2>/dev/null)

    # "subindo" nao vira aviso: todo reinicio passa por ele, e avisar duas vezes
    # por reinicio deixaria o canal cheio de coisa que nao e problema.
    [ "$agora" = "subindo" ] && continue

    if [ -z "$antes" ]; then
      echo "$agora" > "$arquivo"
      date +%s > "$arquivo.quando"
      continue
    fi

    if [ "$agora" != "$antes" ]; then
      quando=$(cat "$arquivo.quando" 2>/dev/null || date +%s)
      minutos=$(( ( $(date +%s) - quando ) / 60 ))
      if [ "$agora" = "fora" ]; then
        falar "🔴 **$s** caiu."
      else
        falar "🟢 **$s** voltou — ficou $minutos min fora."
      fi
      echo "$agora" > "$arquivo"
      date +%s > "$arquivo.quando"
    fi
  done
  sleep "$ESPERA"
done
