# Machado bate mais que espada do mesmo material, entao a faixa dele ja vem um
# nivel abaixo da faixa das armas (definida na banda). Material em #m.
execute if score #m gc_r matches 1 run give @s minecraft:wooden_axe 1
execute if score #m gc_r matches 2 run give @s minecraft:stone_axe 1
execute if score #m gc_r matches 3 run give @s minecraft:iron_axe 1
execute if score #m gc_r matches 4 run give @s minecraft:diamond_axe 1
