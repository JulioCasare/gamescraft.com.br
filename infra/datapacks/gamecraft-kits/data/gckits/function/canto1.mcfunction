# Marca o primeiro canto da area segura na posicao onde voce esta.
execute store result score #x1 gc_zone run data get entity @s Pos[0] 1
execute store result score #y1 gc_zone run data get entity @s Pos[1] 1
execute store result score #z1 gc_zone run data get entity @s Pos[2] 1
tellraw @s [{"text":"Canto 1 marcado. ","color":"green"},{"text":"Va ate o canto oposto e rode /function gckits:canto2","color":"gray"}]
