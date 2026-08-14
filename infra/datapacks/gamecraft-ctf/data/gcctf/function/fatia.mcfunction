# Um pedaco por vez. Enquanto a espera corre, os chunks presos no tile_preparar
# estao vindo do disco; copiar antes disso daria uma copia de ar.
execute if score #espera gcctf matches 1.. run scoreboard players remove #espera gcctf 1
execute if score #espera gcctf matches 1.. run return 0

execute if score #modo gcctf matches 1 run function gcctf:fatia_save with storage gcctf:corte
execute if score #modo gcctf matches 2 run function gcctf:fatia_reset with storage gcctf:corte
function gcctf:tile_soltar with storage gcctf:corte

scoreboard players operation #tx gcctf = #tx2 gcctf
scoreboard players add #tx gcctf 1
execute if score #tx gcctf > #cmaxx gcctf run function gcctf:tile_linha
execute if score #ativo gcctf matches 1 run function gcctf:tile_preparar
