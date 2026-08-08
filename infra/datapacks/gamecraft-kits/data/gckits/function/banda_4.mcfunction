# Armadura 17 a 20 (blindado): armas de madeira ou pedra. Aguenta muito,
# machuca pouco.
scoreboard players set #l gc_r 0
execute store result score #m gc_r run random value 1..2
function gckits:dar_espada
execute store result score #m gc_r run random value 1..2
function gckits:dar_lanca
scoreboard players set #m gc_r 1
execute if score #ta gc_r matches 1 run function gckits:dar_machado
execute if score #tp gc_r matches 1 run function gckits:dar_picareta
