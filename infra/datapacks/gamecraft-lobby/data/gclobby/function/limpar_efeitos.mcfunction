# Tira efeito de pocao de quem chega no lobby: quem sai de uma partida chega com
# o que bebeu ou comeu la, e as particulas ficam girando na praca.
#
# Um a um, e nao "effect clear" de tudo, porque o proprio lobby tem um efeito
# permanente: um bloco de comando do mapa da visao noturna a todo mundo, para a
# praca nunca ficar escura. Limpar tudo apagaria ele a cada segundo e a tela
# piscaria junto.
scoreboard players set #t gcl_t 0
effect clear @a minecraft:speed
effect clear @a minecraft:strength
effect clear @a minecraft:regeneration
effect clear @a minecraft:absorption
effect clear @a minecraft:fire_resistance
effect clear @a minecraft:jump_boost
effect clear @a minecraft:invisibility
effect clear @a minecraft:resistance
effect clear @a minecraft:haste
effect clear @a minecraft:slowness
effect clear @a minecraft:weakness
effect clear @a minecraft:mining_fatigue
effect clear @a minecraft:poison
effect clear @a minecraft:wither
effect clear @a minecraft:blindness
effect clear @a minecraft:nausea
effect clear @a minecraft:hunger
effect clear @a minecraft:levitation
effect clear @a minecraft:slow_falling
effect clear @a minecraft:glowing
effect clear @a minecraft:darkness
