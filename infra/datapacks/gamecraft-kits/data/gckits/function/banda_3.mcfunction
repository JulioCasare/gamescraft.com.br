# Armadura 13 a 16: armas de pedra ou ferro, lanca sem encantamento.
scoreboard players set #l gc_r 0
execute store result score #m gc_r run random value 2..3
function gckits:dar_espada
execute store result score #m gc_r run random value 2..3
function gckits:dar_lanca
execute store result score #m gc_r run random value 1..2
execute if score #ta gc_r matches 1 run function gckits:dar_machado
execute if score #tp gc_r matches 1 run function gckits:dar_picareta
