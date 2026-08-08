execute store result score #p gc_r run random value 1..5
execute if score #p gc_r matches 1 run item replace entity @s armor.head with minecraft:leather_helmet
execute if score #p gc_r matches 2 run item replace entity @s armor.head with minecraft:golden_helmet
execute if score #p gc_r matches 3 run item replace entity @s armor.head with minecraft:chainmail_helmet
execute if score #p gc_r matches 4 run item replace entity @s armor.head with minecraft:iron_helmet
execute if score #p gc_r matches 5 run item replace entity @s armor.head with minecraft:diamond_helmet
scoreboard players operation @s gc_s += #p gc_r
