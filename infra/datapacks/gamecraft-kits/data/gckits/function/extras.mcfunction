# Perola do ender: fuga e reposicionamento, 1 kit em 4.
execute store result score #x1 gc_r run random value 1..4
execute if score #x1 gc_r matches 1 run give @s minecraft:ender_pearl 2

# Arco e flecha sao sorteados SEPARADOS, de proposito: pode vir arco sem flecha
# e flecha sem arco. Quem morre larga o que tinha, entao juntar as duas metades
# no chao vira uma jogada — e o arco sozinho ainda serve para empurrar com o
# knockback se alguem tiver deixado flecha por perto.
execute store result score #x2 gc_r run random value 1..3
execute if score #x2 gc_r matches 1 run give @s minecraft:bow 1
execute store result score #x3 gc_r run random value 1..3
execute if score #x3 gc_r matches 1 run give @s minecraft:arrow 16
