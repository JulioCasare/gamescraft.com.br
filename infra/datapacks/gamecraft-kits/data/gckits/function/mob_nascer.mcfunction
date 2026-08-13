# Um em trinta e o zumbi gigante. O sorteio dele vem antes e separado: se
# entrasse na mesma tabela dos outros, cada mudanca de peso nos comuns mexeria
# na raridade dele sem querer.
execute store result score #mg gc_r run random value 1..30
execute if score #mg gc_r matches 1 run function gckits:mob_gigante

# Sorteio do tipo, em quatorze partes. Os que mais atrapalham vem menos.
# PersistenceRequired mantem o mob no lugar — sem isso ele some sozinho quando
# ninguem esta perto, e o teto de mobs nunca fecha.
execute if score #mg gc_r matches 2.. store result score #mt gc_r run random value 1..14
execute if score #mt gc_r matches 1..3 run summon minecraft:zombie ~ ~ ~ {Tags:["gc_mob","gc_novo"],PersistenceRequired:1b}
execute if score #mt gc_r matches 4..6 run summon minecraft:skeleton ~ ~ ~ {Tags:["gc_mob","gc_novo"],PersistenceRequired:1b}
execute if score #mt gc_r matches 7..8 run summon minecraft:spider ~ ~ ~ {Tags:["gc_mob","gc_novo"],PersistenceRequired:1b}
# Size 3 e o cubo de magma grande.
execute if score #mt gc_r matches 9..10 run summon minecraft:magma_cube ~ ~ ~ {Tags:["gc_mob","gc_novo"],PersistenceRequired:1b,Size:3}
execute if score #mt gc_r matches 11 run summon minecraft:vex ~ ~ ~ {Tags:["gc_mob","gc_novo"],PersistenceRequired:1b}
execute if score #mt gc_r matches 12 run summon minecraft:wither_skeleton ~ ~ ~ {Tags:["gc_mob","gc_novo"],PersistenceRequired:1b}
# Devastador e zoglin ja nascem com o dobro da vida: 200 no lugar de 100, e 80
# no lugar de 40. A vida cheia tem que ser escrita junto, senao o bicho nasce
# com a vida antiga dentro do limite novo.
execute if score #mt gc_r matches 13 run summon minecraft:ravager ~ ~ ~ {Tags:["gc_mob","gc_novo"],PersistenceRequired:1b,attributes:[{id:"minecraft:max_health",base:200}],Health:200f}
execute if score #mt gc_r matches 14 run summon minecraft:zoglin ~ ~ ~ {Tags:["gc_mob","gc_novo"],PersistenceRequired:1b,attributes:[{id:"minecraft:max_health",base:80}],Health:80f}

execute as @e[tag=gc_novo] run function gckits:mob_equipar
