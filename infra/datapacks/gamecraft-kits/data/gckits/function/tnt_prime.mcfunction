# TNT que machuca mas nao fura o mapa: a entidade sobe com explosion_power 0,
# que nao quebra bloco nenhum (testado), e um marcador guarda o mesmo pavio
# para aplicar o dano na hora certa.
setblock ~ ~ ~ air
summon minecraft:tnt ~0.5 ~ ~0.5 {fuse:40,explosion_power:0f}
summon minecraft:marker ~0.5 ~0.5 ~0.5 {Tags:["gc_boom","gc_novo_boom"]}
scoreboard players set @e[type=minecraft:marker,tag=gc_novo_boom] gc_timer 40
tag @e[type=minecraft:marker,tag=gc_novo_boom] remove gc_novo_boom
