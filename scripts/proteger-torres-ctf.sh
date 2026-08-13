#!/bin/bash
# Gera o datapack que protege as torres e as areas do CTF: nada de quebrar nem
# de colocar bloco dentro delas.
#
# Uso: ALTURAS=<arquivo> bash scripts/proteger-torres-ctf.sh [nome-do-container]
#
# Como funciona: cada torre e cada area tem uma copia de referencia enterrada na
# pedra do fundo do mundo, no mesmo x/z dela. De tempos em tempos o servidor
# compara o original com a copia e, se estiver diferente, devolve a copia por
# cima. Bloco quebrado volta, bloco colocado some.
#
# A copia fica no fundo do mundo de proposito: e o mesmo trecho de chunk do
# original, entao ela ja esta carregada junto e a comparacao nao puxa nada novo.
# E ninguem ve.
#
# Para mexer sem as coisas voltarem sozinhas: /obras em jogo. Ao desligar, o
# estado novo vira a referencia.
set -u
export MSYS_NO_PATHCONV=1

CONTAINER="${1:-game-craft-beta-ctf-1}"
RAIZ="$(cd "$(dirname "$0")/.." && pwd)"
PACK="$RAIZ/infra/datapacks/gamecraft-ctf"
FN="$PACK/data/gcctf/function"
ALTURAS="${ALTURAS:?informe ALTURAS=<arquivo com linhas 'TORRE x z y-do-farol'>}"

mkdir -p "$FN" "$PACK/data/minecraft/tags/function"

# As areas que o Julio marcou a mao, cada uma por dois cantos opostos. Elas
# entram inteiras, e as torres que caem dentro delas ficam de fora da protecao
# individual: seria a mesma coisa protegida duas vezes, e a copia de uma
# brigaria com a da outra.
#
# formato: minx miny minz maxx maxy maxz
AREAS="26 67 -207 65 90 -183
-26 65 190 12 88 214"

# Altura onde as copias das areas moram. Nao pode encostar na faixa das torres
# (-60 a -50), senao uma copia sobrescreve a outra.
AREA_Y=-40

dentro_de_area() {
  local x=$1 z=$2
  while read -r minx miny minz maxx maxy maxz; do
    [ -z "$minx" ] && continue
    if [ "$x" -ge "$minx" ] && [ "$x" -le "$maxx" ] && [ "$z" -ge "$minz" ] && [ "$z" -le "$maxz" ]; then
      return 0
    fi
  done <<< "$AREAS"
  return 1
}

: > "$FN/verificar.mcfunction"
: > "$FN/verificar_areas.mcfunction"
: > "$FN/guardar.mcfunction"

cat >> "$FN/verificar.mcfunction" <<'CAB'
# Uma linha por torre. A comparacao vem antes da copia para o servidor so mexer
# em bloco quando algo mudou de verdade — copiar todas toda hora faria os blocos
# piscarem na tela de quem esta perto.
#
# A condicao do sinalizador na copia e trava de seguranca: sem ela, uma torre
# nova sem copia feita seria "consertada" com ar e sumiria.
CAB

cat >> "$FN/verificar_areas.mcfunction" <<'CAB'
# As areas marcadas a mao. Aqui a trava e o placar #copiado: enquanto o guardar
# nao tiver rodado uma vez, nada e restaurado — senao a primeira passagem
# apagaria a area inteira com o vazio da copia que ainda nao existe.
CAB

cat >> "$FN/guardar.mcfunction" <<'CAB'
# Tira uma foto nova de tudo. Roda na instalacao e toda vez que alguem desliga
# o /obras.
CAB

torres=0
fora=0
while read -r _ cx cz ybeacon; do
  [ -z "${cx:-}" ] && continue
  if dentro_de_area "$cx" "$cz"; then
    fora=$((fora+1))
    continue
  fi
  torres=$((torres+1))
  hc=$((ybeacon-10))
  x1=$((cx-3)); x2=$((cx+3)); z1=$((cz-3)); z2=$((cz+3))
  y2=$((hc+10))
  echo "clone from minecraft:overworld $x1 $hc $z1 $x2 $y2 $z2 to minecraft:overworld $x1 -60 $z1 replace force" >> "$FN/guardar.mcfunction"
  echo "execute if block $cx -50 $cz minecraft:beacon unless blocks $x1 -60 $z1 $x2 -50 $z2 $x1 $hc $z1 all run clone from minecraft:overworld $x1 -60 $z1 $x2 -50 $z2 to minecraft:overworld $x1 $hc $z1 replace force" >> "$FN/verificar.mcfunction"
