# Transforma os dois cantos em canto minimo + tamanho, que e o formato que o
# seletor entende (x=..,dx=..). O operador < guarda o menor e o > o maior.
scoreboard players operation #minx gc_zone = #x1 gc_zone
scoreboard players operation #minx gc_zone < #x2 gc_zone
scoreboard players operation #miny gc_zone = #y1 gc_zone
scoreboard players operation #miny gc_zone < #y2 gc_zone
scoreboard players operation #minz gc_zone = #z1 gc_zone
scoreboard players operation #minz gc_zone < #z2 gc_zone

scoreboard players operation #dx gc_zone = #x1 gc_zone
scoreboard players operation #dx gc_zone > #x2 gc_zone
scoreboard players operation #dx gc_zone -= #minx gc_zone
scoreboard players operation #dy gc_zone = #y1 gc_zone
scoreboard players operation #dy gc_zone > #y2 gc_zone
scoreboard players operation #dy gc_zone -= #miny gc_zone
scoreboard players operation #dz gc_zone = #z1 gc_zone
scoreboard players operation #dz gc_zone > #z2 gc_zone
scoreboard players operation #dz gc_zone -= #minz gc_zone

execute store result storage gckits:zona minx int 1 run scoreboard players get #minx gc_zone
execute store result storage gckits:zona miny int 1 run scoreboard players get #miny gc_zone
execute store result storage gckits:zona minz int 1 run scoreboard players get #minz gc_zone
execute store result storage gckits:zona dx int 1 run scoreboard players get #dx gc_zone
execute store result storage gckits:zona dy int 1 run scoreboard players get #dy gc_zone
execute store result storage gckits:zona dz int 1 run scoreboard players get #dz gc_zone
tellraw @s [{"text":"Area segura salva. ","color":"green"},{"text":"Quem esta em combate nao entra mais nela.","color":"gray"}]
