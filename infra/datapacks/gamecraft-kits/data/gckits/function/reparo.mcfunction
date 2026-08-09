# Conserto do mapa. O Minecraft nao avisa quando um bloco e quebrado, mas todo
# bloco quebrado larga um item — entao um item novo no chao serve de aviso, e o
# conserto sai quase junto com a pancada.
#
# O relogio existe por duas razoes: nao clonar a arena mais de uma vez por
# segundo (custa caro) e ainda assim passar um pente fino a cada 10 segundos,
# para pegar bloco que caiu sem largar item.
scoreboard players add #tick gc_zone 1
execute as @e[type=minecraft:item,tag=!gc_visto] run function gckits:item_novo
execute if score #reparar gc_zone matches 1.. if score #tick gc_zone matches 20.. run function gckits:reparo_agora
execute if score #tick gc_zone matches 200.. run function gckits:reparo_agora
