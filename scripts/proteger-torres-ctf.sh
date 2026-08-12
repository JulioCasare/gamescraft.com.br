#!/bin/bash
# Gera o datapack que protege as 50 torres do CTF: nada de quebrar nem de
# colocar bloco dentro delas.
#
# Uso: bash scripts/proteger-torres-ctf.sh [nome-do-container]
#
# Como funciona: cada torre tem uma copia de referencia enterrada na pedra do
# fundo do mundo, em y -60, no mesmo x/z dela. Uma vez por segundo o servidor
# compara a torre com a copia e, se estiver diferente, devolve a copia por cima.
# Bloco quebrado volta, bloco colocado some.
#
# A copia fica no fundo do mundo de proposito: e o mesmo trecho de chunk da
# torre, entao ela ja esta carregada junto e a comparacao nao puxa nada novo. E
# ninguem ve.
#
# Para mexer nas torres sem elas voltarem sozinhas: /obras em jogo. Ao desligar,
# o estado novo vira a referencia.
set -u
export MSYS_NO_PATHCONV=1

CONTAINER="${1:-game-craft-beta-ctf-1}"
RAIZ="$(cd "$(dirname "$0")/.." && pwd)"
PACK="$RAIZ/infra/datapacks/gamecraft-ctf"
FN="$PACK/data/gcctf/function"
ALTURAS="${ALTURAS:-$RAIZ/../alturas.txt}"

mkdir -p "$FN" "$PACK/data/minecraft/tags/function"

# As alturas vem medidas em jogo, achando o sinalizador de cada torre — ele fica
# sempre 10 blocos acima do chao dela. Formato: "TORRE <x> <z> <y do farol>".
if [ ! -f "$ALTURAS" ]; then
  echo "faltou o arquivo de alturas: $ALTURAS" >&2
  exit 1
fi

: > "$FN/verificar.mcfunction"
: > "$FN/guardar.mcfunction"

cat >> "$FN/verificar.mcfunction" <<'CAB'
# Uma linha por torre. A comparacao vem antes da copia para o servidor so mexer
# em bloco quando algo mudou de verdade — copiar as 50 toda hora faria os blocos
# piscarem na tela de quem esta perto.
#
# A condicao do sinalizador na copia e trava de seguranca: sem ela, uma torre
# nova sem copia feita seria "consertada" com ar e sumiria.
CAB

cat >> "$FN/guardar.mcfunction" <<'CAB'
# Tira uma foto nova das 50 torres. Roda na instalacao e toda vez que alguem
# desliga o /obras.
CAB

n=0
while read -r _ cx cz ybeacon; do
  n=$((n+1))
  hc=$((ybeacon-10))
  x1=$((cx-3)); x2=$((cx+3)); z1=$((cz-3)); z2=$((cz+3))
  y2=$((hc+10))
  echo "clone from minecraft:overworld $x1 $hc $z1 $x2 $y2 $z2 to minecraft:overworld $x1 -60 $z1 replace force" >> "$FN/guardar.mcfunction"
  echo "execute if block $cx -50 $cz minecraft:beacon unless blocks $x1 -60 $z1 $x2 -50 $z2 $x1 $hc $z1 all run clone from minecraft:overworld $x1 -60 $z1 $x2 -50 $z2 to minecraft:overworld $x1 $hc $z1 replace force" >> "$FN/verificar.mcfunction"
done < "$ALTURAS"

echo "torres no datapack: $n"

cat > "$PACK/pack.mcmeta" <<'EOF'
{
  "pack": {
    "description": "Game Craft - torres protegidas do CTF",
    "pack_format": 107,
    "min_format": 107,
    "max_format": 107
  }
}
EOF

echo '{ "values": ["gcctf:tick"] }' > "$PACK/data/minecraft/tags/function/tick.json"
echo '{ "values": ["gcctf:load"] }' > "$PACK/data/minecraft/tags/function/load.json"

cat > "$FN/load.mcfunction" <<'EOF'
scoreboard objectives add gcctf dummy
# Placar do gatilho: o jogador liga e desliga com /obras, que e um apelido para
# /trigger obras.
scoreboard objectives add obras trigger
EOF

cat > "$FN/tick.mcfunction" <<'EOF'
# O gatilho precisa ser reabilitado a cada uso, por isso a linha roda todo tick.
scoreboard players enable @a obras
execute as @a[scores={obras=1..}] run function gcctf:trigger_obras

scoreboard players add #t gcctf 1
execute if score #t gcctf matches 20.. run function gcctf:cada_segundo
EOF

cat > "$FN/cada_segundo.mcfunction" <<'EOF'
scoreboard players set #t gcctf 0
# Com alguem de obras ligado, a protecao para: senao a torre voltaria ao estado
# antigo no meio da edicao.
execute unless entity @a[tag=gc_obras] run function gcctf:verificar
EOF

cat > "$FN/trigger_obras.mcfunction" <<'EOF'
scoreboard players set @s obras 0
# A marca temporaria existe porque as duas linhas rodam no mesmo tick: sem ela,
# o mesmo comando ligava e desligava de novo em seguida.
execute if entity @s[tag=!gc_obras] run function gcctf:obras_ligar
execute if entity @s[tag=gc_obras,tag=!gc_alternar] run function gcctf:obras_desligar
tag @s remove gc_alternar
EOF

cat > "$FN/obras_ligar.mcfunction" <<'EOF'
tag @s add gc_obras
tag @s add gc_alternar
tellraw @s [{"text":"Obras ligadas. ","color":"gold"},{"text":"As torres param de se consertar. Desligue com /obras para guardar o novo estado.","color":"gray"}]
EOF

cat > "$FN/obras_desligar.mcfunction" <<'EOF'
tag @s remove gc_obras
function gcctf:guardar
tellraw @s [{"text":"Obras desligadas. ","color":"green"},{"text":"As torres do jeito que estao agora viraram a referencia.","color":"gray"}]
EOF

# Instala no mundo e liga.
DEST=/data/ilha/datapacks/gamecraft-ctf
docker exec -u 1000 "$CONTAINER" mkdir -p "$DEST" >/dev/null
ORIGEM="$PACK"
command -v cygpath >/dev/null 2>&1 && ORIGEM="$(cygpath -w "$PACK")"
docker cp "$ORIGEM/." "$CONTAINER:$DEST/" >/dev/null
docker exec -u 0 "$CONTAINER" chown -R 1000:1000 "$DEST" >/dev/null
docker exec -u 1000 "$CONTAINER" mc-send-to-console "reload" >/dev/null
sleep 5
# Primeira foto das torres: sem ela a protecao nao tem com o que comparar.
docker exec -u 1000 "$CONTAINER" mc-send-to-console "function gcctf:guardar" >/dev/null
echo "=== datapack instalado e primeira copia feita ==="
