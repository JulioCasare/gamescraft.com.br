# Guarda os dois cantos em forma de minimo, maximo e tamanho — o clone precisa
# de minimo e maximo, os seletores precisam de minimo e tamanho.
# O backup fica 150 blocos acima da arena, nos mesmos chunks: nao gera terreno
# novo em lugar nenhum e some do caminho de todo mundo.
scoreboard players operation #aminx gc_zone = #ax1 gc_zone
scoreboard players operation #aminx gc_zone < #ax2 gc_zone
scoreboard players operation #amaxx gc_zone = #ax1 gc_zone
scoreboard players operation #amaxx gc_zone > #ax2 gc_zone
scoreboard players operation #aminy gc_zone = #ay1 gc_zone
scoreboard players operation #aminy gc_zone < #ay2 gc_zone
scoreboard players operation #amaxy gc_zone = #ay1 gc_zone
scoreboard players operation #amaxy gc_zone > #ay2 gc_zone
scoreboard players operation #aminz gc_zone = #az1 gc_zone
scoreboard players operation #aminz gc_zone < #az2 gc_zone
scoreboard players operation #amaxz gc_zone = #az1 gc_zone
scoreboard players operation #amaxz gc_zone > #az2 gc_zone

scoreboard players operation #adx gc_zone = #amaxx gc_zone
scoreboard players operation #adx gc_zone -= #aminx gc_zone
scoreboard players operation #ady gc_zone = #amaxy gc_zone
scoreboard players operation #ady gc_zone -= #aminy gc_zone
scoreboard players operation #adz gc_zone = #amaxz gc_zone
scoreboard players operation #adz gc_zone -= #aminz gc_zone

scoreboard players operation #abkpy gc_zone = #aminy gc_zone
scoreboard players add #abkpy gc_zone 150
scoreboard players operation #abkpymax gc_zone = #abkpy gc_zone
scoreboard players operation #abkpymax gc_zone += #ady gc_zone

execute store result storage gckits:arena minx int 1 run scoreboard players get #aminx gc_zone
execute store result storage gckits:arena miny int 1 run scoreboard players get #aminy gc_zone
execute store result storage gckits:arena minz int 1 run scoreboard players get #aminz gc_zone
execute store result storage gckits:arena maxx int 1 run scoreboard players get #amaxx gc_zone
execute store result storage gckits:arena maxy int 1 run scoreboard players get #amaxy gc_zone
execute store result storage gckits:arena maxz int 1 run scoreboard players get #amaxz gc_zone
execute store result storage gckits:arena dx int 1 run scoreboard players get #adx gc_zone
execute store result storage gckits:arena dy int 1 run scoreboard players get #ady gc_zone
execute store result storage gckits:arena dz int 1 run scoreboard players get #adz gc_zone
execute store result storage gckits:arena bkpy int 1 run scoreboard players get #abkpy gc_zone
execute store result storage gckits:arena bkpymax int 1 run scoreboard players get #abkpymax gc_zone
tellraw @s [{"text":"Area da arena definida. ","color":"green"},{"text":"Rode /function gckits:save para guardar o estado atual.","color":"gray"}]
