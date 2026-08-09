# Conserto continuo do mapa. A cada 30 segundos devolve os blocos do backup em
# modo "masked": so copia o que NAO e ar no backup, entao buraco aberto por
# jogador se fecha, e o que ele construiu no vazio continua de pe ate vencer o
# prazo de 5 minutos.
#
# E assim que ninguem consegue quebrar de verdade o que ja estava no mundo: o
# Minecraft so proibiria isso no modo aventura, que tambem proibiria balde,
# bloco e TNT — ou seja, mataria metade do kit.
scoreboard players add #tick gc_zone 1
execute if score #tick gc_zone matches 600.. run function gckits:reparo_agora
