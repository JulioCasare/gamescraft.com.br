# Ferro: o kit equilibrado, sem truque nenhum. Serve de regua para os outros.
clear @s
item replace entity @s armor.head with minecraft:iron_helmet
item replace entity @s armor.chest with minecraft:iron_chestplate
item replace entity @s armor.legs with minecraft:iron_leggings
item replace entity @s armor.feet with minecraft:iron_boots
give @s minecraft:iron_sword 1
give @s minecraft:shield 1
give @s minecraft:cooked_beef 6
tellraw @s {"text":"Kit sorteado: Guerreiro — ferro, espada de ferro","color":"aqua"}
