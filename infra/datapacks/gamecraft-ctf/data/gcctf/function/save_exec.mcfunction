# Comeca a copia. Ela nao acontece aqui: a area marcada tem milhoes de blocos,
# muito alem do limite de um /clone so, e os chunks dela nem estao carregados —
# /clone em chunk fora da memoria copia ar e nao reclama de nada.
#
# Por isso o mapa vai em pedacos de 80x80, um por tique, cada um preso com
# /forceload antes e solto depois. Ficam dois pedacos presos por vez: o que esta
# sendo copiado e o seguinte, que carrega do disco enquanto isso.
execute if score #ativo gcctf matches 1 run tellraw @s [{"text":"Ja tem uma copia em andamento. Espere ela terminar.","color":"red"}]
execute if score #ativo gcctf matches 1 run return 0

execute store result score #cminx gcctf run data get storage gcctf:arena minx
execute store result score #cmaxx gcctf run data get storage gcctf:arena maxx
execute store result score #cminz gcctf run data get storage gcctf:arena minz
execute store result score #cmaxz gcctf run data get storage gcctf:arena maxz
data modify storage gcctf:tiles molde.y1 set from storage gcctf:arena miny
data modify storage gcctf:tiles molde.y2 set from storage gcctf:arena maxy

# O selo do save antigo cai agora: se esta copia parar no meio, o /reset tem de
# recusar em vez de devolver um backup pela metade.
data remove storage gcctf:arena salvo
scoreboard players operation #tx gcctf = #cminx gcctf
scoreboard players operation #tz gcctf = #cminz gcctf
scoreboard players set #modo gcctf 1
scoreboard players set #ativo gcctf 1

function gcctf:tile_pegar
data modify storage gcctf:tiles atual set from storage gcctf:tiles novo
function gcctf:tile_pegar
scoreboard players operation #temprox gcctf = #achou gcctf
data modify storage gcctf:tiles proximo set from storage gcctf:tiles novo
scoreboard players set #espera gcctf 10
tellraw @a [{"text":"[MegaGames] Salvando o mapa...","color":"green"}]
