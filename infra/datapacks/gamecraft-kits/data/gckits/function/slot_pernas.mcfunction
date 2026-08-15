execute store result score #p gc_r run random value 1..5
# Teto de duas pecas de diamante por kit: a terceira vira ferro.
execute if score #p gc_r matches 5 if score #dima gc_r matches 2.. run scoreboard players set #p gc_r 4
execute if score #p gc_r matches 5 run scoreboard players add #dima gc_r 1
execute if score #p gc_r matches 1 run item replace entity @s armor.legs with minecraft:leather_leggings
execute if score #p gc_r matches 2 run item replace entity @s armor.legs with minecraft:golden_leggings
execute if score #p gc_r matches 3 run item replace entity @s armor.legs with minecraft:chainmail_leggings
execute if score #p gc_r matches 4 run item replace entity @s armor.legs with minecraft:iron_leggings
execute if score #p gc_r matches 5 run item replace entity @s armor.legs with minecraft:diamond_leggings
scoreboard players operation @s gc_s += #p gc_r
