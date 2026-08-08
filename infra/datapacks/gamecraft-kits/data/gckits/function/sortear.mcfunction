# Pool pequeno e curado: cinco kits, todos com chance real de vitoria.
# A regra de equilibrio esta em docs/PVP.md: quanto pior a armadura, mais forte
# o resto do kit.
execute store result score #r gc_r run random value 1..5
execute if score #r gc_r matches 1 run function gckits:kit_lanceiro
execute if score #r gc_r matches 2 run function gckits:kit_arqueiro
execute if score #r gc_r matches 3 run function gckits:kit_tanque
execute if score #r gc_r matches 4 run function gckits:kit_assassino
execute if score #r gc_r matches 5 run function gckits:kit_guerreiro
