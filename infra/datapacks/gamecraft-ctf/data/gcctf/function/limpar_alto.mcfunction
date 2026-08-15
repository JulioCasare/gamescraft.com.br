# Esvazia a faixa acima do mapa, de -236 182 -237 ate 239 267 239.
#
# E ali que vivia a copia de seguranca do /save, e ela tapava o ceu de todos os
# sinalizadores: beacon so acende com a coluna livre ate o topo do mundo.
#
# Vai pelos mesmos pedacos de 80x80 do save. Tentar a faixa inteira de uma vez
# — 19,5 milhoes de blocos — derruba o servidor: o WorldEdit carrega tudo na
# memoria e os 1500M acabam antes do fim.
execute if score #ativo gcctf matches 1 run tellraw @s [{"text":"Ja tem uma operacao em andamento. Espere ela terminar.","color":"red"}]
execute if score #ativo gcctf matches 1 run return 0

scoreboard players set #cminx gcctf -236
scoreboard players set #cmaxx gcctf 239
scoreboard players set #cminz gcctf -237
scoreboard players set #cmaxz gcctf 239
data modify storage gcctf:tiles molde.y1 set value 182
data modify storage gcctf:tiles molde.y2 set value 267
data modify storage gcctf:tiles molde.by1 set value 182
data modify storage gcctf:tiles molde.by2 set value 267

scoreboard players operation #tx gcctf = #cminx gcctf
scoreboard players operation #tz gcctf = #cminz gcctf
scoreboard players set #modo gcctf 3
scoreboard players set #ativo gcctf 1

function gcctf:tile_pegar
data modify storage gcctf:tiles atual set from storage gcctf:tiles novo
function gcctf:tile_pegar
scoreboard players operation #temprox gcctf = #achou gcctf
data modify storage gcctf:tiles proximo set from storage gcctf:tiles novo
scoreboard players set #espera gcctf 10
tellraw @a [{"text":"[MegaGames] Limpando o espaco acima do mapa...","color":"gold"}]
