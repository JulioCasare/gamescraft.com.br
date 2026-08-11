# Sorteio do tipo, em doze partes. Os que mais atrapalham vem menos: vex e
# esqueleto wither sao um em doze cada.
# PersistenceRequired mantem o mob no lugar — sem isso ele some sozinho quando
# ninguem esta perto, e o teto de mobs nunca fecha.
execute store result score #mt gc_r run random value 1..12
execute if score #mt gc_r matches 1..3 run summon minecraft:zombie ~ ~ ~ {Tags:["gc_mob","gc_novo"],PersistenceRequired:1b}
execute if score #mt gc_r matches 4..6 run summon minecraft:skeleton ~ ~ ~ {Tags:["gc_mob","gc_novo"],PersistenceRequired:1b}
execute if score #mt gc_r matches 7..8 run summon minecraft:spider ~ ~ ~ {Tags:["gc_mob","gc_novo"],PersistenceRequired:1b}
# Size 3 e o cubo de magma grande.
execute if score #mt gc_r matches 9..10 run summon minecraft:magma_cube ~ ~ ~ {Tags:["gc_mob","gc_novo"],PersistenceRequired:1b,Size:3}
execute if score #mt gc_r matches 11 run summon minecraft:vex ~ ~ ~ {Tags:["gc_mob","gc_novo"],PersistenceRequired:1b}
execute if score #mt gc_r matches 12 run summon minecraft:wither_skeleton ~ ~ ~ {Tags:["gc_mob","gc_novo"],PersistenceRequired:1b}

execute as @e[tag=gc_novo] run function gckits:mob_equipar
