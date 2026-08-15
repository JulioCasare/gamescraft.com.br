# Guarda os dois cantos em forma de minimo, maximo e tamanho — o clone precisa
# de minimo e maximo, os seletores precisam de minimo e tamanho.
#
# O backup nao mora mais acima do mapa, e sim cinco mil blocos ao lado (ver o
# #desvio no load): por cima ele tapava o feixe de todos os sinalizadores, e
# marcar a arena alta demais fazia origem e destino se cruzarem, o que o /clone
# recusa. Ao lado, qualquer marcacao funciona.
scoreboard players operation #aminx gcctf = #ax1 gcctf
scoreboard players operation #aminx gcctf < #ax2 gcctf
scoreboard players operation #amaxx gcctf = #ax1 gcctf
scoreboard players operation #amaxx gcctf > #ax2 gcctf
scoreboard players operation #aminy gcctf = #ay1 gcctf
scoreboard players operation #aminy gcctf < #ay2 gcctf
scoreboard players operation #amaxy gcctf = #ay1 gcctf
scoreboard players operation #amaxy gcctf > #ay2 gcctf
scoreboard players operation #aminz gcctf = #az1 gcctf
scoreboard players operation #aminz gcctf < #az2 gcctf
scoreboard players operation #amaxz gcctf = #az1 gcctf
scoreboard players operation #amaxz gcctf > #az2 gcctf

scoreboard players operation #adx gcctf = #amaxx gcctf
scoreboard players operation #adx gcctf -= #aminx gcctf
scoreboard players operation #ady gcctf = #amaxy gcctf
scoreboard players operation #ady gcctf -= #aminy gcctf
scoreboard players operation #adz gcctf = #amaxz gcctf
scoreboard players operation #adz gcctf -= #aminz gcctf

scoreboard players operation #abkpy gcctf = #aminy gcctf
scoreboard players add #abkpy gcctf 150
scoreboard players operation #abkpymax gcctf = #abkpy gcctf
scoreboard players operation #abkpymax gcctf += #ady gcctf

execute store result storage gcctf:arena minx int 1 run scoreboard players get #aminx gcctf
execute store result storage gcctf:arena miny int 1 run scoreboard players get #aminy gcctf
execute store result storage gcctf:arena minz int 1 run scoreboard players get #aminz gcctf
execute store result storage gcctf:arena maxx int 1 run scoreboard players get #amaxx gcctf
execute store result storage gcctf:arena maxy int 1 run scoreboard players get #amaxy gcctf
execute store result storage gcctf:arena maxz int 1 run scoreboard players get #amaxz gcctf
execute store result storage gcctf:arena dx int 1 run scoreboard players get #adx gcctf
execute store result storage gcctf:arena dy int 1 run scoreboard players get #ady gcctf
execute store result storage gcctf:arena dz int 1 run scoreboard players get #adz gcctf
execute store result storage gcctf:arena bkpy int 1 run scoreboard players get #abkpy gcctf
execute store result storage gcctf:arena bkpymax int 1 run scoreboard players get #abkpymax gcctf
tellraw @s [{"text":"Area definida. ","color":"green"},{"text":"Use /save para guardar o estado atual.","color":"gray"}]
