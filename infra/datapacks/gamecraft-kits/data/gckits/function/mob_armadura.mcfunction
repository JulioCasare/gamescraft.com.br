# Um material para o conjunto todo, puxando para os baratos: couro 4 em 10,
# malha 3, ferro 2, ouro 1. Diamante fica de fora — mob de arena e estorvo no
# meio da briga, nao chefe.
execute store result score #mm gc_r run random value 1..10

# Cada peca entra ou nao, meio a meio. E por isso que sai gente de capacete e
# bota, ou so peitoral, e nunca sempre o conjunto inteiro.
execute store result score #p1 gc_r run random value 1..2
execute store result score #p2 gc_r run random value 1..2
execute store result score #p3 gc_r run random value 1..2
execute store result score #p4 gc_r run random value 1..2

execute if score #p1 gc_r matches 1 if score #mm gc_r matches 1..4 run item replace entity @s armor.head with minecraft:leather_helmet
execute if score #p1 gc_r matches 1 if score #mm gc_r matches 5..7 run item replace entity @s armor.head with minecraft:chainmail_helmet
execute if score #p1 gc_r matches 1 if score #mm gc_r matches 8..9 run item replace entity @s armor.head with minecraft:iron_helmet
execute if score #p1 gc_r matches 1 if score #mm gc_r matches 10 run item replace entity @s armor.head with minecraft:golden_helmet

execute if score #p2 gc_r matches 1 if score #mm gc_r matches 1..4 run item replace entity @s armor.chest with minecraft:leather_chestplate
execute if score #p2 gc_r matches 1 if score #mm gc_r matches 5..7 run item replace entity @s armor.chest with minecraft:chainmail_chestplate
execute if score #p2 gc_r matches 1 if score #mm gc_r matches 8..9 run item replace entity @s armor.chest with minecraft:iron_chestplate
execute if score #p2 gc_r matches 1 if score #mm gc_r matches 10 run item replace entity @s armor.chest with minecraft:golden_chestplate

execute if score #p3 gc_r matches 1 if score #mm gc_r matches 1..4 run item replace entity @s armor.legs with minecraft:leather_leggings
execute if score #p3 gc_r matches 1 if score #mm gc_r matches 5..7 run item replace entity @s armor.legs with minecraft:chainmail_leggings
execute if score #p3 gc_r matches 1 if score #mm gc_r matches 8..9 run item replace entity @s armor.legs with minecraft:iron_leggings
execute if score #p3 gc_r matches 1 if score #mm gc_r matches 10 run item replace entity @s armor.legs with minecraft:golden_leggings

execute if score #p4 gc_r matches 1 if score #mm gc_r matches 1..4 run item replace entity @s armor.feet with minecraft:leather_boots
execute if score #p4 gc_r matches 1 if score #mm gc_r matches 5..7 run item replace entity @s armor.feet with minecraft:chainmail_boots
execute if score #p4 gc_r matches 1 if score #mm gc_r matches 8..9 run item replace entity @s armor.feet with minecraft:iron_boots
execute if score #p4 gc_r matches 1 if score #mm gc_r matches 10 run item replace entity @s armor.feet with minecraft:golden_boots
