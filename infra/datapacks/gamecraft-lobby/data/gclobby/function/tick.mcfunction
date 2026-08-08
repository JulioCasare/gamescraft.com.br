# Chegou no lobby (login ou troca de modo pelo proxy): vai para o centro.
execute as @a[scores={gcl_left=1..}] run function gclobby:chegou
execute as @a[tag=!gcl_visto] run function gclobby:chegou

# Morreu no lobby (queda, afogamento): volta ao centro tambem.
execute as @a[scores={gcl_deaths=1..}] run function gclobby:morreu

# Encostou na agua: devolve ao centro. Checa os pes e a altura do corpo, para
# pegar tanto quem pisou na beirada quanto quem caiu no mar.
execute as @a at @s if block ~ ~ ~ minecraft:water run function gclobby:ao_centro
execute as @a at @s if block ~ ~1 ~ minecraft:water run function gclobby:ao_centro
