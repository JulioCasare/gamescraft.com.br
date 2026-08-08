# Armadura 4 a 8 (quase pelado): armas de ferro ou diamante, lanca com Lunge.
scoreboard players set #l gc_r 1
execute store result score #m gc_r run random value 3..4
function gckits:dar_espada
execute store result score #m gc_r run random value 3..4
function gckits:dar_lanca
execute if score #tp gc_r matches 1 run function gckits:dar_picareta
execute store result score #m gc_r run random value 2..3
execute if score #ta gc_r matches 1 run function gckits:dar_machado
