# Machado bate mais que espada do mesmo material, entao entra um nivel abaixo
# da arma principal — senao o kit blindado ganharia dano de graca.
execute if score @s gc_s matches ..8 run give @s minecraft:iron_axe 1
execute if score @s gc_s matches 9..16 run give @s minecraft:stone_axe 1
execute if score @s gc_s matches 17.. run give @s minecraft:wooden_axe 1
