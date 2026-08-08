# Ou maca dourada, ou carne — nunca os dois. A maca cura na hora e vale mais,
# entao a quantidade cai conforme a armadura sobe; a carne vem sempre em 16.
execute store result score #c gc_r run random value 1..2
execute if score #c gc_r matches 2 run give @s minecraft:cooked_beef 16
execute if score #c gc_r matches 1 if score @s gc_s matches ..8 run give @s minecraft:golden_apple 5
execute if score #c gc_r matches 1 if score @s gc_s matches 9..12 run give @s minecraft:golden_apple 4
execute if score #c gc_r matches 1 if score @s gc_s matches 13..16 run give @s minecraft:golden_apple 3
execute if score #c gc_r matches 1 if score @s gc_s matches 17.. run give @s minecraft:golden_apple 2
# Armadura fraquissima ganha tambem a fuga.
execute if score @s gc_s matches ..8 run give @s minecraft:potion[minecraft:potion_contents={potion:"minecraft:swiftness"}] 1
