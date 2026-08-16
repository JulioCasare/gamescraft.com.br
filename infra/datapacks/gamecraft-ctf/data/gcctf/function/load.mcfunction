scoreboard objectives add gcctf dummy

# Sempre de dia. A ilha e aberta e a disputa acontece no chao: de noite o mapa
# fica escuro demais para ver quem esta subindo na torre ao lado, e a partida
# vira um jogo de ouvido.
#
# Feito no relogio, e nao por gamerule: na 26.2 o ciclo do dia deixou de ter
# regra com esse nome — o tempo virou marcador por dimensao, e `gamerule
# doDaylightCycle` nao existe mais. Repor o dia de dez em dez segundos custa
# nada e nao depende de adivinhar o nome novo.
time set day
weather clear
# Placar do gatilho: o jogador liga e desliga com /obras, que e um apelido para
# /trigger obras.
scoreboard objectives add obras trigger

# O /clone vem de fabrica limitado a 32.768 blocos, e um pedaco do mapa passa de
# meio milhao. Sem levantar isto o save e o reset falham calados — foi o que
# aconteceu ate agora.
gamerule max_block_modifications 4000000

# Onde mora a copia de seguranca: cinco mil blocos ao lado, no mesmo Y.
#
# Ela ja morou 150 blocos acima do mapa, e foi um erro caro. Bloco em cima de
# sinalizador tapa o feixe, e o mapa inteiro sobre as torres apagou os 58
# beacons de uma vez. Ao lado, e em chunks que ninguem visita, ela nao tapa nada
# — e ainda deixa o Julio marcar a arena na altura que quiser, sem a copia
# esbarrar na origem.
scoreboard players set #desvio gcctf 5000

# Uma copia interrompida por reinicio nao pode ficar marcada como em andamento:
# o cursor se perdeu junto com o tique. E o pedaco que estava preso na hora da
# queda ficaria preso para sempre — forceload sobrevive a reinicio.
scoreboard players set #ativo gcctf 0
scoreboard players set #modo gcctf 0
scoreboard players set #espera gcctf 0
forceload remove all
