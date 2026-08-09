# Marca o segundo canto e ja salva a area.
execute store result score #x2 gc_zone run data get entity @s Pos[0] 1
execute store result score #y2 gc_zone run data get entity @s Pos[1] 1
execute store result score #z2 gc_zone run data get entity @s Pos[2] 1
function gckits:zona_salvar
