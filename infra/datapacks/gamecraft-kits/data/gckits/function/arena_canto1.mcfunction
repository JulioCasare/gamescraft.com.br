# Primeiro canto da arena — a area que o /save guarda e o /reset devolve.
execute store result score #ax1 gc_zone run data get entity @s Pos[0] 1
execute store result score #ay1 gc_zone run data get entity @s Pos[1] 1
execute store result score #az1 gc_zone run data get entity @s Pos[2] 1
tellraw @s [{"text":"Canto 1 da arena marcado. ","color":"green"},{"text":"Va ao canto oposto e rode /function gckits:arena_canto2","color":"gray"}]
