# Roda como o jogador, na posicao dele, uma vez por tique.
scoreboard players remove @s gce_t 1

# 24 graus por tique: uma volta inteira a cada 15 tiques, tres quartos de
# segundo. Mais rapido que isso embrulha o estomago de quem esta jogando.
tp @s ~ ~ ~ ~24 ~

# O funil: tres aneis, o de baixo mais largo que o de cima.
particle minecraft:cloud ~ ~0.1 ~ 0.55 0.05 0.55 0 6 normal
particle minecraft:cloud ~ ~0.9 ~ 0.4 0.05 0.4 0 5 normal
particle minecraft:cloud ~ ~1.7 ~ 0.22 0.05 0.22 0 4 normal
# Faisca de energia no meio, para nao ficar so uma nuvem cinzenta.
particle minecraft:crit ~ ~1 ~ 0.3 0.5 0.3 0.1 4 normal

# Assobio de vento a cada meio segundo, e nao a cada tique: senao vira zumbido.
execute if score @s gce_t matches 90 run playsound minecraft:entity.player.attack_sweep player @a ~ ~ ~ 0.7 1.4
execute if score @s gce_t matches 80 run playsound minecraft:entity.player.attack_sweep player @a ~ ~ ~ 0.7 1.5
execute if score @s gce_t matches 70 run playsound minecraft:entity.player.attack_sweep player @a ~ ~ ~ 0.7 1.6
execute if score @s gce_t matches 60 run playsound minecraft:entity.player.attack_sweep player @a ~ ~ ~ 0.7 1.7
execute if score @s gce_t matches 50 run playsound minecraft:entity.player.attack_sweep player @a ~ ~ ~ 0.7 1.8
execute if score @s gce_t matches 40 run playsound minecraft:entity.player.attack_sweep player @a ~ ~ ~ 0.7 1.9
execute if score @s gce_t matches 30 run playsound minecraft:entity.player.attack_sweep player @a ~ ~ ~ 0.7 2
execute if score @s gce_t matches 20 run playsound minecraft:entity.player.attack_sweep player @a ~ ~ ~ 0.7 2

execute if score @s gce_t matches ..0 run function gcemote:spin_fim
