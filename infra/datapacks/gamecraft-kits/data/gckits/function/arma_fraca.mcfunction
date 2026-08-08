# Armadura pesada (17 a 20): aguenta muito, entao machuca pouco.
execute if score #w gc_r matches 1 run give @s minecraft:stone_spear[minecraft:enchantments={"minecraft:lunge":1}] 1
execute unless score #w gc_r matches 1 run give @s minecraft:stone_sword 1
give @s minecraft:shield 1
