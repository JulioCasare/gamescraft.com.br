# Devolve o jogador a ultima posicao que ele ocupava fora da zona. As
# coordenadas vao para um armazenamento e voltam como macro, porque o comando
# tp so aceita numero escrito, nao placar.
execute store result storage gckits:pos x int 1 run scoreboard players get @s gc_x
execute store result storage gckits:pos y int 1 run scoreboard players get @s gc_y
execute store result storage gckits:pos z int 1 run scoreboard players get @s gc_z
function gckits:tp_volta with storage gckits:pos
title @s actionbar {"text":"Em combate: a area segura esta fechada para voce","color":"red"}
