# Monta uma caixa de 8 blocos em volta do item — o bloco quebrado esta sempre
# perto de onde o item caiu. Sao ~4900 blocos por conserto, contra 300 mil se
# fosse a arena inteira: cabe em qualquer tick sem travar o servidor.
#
# Nao precisa checar se a caixa passou do limite da arena: fora dela o backup
# e ar, e o clone em modo masked nao copia ar.
scoreboard players operation #x1 gc_zone = #ix gc_zone
scoreboard players remove #x1 gc_zone 8
scoreboard players operation #x2 gc_zone = #ix gc_zone
scoreboard players add #x2 gc_zone 8
scoreboard players operation #y1 gc_zone = #iy gc_zone
scoreboard players remove #y1 gc_zone 8
scoreboard players operation #y2 gc_zone = #iy gc_zone
scoreboard players add #y2 gc_zone 8
scoreboard players operation #z1 gc_zone = #iz gc_zone
scoreboard players remove #z1 gc_zone 8
scoreboard players operation #z2 gc_zone = #iz gc_zone
scoreboard players add #z2 gc_zone 8

# O backup mora 150 blocos acima, como definido em arena_salvar_area.
scoreboard players operation #by1 gc_zone = #y1 gc_zone
scoreboard players add #by1 gc_zone 150
scoreboard players operation #by2 gc_zone = #y2 gc_zone
scoreboard players add #by2 gc_zone 150

execute store result storage gckits:local x1 int 1 run scoreboard players get #x1 gc_zone
execute store result storage gckits:local x2 int 1 run scoreboard players get #x2 gc_zone
execute store result storage gckits:local y1 int 1 run scoreboard players get #y1 gc_zone
execute store result storage gckits:local z1 int 1 run scoreboard players get #z1 gc_zone
execute store result storage gckits:local z2 int 1 run scoreboard players get #z2 gc_zone
execute store result storage gckits:local by1 int 1 run scoreboard players get #by1 gc_zone
execute store result storage gckits:local by2 int 1 run scoreboard players get #by2 gc_zone
function gckits:reparo_local with storage gckits:local
