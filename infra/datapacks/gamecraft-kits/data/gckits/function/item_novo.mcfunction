# Item que acabou de cair no chao: e o rastro de um bloco quebrado. Marca como
# visto, guarda onde caiu e manda consertar so ali em volta.
tag @s add gc_visto
execute store result score #ix gc_zone run data get entity @s Pos[0] 1
execute store result score #iy gc_zone run data get entity @s Pos[1] 1
execute store result score #iz gc_zone run data get entity @s Pos[2] 1
execute unless entity @a[tag=gc_build] if data storage gckits:arena {salvo:1b} run function gckits:reparo_local_prep
