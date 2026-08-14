# Marca os limites do pedaco atual e prende os chunks dele.
#
# Oitenta blocos de lado porque 80x126x80 da 806.400 blocos — cabe no limite que
# o load levanta — e ocupa 25 chunks, que carregam do disco em bem menos que os
# oito tiques de espera.
scoreboard players operation #tx2 gcctf = #tx gcctf
scoreboard players add #tx2 gcctf 79
scoreboard players operation #tx2 gcctf < #cmaxx gcctf
scoreboard players operation #tz2 gcctf = #tz gcctf
scoreboard players add #tz2 gcctf 79
scoreboard players operation #tz2 gcctf < #cmaxz gcctf

execute store result storage gcctf:corte x1 int 1 run scoreboard players get #tx gcctf
execute store result storage gcctf:corte x2 int 1 run scoreboard players get #tx2 gcctf
execute store result storage gcctf:corte z1 int 1 run scoreboard players get #tz gcctf
execute store result storage gcctf:corte z2 int 1 run scoreboard players get #tz2 gcctf

function gcctf:tile_travar with storage gcctf:corte
scoreboard players set #espera gcctf 8
