# Pega o pedaco onde o cursor esta, prende os chunks dele e adianta o cursor.
# Quem chama decide se o resultado vira o pedaco de agora ou o da vez seguinte.
execute if score #tz gcctf > #cmaxz gcctf run scoreboard players set #achou gcctf 0
execute if score #tz gcctf > #cmaxz gcctf run return 0
scoreboard players set #achou gcctf 1

scoreboard players operation #tx2 gcctf = #tx gcctf
scoreboard players add #tx2 gcctf 79
scoreboard players operation #tx2 gcctf < #cmaxx gcctf
scoreboard players operation #tz2 gcctf = #tz gcctf
scoreboard players add #tz2 gcctf 79
scoreboard players operation #tz2 gcctf < #cmaxz gcctf

# O molde traz as alturas, que nao mudam de pedaco para pedaco.
data modify storage gcctf:tiles novo set from storage gcctf:tiles molde
execute store result storage gcctf:tiles novo.x1 int 1 run scoreboard players get #tx gcctf
execute store result storage gcctf:tiles novo.x2 int 1 run scoreboard players get #tx2 gcctf
execute store result storage gcctf:tiles novo.z1 int 1 run scoreboard players get #tz gcctf
execute store result storage gcctf:tiles novo.z2 int 1 run scoreboard players get #tz2 gcctf
function gcctf:tile_travar with storage gcctf:tiles novo

scoreboard players operation #tx gcctf = #tx2 gcctf
scoreboard players add #tx gcctf 1
execute if score #tx gcctf > #cmaxx gcctf run function gcctf:tile_linha
