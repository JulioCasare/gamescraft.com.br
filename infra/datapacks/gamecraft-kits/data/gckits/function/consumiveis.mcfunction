# Carne sempre, para ninguem perder luta por fome.
give @s minecraft:cooked_beef 16
# Macas douradas de 2 a 5, na razao inversa da armadura: quem veio de couro
# se cura mais vezes; quem veio de diamante ja aguenta por conta da protecao.
execute if score @s gc_s matches ..8 run give @s minecraft:golden_apple 5
execute if score @s gc_s matches 9..12 run give @s minecraft:golden_apple 4
execute if score @s gc_s matches 13..16 run give @s minecraft:golden_apple 3
execute if score @s gc_s matches 17.. run give @s minecraft:golden_apple 2
# Armadura fraquissima ganha tambem a fuga.
execute if score @s gc_s matches ..8 run give @s minecraft:potion[minecraft:potion_contents={potion:"minecraft:swiftness"}] 1
