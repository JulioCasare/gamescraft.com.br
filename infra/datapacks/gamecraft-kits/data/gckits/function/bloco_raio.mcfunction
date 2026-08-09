# Acha o primeiro bloco solido na linha do olhar, ate 6 blocos — que e o bloco
# que o jogador acabou de colocar — e marca ele para sumir depois.
execute unless block ~ ~ ~ minecraft:air unless block ~ ~ ~ minecraft:water unless block ~ ~ ~ minecraft:lava run function gckits:marcar_bloco
execute if block ~ ~ ~ minecraft:air positioned ^ ^ ^0.5 if entity @s[distance=..6] run function gckits:bloco_raio
