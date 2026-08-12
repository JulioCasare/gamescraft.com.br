#!/bin/bash
# Constroi o mapa do CTF: uma ilha de mais ou menos 500x500 num mar plano, com
# cinco mini torres de sinalizador.
#
# O mundo `ilha` nasce so de mar (ver LEVEL/GENERATOR_SETTINGS do servico ctf no
# compose.yaml). Este script desenha a ilha por cima, com o WorldEdit rodando
# pelo console do servidor — nao precisa de ninguem em jogo.
#
# Uso: bash scripts/ilha-ctf.sh [nome-do-container]
#
# Roda em pedacos de 100x100: um unico //gen do tamanho da ilha seriam 10
# milhoes de blocos de uma vez, e o historico do WorldEdit estoura a memoria do
# servidor. Entre uma coluna e outra o historico e limpo pelo mesmo motivo.
set -u
export MSYS_NO_PATHCONV=1

CONTAINER="${1:-game-craft-beta-ctf-1}"
S() { docker exec -u 1000 "$CONTAINER" mc-send-to-console "$1" >/dev/null 2>&1; }

# dd  distancia do centro da ilha
# rr  raio da costa: 250 blocos mais ou menos 28 de ruido, para o contorno nao
#     sair de compasso
# tt  0 na beira e 1 terra adentro; ao quadrado, vira a subida macia da praia
# hh  altura do terreno: 62 (nivel do mar) mais as colinas
EXPR='dd=sqrt(x*x+z*z);rr=250+28*perlin(7,x*0.008,0,z*0.008,1,3,0.5);tt=min(1,max(0,(rr-dd)/45));hh=62+tt*tt*max(2,6+8*perlin(13,x*0.012,0,z*0.012,1,4,0.5));dd<rr&&y<=hh'

TILES="-300 -200 -100 0 100 200"

S '//world ilha'

echo "=== terreno ==="
for x0 in $TILES; do
  for z0 in $TILES; do
    S "//pos1 $x0,51,$z0"
    S "//pos2 $((x0+99)),80,$((z0+99))"
    S "//gen -r minecraft:stone $EXPR"
    sleep 3
  done
  S '//clearhistory'
  echo "  x=$x0"
done

echo "=== grama, terra e pedra ==="
for x0 in $TILES; do
  for z0 in $TILES; do
    S "//pos1 $x0,51,$z0"
    S "//pos2 $((x0+99)),80,$((z0+99))"
    S "//naturalize"
    sleep 3
  done
  S '//clearhistory'
  echo "  x=$x0"
done

echo "=== areia na beira ==="
for x0 in $TILES; do
  for z0 in $TILES; do
    S "//pos1 $x0,62,$z0"
    S "//pos2 $((x0+99)),63,$((z0+99))"
    S "//replace minecraft:grass_block,minecraft:dirt minecraft:sand"
    sleep 1
  done
  S '//clearhistory'
done

# A altura do chao de cada torre foi medida em jogo depois do terreno pronto.
# Altura fixa nao serve: o terreno tem colina, e a torre ficaria enterrada num
# sitio e de pe no ar em outro. Para medir de novo:
#   execute unless block <x> <y> <z> #minecraft:air if block <x> <y+1> <z> #minecraft:air run say <y>
TORRES="centro,0,0,72 leste,150,0,70 oeste,-150,0,72 sul,0,150,69 norte,0,-150,69"

echo "=== torres ==="
for t in $TORRES; do
  IFS=',' read -r nome cx cz g <<< "$t"
  echo "  $nome em $cx,$cz (chao $g)"

  # Espaco limpo acima: colina em cima da torre cortaria o feixe do sinalizador.
  S "//pos1 $((cx-4)),$((g+1)),$((cz-4))"
  S "//pos2 $((cx+4)),$((g+30)),$((cz+4))"
  S "//set minecraft:air"

  # Bloco macico, comecando abaixo do chao para a torre nao ficar de pe no ar
  # do lado em que o terreno desce.
  S "//pos1 $((cx-3)),$((g-4)),$((cz-3))"
  S "//pos2 $((cx+3)),$((g+8)),$((cz+3))"
  S "//set minecraft:stone_bricks"

  S "//pos1 $((cx-2)),$((g+1)),$((cz-2))"
  S "//pos2 $((cx+2)),$((g+7)),$((cz+2))"
  S "//set minecraft:air"

  S "//pos1 $cx,$((g+1)),$((cz-3))"
  S "//pos2 $cx,$((g+2)),$((cz-3))"
  S "//set minecraft:air"

  S "//pos1 $cx,$((g+8)),$cz"
  S "//pos2 $cx,$((g+8)),$cz"
  S "//set minecraft:glowstone"

  S "//pos1 $((cx-3)),$((g+9)),$((cz-3))"
  S "//pos2 $((cx+3)),$((g+9)),$((cz+3))"
  S "//set minecraft:stone_brick_wall"
  S "//pos1 $((cx-2)),$((g+9)),$((cz-2))"
  S "//pos2 $((cx+2)),$((g+9)),$((cz+2))"
  S "//set minecraft:air"

  # 3x3 de ferro logo abaixo do sinalizador: e o que o deixa aceso.
  S "//pos1 $((cx-1)),$((g+9)),$((cz-1))"
  S "//pos2 $((cx+1)),$((g+9)),$((cz+1))"
  S "//set minecraft:iron_block"

  S "//pos1 $cx,$((g+10)),$cz"
  S "//pos2 $cx,$((g+10)),$cz"
  S "//set minecraft:beacon"
  sleep 1
done

S '//clearhistory'
S 'setworldspawn 12 72 0'
S 'save-all'
echo "=== pronto ==="
