# Roda como o jogador, na posicao dele, uma vez por tique.
scoreboard players remove @s gce_t 1

# 24 graus por tique: uma volta inteira a cada 15 tiques, tres quartos de
# segundo. Mais rapido que isso embrulha o estomago de quem esta jogando.
tp @s ~ ~ ~ ~24 ~

# O funil, em oito discos empilhados: estreito no pe e abrindo para cima, que e
# o formato de furacao de verdade. Cada disco e uma nuvem de particulas espalhada
# num quadrado achatado — o numero de particulas sobe junto com o raio, senao os
# discos de cima ficariam ralos e os de baixo empapados.
particle minecraft:cloud ~ ~0.05 ~ 0.10 0.02 0.10 0 3 normal
particle minecraft:cloud ~ ~0.45 ~ 0.18 0.02 0.18 0 4 normal
particle minecraft:cloud ~ ~0.85 ~ 0.28 0.02 0.28 0 5 normal
particle minecraft:cloud ~ ~1.25 ~ 0.40 0.02 0.40 0 6 normal
particle minecraft:cloud ~ ~1.65 ~ 0.54 0.02 0.54 0 8 normal
particle minecraft:cloud ~ ~2.05 ~ 0.70 0.02 0.70 0 10 normal
particle minecraft:cloud ~ ~2.45 ~ 0.86 0.02 0.86 0 12 normal
particle minecraft:cloud ~ ~2.85 ~ 1.00 0.03 1.00 0 14 normal

# Faisca de energia subindo pelo meio, para nao virar so uma nuvem cinzenta.
particle minecraft:crit ~ ~1 ~ 0.15 0.9 0.15 0.1 5 normal

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
