# Fim de uma faixa: o cursor volta ao comeco em X e desce uma faixa em Z.
scoreboard players operation #tx gcctf = #cminx gcctf
scoreboard players operation #tz gcctf = #tz2 gcctf
scoreboard players add #tz gcctf 1
