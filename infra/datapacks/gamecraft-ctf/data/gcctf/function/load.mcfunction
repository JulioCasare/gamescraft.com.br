scoreboard objectives add gcctf dummy
# Placar do gatilho: o jogador liga e desliga com /obras, que e um apelido para
# /trigger obras.
scoreboard objectives add obras trigger

# O /clone vem de fabrica limitado a 32.768 blocos, e uma fatia do mapa tem
# 441.800. Sem levantar isto o save e o reset falham calados — foi o que
# aconteceu ate agora.
gamerule max_block_modifications 1000000

# Uma copia interrompida por reinicio nao pode ficar marcada como em andamento:
# o cursor se perdeu junto com o tique. E o pedaco que estava preso na hora da
# queda ficaria preso para sempre — forceload sobrevive a reinicio.
scoreboard players set #ativo gcctf 0
scoreboard players set #modo gcctf 0
scoreboard players set #espera gcctf 0
forceload remove all
