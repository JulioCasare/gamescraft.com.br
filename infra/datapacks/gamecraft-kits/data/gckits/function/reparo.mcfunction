# Conserto do mapa. O Minecraft nao avisa quando um bloco e quebrado, mas todo
# bloco quebrado larga um item — entao um item novo no chao serve de aviso, e o
# conserto sai no mesmo instante, so em volta de onde caiu (gckits:item_novo).
#
# Alem disso, um pente fino na arena inteira a cada 5 minutos, para pegar bloco
# que sumiu sem largar item. E caro (a arena toda), por isso raro.
scoreboard players add #tick gc_zone 1
execute as @e[type=minecraft:item,tag=!gc_visto] run function gckits:item_novo
execute if score #tick gc_zone matches 6000.. run function gckits:reparo_agora
