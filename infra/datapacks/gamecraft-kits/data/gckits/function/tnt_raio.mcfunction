# Raio de meio bloco por passo, ate 6 blocos de distancia do jogador: e o
# alcance de colocar bloco. Para no primeiro TNT que encontrar.
execute if block ~ ~ ~ minecraft:tnt run function gckits:tnt_prime
execute unless block ~ ~ ~ minecraft:tnt positioned ^ ^ ^0.5 if entity @s[distance=..6] run function gckits:tnt_raio
