# Fim de uma faixa: volta ao comeco em X e desce uma faixa em Z.
scoreboard players operation #tx gcctf = #cminx gcctf
scoreboard players operation #tz gcctf = #tz2 gcctf
scoreboard players add #tz gcctf 1
execute if score #tz gcctf > #cmaxz gcctf run function gcctf:fatia_fim
