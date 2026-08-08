# Escudo na maioria das vezes. O totem vale muito mais — vira uma vida extra —
# entao sai em 1 de cada 8 kits, nao mais que isso.
execute store result score #d gc_r run random value 1..8
execute if score #d gc_r matches 1 run give @s minecraft:totem_of_undying 1
execute unless score #d gc_r matches 1 run give @s minecraft:shield 1
