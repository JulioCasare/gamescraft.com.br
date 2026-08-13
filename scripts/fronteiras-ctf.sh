#!/bin/bash
# Desenha as fronteiras entre as areas das torres, em concreto vermelho rente ao
# chao, e tira as cercas antigas.
#
# Uso: bash scripts/fronteiras-ctf.sh [nome-do-container]
#
# A divisao e por proximidade: cada pedaco de chao pertence a torre mais perto
# dele. A fronteira e onde duas torres empatam, entao as areas encostam umas nas
# outras sem sobra de terreno no meio — que era o defeito da cerca quadrada de
# raio fixo.
#
# A conta roda dentro do WorldEdit, um bloco por vez: para cada coluna ele mede
# a distancia ate as 50 torres, guarda as duas menores e pinta quando as duas
# quase empatam. E por isso que a lista de torres aparece escrita por extenso na
# expressao — a linguagem de expressao do WorldEdit nao tem lista.
set -u
export MSYS_NO_PATHCONV=1

CONTAINER="${1:-game-craft-beta-ctf-1}"
LISTA="$(mktemp)"
trap 'rm -f "$LISTA"' EXIT

# Mesma altura de terreno do ilha-ctf.sh: o concreto entra no lugar do bloco de
# cima do chao, entao fica rente e nao vira mureta.
TERR='dd=sqrt(x*x+z*z);rr=250+28*perlin(7,x*0.008,0,z*0.008,1,3,0.5);tt=min(1,max(0,(rr-dd)/45));hh=62+tt*tt*max(2,6+8*perlin(13,x*0.012,0,z*0.012,1,4,0.5));'

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

# m1 e a distancia da torre mais perto, m2 a da segunda. Ficam ao quadrado
# durante a conta e so viram distancia de verdade no fim, para nao tirar 50
# raizes por bloco.
VORONOI='m1=1e9;m2=1e9;'
while read -r cx cz; do
  VORONOI="${VORONOI}dq=(x-($cx))*(x-($cx))+(z-($cz))*(z-($cz));if(dq<m1){m2=m1;m1=dq;}else{if(dq<m2){m2=dq;}}"
done < <(posicoes)
# Pinta onde as duas mais perto quase empatam: e a linha do meio entre elas.
#
# O limite seria a largura em blocos se toda linha corresse reta no eixo. Nao e
# o caso: na diagonal, uma faixa de 2 de largura de verdade cobre quase 3 blocos
# medidos em linha, e o desenho engorda. Com 1,4 a diagonal cai para 2 e a reta
# fica em 1 ou 2.
#
# Onde tres areas se encontram a linha continua mais grossa, e isso nao tem como
# evitar por aqui: sao tres faixas passando pelo mesmo ponto.
VORONOI="${VORONOI}(sqrt(m2)-sqrt(m1))<1.4&&dd<rr&&y==floor(hh)"

P() { echo "$1" >> "$LISTA"; }

P '//world ilha'

TILES="-300 -200 -100 0 100 200"

# Fora o que marcava area nas versoes anteriores: a cerca de carvalho e as
# linhas ja desenhadas.
for x0 in $TILES; do
  for z0 in $TILES; do
    P "//pos1 $x0,60,$z0"
    P "//pos2 $((x0+99)),90,$((z0+99))"
    P "//replace minecraft:oak_fence minecraft:air"
    # A linha antiga sai antes de a nova entrar: agora a nova e mais estreita
    # que a velha, entao pintar por cima deixaria a sobra da anterior no chao.
    # Volta a virar grama, e a faixa de areia da beirada e refeita logo abaixo.
    P "//replace minecraft:red_concrete minecraft:grass_block"
    P "//replace minecraft:gray_concrete minecraft:grass_block"
  done
  P '//clearhistory'
done

# A faixa de areia da beirada, refeita: a limpeza acima devolveu grama tambem
# onde a linha antiga passava por cima da praia.
for x0 in $TILES; do
  for z0 in $TILES; do
    P "//pos1 $x0,62,$z0"
    P "//pos2 $((x0+99)),63,$((z0+99))"
    P "//replace minecraft:grass_block,minecraft:dirt minecraft:sand"
  done
  P '//clearhistory'
done

# As fronteiras. So a faixa de altura onde o chao existe: de 62 (nivel do mar) a
# 78 (o topo das colinas). Varrer o mundo inteiro seria dez vezes mais conta
# para o mesmo desenho.
for x0 in $TILES; do
  for z0 in $TILES; do
    P "//pos1 $x0,62,$z0"
    P "//pos2 $((x0+99)),78,$((z0+99))"
    P "//gen -r minecraft:gray_concrete ${TERR}${VORONOI}"
  done
  P '//clearhistory'
done

P 'save-all'

echo "comandos: $(wc -l < "$LISTA")"
ORIGEM="$LISTA"
command -v cygpath >/dev/null 2>&1 && ORIGEM="$(cygpath -w "$LISTA")"
docker cp "$ORIGEM" "$CONTAINER:/tmp/fronteiras.txt" >/dev/null
docker exec -u 1000 "$CONTAINER" sh -c 'while IFS= read -r linha; do [ -n "$linha" ] && mc-send-to-console "$linha"; done < /tmp/fronteiras.txt'
echo "=== enviado; o servidor ainda esta processando a fila ==="
