# Procura agua ou lava na linha do olhar e marca a fonte para secar depois.
# So a fonte precisa de marca: tirando ela, o que escorreu some sozinho.
execute if block ~ ~ ~ minecraft:water run function gckits:marcar_bloco
execute if block ~ ~ ~ minecraft:lava run function gckits:marcar_bloco
execute unless block ~ ~ ~ minecraft:water unless block ~ ~ ~ minecraft:lava positioned ^ ^ ^0.5 if entity @s[distance=..6] run function gckits:fluido_raio
