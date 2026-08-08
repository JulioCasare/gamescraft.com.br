# Couro: a pior armadura, compensada pela lanca com Lunge — a vitrine do 26.2.
clear @s
item replace entity @s armor.head with minecraft:leather_helmet
item replace entity @s armor.chest with minecraft:leather_chestplate
item replace entity @s armor.legs with minecraft:leather_leggings
item replace entity @s armor.feet with minecraft:leather_boots
give @s minecraft:iron_spear[minecraft:enchantments={"minecraft:lunge":3}] 1
give @s minecraft:cooked_beef 8
tellraw @s {"text":"Kit sorteado: Lanceiro — couro, lanca com Lunge 3","color":"aqua"}
