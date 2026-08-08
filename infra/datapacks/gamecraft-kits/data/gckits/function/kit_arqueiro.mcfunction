# Malha: armadura media, forca no alcance.
clear @s
item replace entity @s armor.head with minecraft:chainmail_helmet
item replace entity @s armor.chest with minecraft:chainmail_chestplate
item replace entity @s armor.legs with minecraft:chainmail_leggings
item replace entity @s armor.feet with minecraft:chainmail_boots
give @s minecraft:bow[minecraft:enchantments={"minecraft:power":2}] 1
give @s minecraft:arrow 32
give @s minecraft:stone_sword 1
give @s minecraft:cooked_beef 6
tellraw @s {"text":"Kit sorteado: Arqueiro — malha, arco com Power 2","color":"aqua"}
