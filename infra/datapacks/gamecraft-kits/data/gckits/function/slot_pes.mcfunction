execute store result score #p gc_r run random value 1..5
execute if score #p gc_r matches 1 run item replace entity @s armor.feet with minecraft:leather_boots
execute if score #p gc_r matches 2 run item replace entity @s armor.feet with minecraft:golden_boots
execute if score #p gc_r matches 3 run item replace entity @s armor.feet with minecraft:chainmail_boots
execute if score #p gc_r matches 4 run item replace entity @s armor.feet with minecraft:iron_boots
execute if score #p gc_r matches 5 run item replace entity @s armor.feet with minecraft:diamond_boots
scoreboard players operation @s gc_s += #p gc_r
