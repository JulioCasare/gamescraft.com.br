# Roda como o mob. Escreve o nome dele com a vida atual na cabeca.
#
# Nome de entidade nao acompanha vida sozinho — o placar "below_name", que faz
# isso para jogador, nao vale para mob. Entao o nome e reescrito uma vez por
# segundo com o numero dentro dele.
execute store result score #hp gc_r run data get entity @s Health 1
execute store result storage gckits:mob hp int 1 run scoreboard players get #hp gc_r

data modify storage gckits:mob nome set value "Mob"
data modify storage gckits:mob cor set value "white"
execute if entity @s[type=minecraft:zombie,tag=!gc_gigante] run data modify storage gckits:mob nome set value "Zumbi"
execute if entity @s[type=minecraft:zombie,tag=!gc_gigante] run data modify storage gckits:mob cor set value "green"
execute if entity @s[tag=gc_gigante] run data modify storage gckits:mob nome set value "Zumbi Gigante"
execute if entity @s[tag=gc_gigante] run data modify storage gckits:mob cor set value "dark_green"
execute if entity @s[type=minecraft:skeleton] run data modify storage gckits:mob nome set value "Esqueleto"
execute if entity @s[type=minecraft:skeleton] run data modify storage gckits:mob cor set value "gray"
execute if entity @s[type=minecraft:spider] run data modify storage gckits:mob nome set value "Aranha"
execute if entity @s[type=minecraft:spider] run data modify storage gckits:mob cor set value "dark_purple"
execute if entity @s[type=minecraft:magma_cube] run data modify storage gckits:mob nome set value "Cubo de Magma"
execute if entity @s[type=minecraft:magma_cube] run data modify storage gckits:mob cor set value "gold"
execute if entity @s[type=minecraft:vex] run data modify storage gckits:mob nome set value "Vex"
execute if entity @s[type=minecraft:vex] run data modify storage gckits:mob cor set value "aqua"
execute if entity @s[type=minecraft:wither_skeleton] run data modify storage gckits:mob nome set value "Esqueleto Wither"
execute if entity @s[type=minecraft:wither_skeleton] run data modify storage gckits:mob cor set value "dark_gray"
execute if entity @s[type=minecraft:ravager] run data modify storage gckits:mob nome set value "Devastador"
execute if entity @s[type=minecraft:ravager] run data modify storage gckits:mob cor set value "red"
execute if entity @s[type=minecraft:zoglin] run data modify storage gckits:mob nome set value "Zoglin"
execute if entity @s[type=minecraft:zoglin] run data modify storage gckits:mob cor set value "light_purple"

function gckits:mob_nome_por with storage gckits:mob
