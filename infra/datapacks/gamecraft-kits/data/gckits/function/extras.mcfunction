# Projetil de vento: todo kit leva. Empurra quem chega perto e ajuda a subir —
# e a ferramenta de reposicionamento que todo mundo tem.
give @s minecraft:wind_charge 16

# Tridente com Lealdade (volta para a mao): 1 kit em 3.
execute store result score #x0 gc_r run random value 1..3
execute if score #x0 gc_r matches 1 run give @s minecraft:trident 1

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

# Teia: prende quem esta perseguindo e corta queda. Como todo kit tem espada,
# quem for preso consegue se soltar — nao trava a luta, so muda o ritmo.
execute store result score #x6 gc_r run random value 1..3
execute if score #x6 gc_r matches 1 run give @s minecraft:cobweb 8

# TNT que acende sozinha ao ser colocada (ver gckits:tnt_acende). Nao precisa
# de isqueiro: colocou, conta 2 segundos e explode.
execute store result score #x7 gc_r run random value 1..5
execute if score #x7 gc_r matches 1 run give @s minecraft:tnt 4
