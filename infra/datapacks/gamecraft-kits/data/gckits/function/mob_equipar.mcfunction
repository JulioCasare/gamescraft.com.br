tag @s remove gc_novo

# A arma ja veio no summon: dar depois faz o esqueleto ficar sem atirar. Aqui so
# entra a armadura, que nao mexe em como o mob luta.
#
# Metade dos que sabem vestir vem com alguma peca. Aranha, cubo de magma, vex,
# devastador e zoglin ficam de fora: nao usam armadura.
execute store result score #ma gc_r run random value 1..2
execute if score #ma gc_r matches 1 if entity @s[type=minecraft:zombie] run function gckits:mob_armadura
execute if score #ma gc_r matches 1 if entity @s[type=minecraft:skeleton] run function gckits:mob_armadura
execute if score #ma gc_r matches 1 if entity @s[type=minecraft:wither_skeleton] run function gckits:mob_armadura

# Nada do que ele veste ou carrega cai no chao. Sem isso a arena vira loja de
# armadura de graca e o kit sorteado perde a graca.
data merge entity @s {drop_chances:{head:0.0f,chest:0.0f,legs:0.0f,feet:0.0f,mainhand:0.0f,offhand:0.0f}}
