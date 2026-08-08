# Perola do ender: ou vem o punhado inteiro, ou nao vem nada. 1 kit em 4 leva
# quatro perolas — o suficiente para fugir de verdade e ainda cacar alguem.
execute store result score #x1 gc_r run random value 1..4
execute if score #x1 gc_r matches 1 run give @s minecraft:ender_pearl 4

# Arco e flecha sao sorteados SEPARADOS, de proposito: pode vir arco sem flecha
# e flecha sem arco. Como quem morre larga o que tinha, juntar as duas metades
# no chao vira uma jogada.
execute store result score #x2 gc_r run random value 1..3
execute if score #x2 gc_r matches 1 run give @s minecraft:bow 1
execute store result score #x3 gc_r run random value 1..3
execute if score #x3 gc_r matches 1 run give @s minecraft:arrow 16

# Balde de agua: salva de queda, apaga fogo e empurra quem esta na beirada.
execute store result score #x4 gc_r run random value 1..3
execute if score #x4 gc_r matches 1 run give @s minecraft:water_bucket 1

# Balde de lava e a ferramenta mais cruel do modo — mata devagar e bloqueia
# passagem. Sai bem mais raro que o de agua.
execute store result score #x5 gc_r run random value 1..6
execute if score #x5 gc_r matches 1 run give @s minecraft:lava_bucket 1
