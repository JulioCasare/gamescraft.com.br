# Deixa um marcador invisivel no lugar do bloco, com 6000 ticks (5 minutos) de
# validade. O marcador e uma entidade leve, feita para isso, e sobrevive a
# reinicio do servidor junto com o mundo.
summon minecraft:marker ~ ~ ~ {Tags:["gc_temp","gc_novo"]}
scoreboard players set @e[type=minecraft:marker,tag=gc_novo] gc_timer 6000
tag @e[type=minecraft:marker,tag=gc_novo] remove gc_novo