done < "$ALTURAS"

areas=0
while read -r minx miny minz maxx maxy maxz; do
  [ -z "$minx" ] && continue
  areas=$((areas+1))
  alt=$((maxy-miny))
  by2=$((AREA_Y+alt))
  # A copia, e logo abaixo dela um bloco de marca. O setblock so acontece se o
  # pedaco do mapa estiver carregado — o mesmo que vale para o clone. Entao a
  # marca prova que a copia foi feita de verdade.
  #
  # Isto existe porque a versao anterior confiava num placar ligado no fim do
  # guardar, sem checar se o clone tinha acontecido. Com o mapa descarregado o
  # clone falhava calado, o placar ligava do mesmo jeito, e a "copia" era a
  # pedra virgem do fundo do mundo — que foi restaurada por cima da area e a
  # transformou num cubo macico. Nao repetir.
  echo "clone from minecraft:overworld $minx $miny $minz $maxx $maxy $maxz to minecraft:overworld $minx $AREA_Y $minz replace force" >> "$FN/guardar.mcfunction"
  echo "setblock $minx $((AREA_Y-1)) $minz minecraft:bedrock replace" >> "$FN/guardar.mcfunction"
  echo "execute if block $minx $((AREA_Y-1)) $minz minecraft:bedrock unless blocks $minx $AREA_Y $minz $maxx $by2 $maxz $minx $miny $minz all run clone from minecraft:overworld $minx $AREA_Y $minz $maxx $by2 $maxz to minecraft:overworld $minx $miny $minz replace force" >> "$FN/verificar_areas.mcfunction"
done <<< "$AREAS"


echo "torres protegidas uma a uma: $torres (fora, por estarem dentro das areas: $fora)"
echo "areas protegidas inteiras: $areas"

cat > "$PACK/pack.mcmeta" <<'EOF'
{
  "pack": {
    "description": "Game Craft - torres e areas protegidas do CTF",
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
execute if score #t gcctf matches 10.. run function gcctf:meio_segundo

# As areas sao grandes: comparar as duas custa mais que as 48 torres juntas.
# Uma vez por segundo e o ponto em que o conserto parece imediato para quem
# quebrou sem a conta pesar no servidor.
scoreboard players add #ta gcctf 1
execute if score #ta gcctf matches 20.. run function gcctf:cada_segundo
EOF

cat > "$FN/meio_segundo.mcfunction" <<'EOF'
scoreboard players set #t gcctf 0
# Com alguem de obras ligado, a protecao para: senao a torre voltaria ao estado
# antigo no meio da edicao.
execute unless entity @a[tag=gc_obras] run function gcctf:verificar
EOF

cat > "$FN/cada_segundo.mcfunction" <<'EOF'
scoreboard players set #ta gcctf 0
execute unless entity @a[tag=gc_obras] run function gcctf:verificar_areas
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
tellraw @s [{"text":"Obras ligadas. ","color":"gold"},{"text":"As torres e as areas param de se consertar. Desligue com /obras para guardar o novo estado.","color":"gray"}]
EOF

cat > "$FN/obras_desligar.mcfunction" <<'EOF'
tag @s remove gc_obras
function gcctf:guardar
tellraw @s [{"text":"Obras desligadas. ","color":"green"},{"text":"Tudo do jeito que esta agora virou a referencia.","color":"gray"}]
EOF

DEST=/data/ilha/datapacks/gamecraft-ctf
docker exec -u 1000 "$CONTAINER" mkdir -p "$DEST" >/dev/null
ORIGEM="$PACK"
command -v cygpath >/dev/null 2>&1 && ORIGEM="$(cygpath -w "$PACK")"
docker cp "$ORIGEM/." "$CONTAINER:$DEST/" >/dev/null
docker exec -u 0 "$CONTAINER" chown -R 1000:1000 "$DEST" >/dev/null
docker exec -u 1000 "$CONTAINER" mc-send-to-console 'datapack enable "file/gamecraft-ctf"' >/dev/null
docker exec -u 1000 "$CONTAINER" mc-send-to-console "reload" >/dev/null
sleep 6
# Primeira foto: sem ela a protecao nao tem com o que comparar.
docker exec -u 1000 "$CONTAINER" mc-send-to-console "function gcctf:guardar" >/dev/null
echo "=== datapack instalado, ligado, e primeira copia feita ==="
