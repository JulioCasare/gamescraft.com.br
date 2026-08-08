# Picareta e utilidade, nao arma (dano baixo em qualquer material), entao o
# material dela e sorteado livre, sem passar pelo orcamento.
execute store result score #mp gc_r run random value 1..4
execute if score #mp gc_r matches 1 run give @s minecraft:wooden_pickaxe 1
execute if score #mp gc_r matches 2 run give @s minecraft:stone_pickaxe 1
execute if score #mp gc_r matches 3 run give @s minecraft:iron_pickaxe 1
execute if score #mp gc_r matches 4 run give @s minecraft:diamond_pickaxe 1
