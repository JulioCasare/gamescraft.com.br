# Diamante: aguenta muito, machuca pouco. Ganha por desgaste, nao por dano.
clear @s
item replace entity @s armor.head with minecraft:diamond_helmet
item replace entity @s armor.chest with minecraft:diamond_chestplate
item replace entity @s armor.legs with minecraft:diamond_leggings
item replace entity @s armor.feet with minecraft:diamond_boots
give @s minecraft:wooden_sword 1
give @s minecraft:shield 1
give @s minecraft:cooked_beef 4
tellraw @s {"text":"Kit sorteado: Tanque — diamante, espada de madeira","color":"aqua"}
