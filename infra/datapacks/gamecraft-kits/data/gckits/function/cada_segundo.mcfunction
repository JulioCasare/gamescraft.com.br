# Tudo que roda uma vez por segundo fica junto aqui, para o tick nao virar uma
# colecao de contadores soltos.
scoreboard players set #tz gc_zone 0

# Durabilidade de quem esta na area segura.
function gckits:zona_durabilidade

# Filhote de cubo de magma nasce sem marca nenhuma. Adota ele, senao vira
# entulho fora da conta do teto e sem as regras dos outros.
tag @e[type=minecraft:magma_cube,tag=!gc_mob] add gc_mob

# Mob que entrou na area segura volta para o meio da arena. A area e refugio de
# tudo, nao so de jogador.
execute as @e[tag=gc_mob] at @s run function gckits:mob_zona with storage gckits:zona

# Sol nao mata os mobs da arena. Parte do mapa e a ceu aberto e zumbi e
# esqueleto se acabariam sozinhos antes de encontrar alguem. Lava e fogo
# continuam valendo: quem esta dentro deles nao e apagado.
execute as @e[tag=gc_mob] at @s unless block ~ ~ ~ minecraft:lava unless block ~ ~ ~ minecraft:fire run data merge entity @s {Fire:-20s}

# Nascimento de mob no meio da arena, a cada 5 segundos.
scoreboard players add #ms gc_zone 1
execute if score #ms gc_zone matches 5.. run function gckits:mobs
