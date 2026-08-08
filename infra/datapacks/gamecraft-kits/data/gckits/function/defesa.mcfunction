# Escudo ou totem. O totem e bem mais forte que o escudo, entao sai em 1 de
# cada 4 kits — e a carta que vira uma luta perdida.
execute store result score #d gc_r run random value 1..4
execute if score #d gc_r matches 1 run give @s minecraft:totem_of_undying 1
execute unless score #d gc_r matches 1 run give @s minecraft:shield 1
