# Comeca a copia. Ela nao acontece aqui: a area marcada tem 470x126x470 blocos,
# mil vezes o limite de um /clone so, e os chunks dela nem estao carregados —
# com o mapa vazio o /clone copia ar e nao reclama de nada.
#
# Por isso o mapa vai em pedacos de 80x80: cada pedaco e preso com /forceload,
# espera oito tiques para o disco entregar os chunks, e so entao e copiado e
# solto. Um pedaco por vez mantem 25 chunks na memoria, e nao os 900 do mapa
# inteiro — o servidor tem 1500M e nao aguentaria os 900.
execute if score #ativo gcctf matches 1 run tellraw @s [{"text":"Ja tem uma copia em andamento. Espere ela terminar.","color":"red"}]
execute if score #ativo gcctf matches 1 run return 0

execute store result score #cminx gcctf run data get storage gcctf:arena minx
execute store result score #cmaxx gcctf run data get storage gcctf:arena maxx
execute store result score #cminz gcctf run data get storage gcctf:arena minz
execute store result score #cmaxz gcctf run data get storage gcctf:arena maxz
data modify storage gcctf:corte y1 set from storage gcctf:arena miny
data modify storage gcctf:corte y2 set from storage gcctf:arena maxy
data modify storage gcctf:corte by1 set from storage gcctf:arena bkpy
data modify storage gcctf:corte by2 set from storage gcctf:arena bkpymax

# O selo do save antigo cai agora: se esta copia parar no meio, o /reset tem de
# recusar em vez de devolver um backup pela metade.
data remove storage gcctf:arena salvo
scoreboard players operation #tx gcctf = #cminx gcctf
scoreboard players operation #tz gcctf = #cminz gcctf
scoreboard players set #modo gcctf 1
scoreboard players set #ativo gcctf 1
function gcctf:tile_preparar
tellraw @a [{"text":"[MegaGames] Salvando o mapa...","color":"green"}]
