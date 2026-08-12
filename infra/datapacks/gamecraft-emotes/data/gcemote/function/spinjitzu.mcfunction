# Spinjitzu: cinco segundos virando furacao.
#
# Quem gira e a camera do jogador, com um tp que muda so o angulo. E o que
# vende o efeito — particula sozinha nao passa a sensacao de estar rodando.
tag @s add gce_spin
scoreboard players set @s gce_t 100
execute at @s run playsound minecraft:entity.ender_dragon.flap player @a ~ ~ ~ 1 1.6
tellraw @s [{"text":"Spinjitzu!","color":"aqua","bold":true}]
