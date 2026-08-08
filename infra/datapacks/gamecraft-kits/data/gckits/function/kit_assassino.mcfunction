# Sem armadura nenhuma: o kit mais fragil e o que mais machuca. Vence quem
# acerta primeiro e foge bem.
clear @s
give @s minecraft:diamond_sword[minecraft:enchantments={"minecraft:sharpness":2}] 1
give @s minecraft:potion[minecraft:potion_contents={potion:"minecraft:swiftness"}] 2
give @s minecraft:golden_apple 2
give @s minecraft:cooked_beef 4
tellraw @s {"text":"Kit sorteado: Assassino — sem armadura, espada de diamante","color":"aqua"}
