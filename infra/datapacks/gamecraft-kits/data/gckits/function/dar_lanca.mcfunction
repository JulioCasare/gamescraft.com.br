# Material em #m; #l diz se a lanca vem com Lunge (so nas bandas de armadura
# fraca, onde o arremesso e a compensacao por nao ter protecao).
execute if score #l gc_r matches 0 if score #m gc_r matches 1 run give @s minecraft:wooden_spear 1
execute if score #l gc_r matches 0 if score #m gc_r matches 2 run give @s minecraft:stone_spear 1
execute if score #l gc_r matches 0 if score #m gc_r matches 3 run give @s minecraft:iron_spear 1
execute if score #l gc_r matches 0 if score #m gc_r matches 4 run give @s minecraft:diamond_spear 1
execute if score #l gc_r matches 1 if score #m gc_r matches 1 run give @s minecraft:wooden_spear[minecraft:enchantments={"minecraft:lunge":2}] 1
execute if score #l gc_r matches 1 if score #m gc_r matches 2 run give @s minecraft:stone_spear[minecraft:enchantments={"minecraft:lunge":2}] 1
execute if score #l gc_r matches 1 if score #m gc_r matches 3 run give @s minecraft:iron_spear[minecraft:enchantments={"minecraft:lunge":2}] 1
execute if score #l gc_r matches 1 if score #m gc_r matches 4 run give @s minecraft:diamond_spear[minecraft:enchantments={"minecraft:lunge":2}] 1
