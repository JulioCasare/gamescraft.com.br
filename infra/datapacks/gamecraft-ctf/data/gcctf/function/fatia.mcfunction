# Um pedaco por tique. A espera so acontece no primeiro: dali em diante o
# proximo ja vem carregando desde o tique anterior.
execute if score #espera gcctf matches 1.. run scoreboard players remove #espera gcctf 1
execute if score #espera gcctf matches 1.. run return 0

execute if score #modo gcctf matches 1 run function gcctf:fatia_save with storage gcctf:tiles atual
execute if score #modo gcctf matches 2 run function gcctf:fatia_reset with storage gcctf:tiles atual
function gcctf:tile_soltar with storage gcctf:tiles atual

execute if score #temprox gcctf matches 0 run function gcctf:fatia_fim
execute if score #ativo gcctf matches 1 run function gcctf:avancar
