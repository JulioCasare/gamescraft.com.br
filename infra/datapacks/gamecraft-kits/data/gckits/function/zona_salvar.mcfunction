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

# Caixa folgada, 3 blocos maior de cada lado. Ela decide onde e "bem fora" da
# area: so ali a posicao do jogador e guardada como ponto de retorno. Sem essa
# folga, quem estava colado na borda era devolvido para dentro da propria area.
scoreboard players operation #minx2 gc_zone = #minx gc_zone
scoreboard players remove #minx2 gc_zone 3
scoreboard players operation #miny2 gc_zone = #miny gc_zone
scoreboard players remove #miny2 gc_zone 3
scoreboard players operation #minz2 gc_zone = #minz gc_zone
scoreboard players remove #minz2 gc_zone 3
scoreboard players operation #dx2 gc_zone = #dx gc_zone
scoreboard players add #dx2 gc_zone 6
scoreboard players operation #dy2 gc_zone = #dy gc_zone
scoreboard players add #dy2 gc_zone 6
scoreboard players operation #dz2 gc_zone = #dz gc_zone
scoreboard players add #dz2 gc_zone 6

execute store result storage gckits:zona minx int 1 run scoreboard players get #minx gc_zone
execute store result storage gckits:zona miny int 1 run scoreboard players get #miny gc_zone
execute store result storage gckits:zona minz int 1 run scoreboard players get #minz gc_zone
execute store result storage gckits:zona dx int 1 run scoreboard players get #dx gc_zone
execute store result storage gckits:zona dy int 1 run scoreboard players get #dy gc_zone
execute store result storage gckits:zona dz int 1 run scoreboard players get #dz gc_zone
execute store result storage gckits:zona minx2 int 1 run scoreboard players get #minx2 gc_zone
execute store result storage gckits:zona miny2 int 1 run scoreboard players get #miny2 gc_zone
execute store result storage gckits:zona minz2 int 1 run scoreboard players get #minz2 gc_zone
execute store result storage gckits:zona dx2 int 1 run scoreboard players get #dx2 gc_zone
execute store result storage gckits:zona dy2 int 1 run scoreboard players get #dy2 gc_zone
execute store result storage gckits:zona dz2 int 1 run scoreboard players get #dz2 gc_zone
tellraw @s [{"text":"Area segura salva. ","color":"green"},{"text":"Quem esta em combate nao entra mais nela.","color":"gray"}]
