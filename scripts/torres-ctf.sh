#!/bin/bash
# Carimba as 50 torres da ilha do CTF, cada uma com sinalizador aceso e a area
# em volta marcada por cerca.
#
# Uso: bash scripts/torres-ctf.sh [nome-do-container]
#
# Roda depois do scripts/ilha-ctf.sh, e pode rodar de novo em cima de si mesmo:
# cada torre e refeita do zero no lugar.
#
# A altura do chao nao e medida em jogo, e sim recalculada: o proprio WorldEdit
# avalia a mesma formula de ruido que desenhou a ilha, so que nas coordenadas do
# centro da torre. E o que permite carimbar 50 torres sem sondar 50 terrenos.
set -u
export MSYS_NO_PATHCONV=1

CONTAINER="${1:-game-craft-beta-ctf-1}"
LISTA="$(mktemp)"
trap 'rm -f "$LISTA"' EXIT

# As 50 posicoes: o centro, as quatro primeiras torres e quatro aneis. Anel em
# vez de grade porque a ilha e redonda — grade deixaria canto vazio e beirada
# cheia. O anel de fora para em 205: a costa chega a encolher ate 222, e a cerca
# ainda precisa de 12 blocos de folga.
#
# Os angulos de partida de cada anel sao escolhidos, nao redondos: com o anel de
# 175 comecando em zero, ele caia em cima do eixo e encostava na torre de
# (-150,0) — 24 blocos, exatamente a soma dos dois raios de cerca. Em 20 graus a
# menor distancia entre duas torres quaisquer sobe para 30.
posicoes() {
  awk 'BEGIN{
    print 0, 0; print 150, 0; print -150, 0; print 0, 150; print 0, -150
    r[1]=70;  n[1]=8;  a[1]=22.5
    r[2]=125; n[2]=12; a[2]=15
    r[3]=175; n[3]=12; a[3]=20
    r[4]=205; n[4]=13; a[4]=10
    pi=3.14159265358979
    for (k=1; k<=4; k++)
      for (i=0; i<n[k]; i++) {
        ang=(a[k] + i*360/n[k]) * pi/180
        printf "%d %d\n", int(r[k]*cos(ang)+0.5), int(r[k]*sin(ang)+0.5)
      }
  }'
}

P() { echo "$1" >> "$LISTA"; }

P '//world ilha'

total=0
while read -r cx cz; do
  total=$((total+1))
  dc=$(awk -v a="$cx" -v b="$cz" 'BEGIN{printf "%.3f", sqrt(a*a+b*b)}')
  # hc: altura do chao no centro desta torre, arredondada para baixo.
  CEN="rc=250+28*perlin(7,$cx*0.008,0,$cz*0.008,1,3,0.5);tc=min(1,max(0,(rc-$dc)/45));hc=floor(62+tc*tc*max(2,6+8*perlin(13,$cx*0.012,0,$cz*0.012,1,4,0.5)));"

  # Terreno: limpa o que estiver acima e nivela um patamar de 9x9. Colina em
  # cima da torre cortaria o feixe do sinalizador.
  P "//pos1 $((cx-4)),51,$((cz-4))"; P "//pos2 $((cx+4)),110,$((cz+4))"
  P "//gen -r minecraft:air ${CEN}y>hc"
  P "//gen -r minecraft:dirt ${CEN}y<hc&&y>=hc-4"
  P "//gen -r minecraft:grass_block ${CEN}y==hc"

  # Corpo macico, comecando abaixo do chao para nao ficar de pe no ar do lado em
  # que o terreno desce.
  P "//pos1 $((cx-3)),51,$((cz-3))"; P "//pos2 $((cx+3)),110,$((cz+3))"
  P "//gen -r minecraft:stone_bricks ${CEN}y>=hc-4&&y<=hc+8"

  # Sala por dentro.
  P "//pos1 $((cx-2)),51,$((cz-2))"; P "//pos2 $((cx+2)),110,$((cz+2))"
  P "//gen -r minecraft:air ${CEN}y>=hc+1&&y<=hc+7"

  # Porta, na face norte.
  P "//pos1 $cx,51,$((cz-3))"; P "//pos2 $cx,110,$((cz-3))"
  P "//gen -r minecraft:air ${CEN}y>=hc+1&&y<=hc+2"

  # Escada encostada na parede sul, subindo ate o teto. O ultimo degrau ocupa o
  # bloco do teto e vira o alcapao: sem ele nao ha como chegar no sinalizador.
  P "//pos1 $cx,51,$((cz+2))"; P "//pos2 $cx,110,$((cz+2))"
  P "//gen -r minecraft:ladder[facing=north] ${CEN}y>=hc+1&&y<=hc+8"

  # Luz da sala, no meio do teto.
  P "//pos1 $cx,51,$cz"; P "//pos2 $cx,110,$cz"
  P "//gen -r minecraft:glowstone ${CEN}y==hc+8"

  # Guarda-corpo do teto, de cerca de spruce — e o alcapao em cima da escada.
  # Os dois vem da litematica que o Julio mandou.
  P "//pos1 $((cx-3)),51,$((cz-3))"; P "//pos2 $((cx+3)),110,$((cz+3))"
  P "//gen -r minecraft:spruce_fence ${CEN}y==hc+9"
  P "//pos1 $((cx-2)),51,$((cz-2))"; P "//pos2 $((cx+2)),110,$((cz+2))"
  P "//gen -r minecraft:air ${CEN}y==hc+9"
  P "//pos1 $cx,51,$((cz+2))"; P "//pos2 $cx,110,$((cz+2))"
  P "//gen -r minecraft:spruce_trapdoor[facing=north,half=top,open=false] ${CEN}y==hc+9"

  # 3x3 de ferro: e ele, logo abaixo, que deixa o sinalizador aceso.
  P "//pos1 $((cx-1)),51,$((cz-1))"; P "//pos2 $((cx+1)),110,$((cz+1))"
  P "//gen -r minecraft:iron_block ${CEN}y==hc+9"

  P "//pos1 $cx,51,$cz"; P "//pos2 $cx,110,$cz"
  P "//gen -r minecraft:beacon ${CEN}y==hc+10"

  if [ $((total % 10)) = 0 ]; then P '//clearhistory'; fi
done < <(posicoes)

P '//clearhistory'
P 'save-all'

echo "torres na lista: $total"
echo "comandos: $(wc -l < "$LISTA")"

# No Git Bash do Windows o caminho precisa ir convertido: o docker e um programa
# do Windows e leria /tmp/... como D:\tmp.
ORIGEM="$LISTA"
command -v cygpath >/dev/null 2>&1 && ORIGEM="$(cygpath -w "$LISTA")"
docker cp "$ORIGEM" "$CONTAINER:/tmp/torres.txt" >/dev/null
docker exec -u 1000 "$CONTAINER" sh -c 'while IFS= read -r linha; do [ -n "$linha" ] && mc-send-to-console "$linha"; done < /tmp/torres.txt'
echo "=== enviado; o servidor ainda esta processando a fila ==="
