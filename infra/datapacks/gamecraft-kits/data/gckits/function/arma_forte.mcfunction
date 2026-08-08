# Armadura fraquissima (4 a 8): recebe a arma mais perigosa e fuga.
execute if score #w gc_r matches 1 run give @s minecraft:diamond_spear[minecraft:enchantments={"minecraft:lunge":3}] 1
execute unless score #w gc_r matches 1 run give @s minecraft:diamond_sword[minecraft:enchantments={"minecraft:sharpness":2}] 1
give @s minecraft:golden_apple 2
give @s minecraft:potion[minecraft:potion_contents={potion:"minecraft:swiftness"}] 1
