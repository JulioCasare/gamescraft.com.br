# Conserto do mapa. Duas redes, porque uma so nao pega tudo:
#
# 1. Item novo no chao: e o rastro de um bloco quebrado, e conserta na hora.
#    Nao serve sozinho — quem quebra no modo criativo nao larga item nenhum.
# 2. Varredura de perto: a cada segundo, conserta em volta de cada jogador.
#    Como so da para quebrar o que esta ao alcance, isso cobre qualquer quebra,
#    em qualquer modo de jogo, sem varrer a arena inteira.
scoreboard players add #tick gc_zone 1
execute as @e[type=minecraft:item,tag=!gc_visto] run function gckits:item_novo
execute if score #tick gc_zone matches 20.. run function gckits:reparo_ciclo
