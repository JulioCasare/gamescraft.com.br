# Agilidade ou forca, para beber ou para arremessar. A arremessavel pega o
# time todo em volta, entao vale tanto quanto a bebivel.
execute store result score #pt gc_r run random value 1..4
execute if score #pt gc_r matches 1 run give @s minecraft:potion[minecraft:potion_contents={potion:"minecraft:swiftness"}] 1
execute if score #pt gc_r matches 2 run give @s minecraft:potion[minecraft:potion_contents={potion:"minecraft:strength"}] 1
execute if score #pt gc_r matches 3 run give @s minecraft:splash_potion[minecraft:potion_contents={potion:"minecraft:swiftness"}] 1
execute if score #pt gc_r matches 4 run give @s minecraft:splash_potion[minecraft:potion_contents={potion:"minecraft:strength"}] 1
