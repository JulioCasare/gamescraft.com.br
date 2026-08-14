# Limpa o que ficou solto na area — item no chao, TNT no ar — e arma o cursor.
# A devolucao dos blocos vem em pedacos, igual ao save.
execute if score #ativo gcctf matches 1 run tellraw @s [{"text":"Ja tem uma copia em andamento. Espere ela terminar.","color":"red"}]
execute if score #ativo gcctf matches 1 run return 0

$kill @e[type=minecraft:item,x=$(minx),dx=$(dx),y=$(miny),dy=$(dy),z=$(minz),dz=$(dz)]
$kill @e[type=minecraft:tnt,x=$(minx),dx=$(dx),y=$(miny),dy=$(dy),z=$(minz),dz=$(dz)]

execute store result score #cminx gcctf run data get storage gcctf:arena minx
execute store result score #cmaxx gcctf run data get storage gcctf:arena maxx
execute store result score #cminz gcctf run data get storage gcctf:arena minz
execute store result score #cmaxz gcctf run data get storage gcctf:arena maxz
data modify storage gcctf:tiles molde.y1 set from storage gcctf:arena miny
data modify storage gcctf:tiles molde.y2 set from storage gcctf:arena maxy
data modify storage gcctf:tiles molde.by1 set from storage gcctf:arena bkpy
data modify storage gcctf:tiles molde.by2 set from storage gcctf:arena bkpymax

scoreboard players operation #tx gcctf = #cminx gcctf
scoreboard players operation #tz gcctf = #cminz gcctf
scoreboard players set #modo gcctf 2
scoreboard players set #ativo gcctf 1

function gcctf:tile_pegar
data modify storage gcctf:tiles atual set from storage gcctf:tiles novo
function gcctf:tile_pegar
scoreboard players operation #temprox gcctf = #achou gcctf
data modify storage gcctf:tiles proximo set from storage gcctf:tiles novo
scoreboard players set #espera gcctf 10
tellraw @a [{"text":"[MegaGames] Restaurando o mapa...","color":"aqua"}]
