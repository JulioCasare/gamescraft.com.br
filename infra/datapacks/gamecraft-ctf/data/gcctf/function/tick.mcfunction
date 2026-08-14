# O gatilho precisa ser reabilitado a cada uso, por isso a linha roda todo tick.
scoreboard players enable @a obras
execute as @a[scores={obras=1..}] run function gcctf:trigger_obras

# Copia do mapa em andamento: uma fatia por tique ate acabar.
execute if score #ativo gcctf matches 1 run function gcctf:fatia

scoreboard players add #t gcctf 1
execute if score #t gcctf matches 10.. run function gcctf:meio_segundo

# As areas sao grandes: comparar as duas custa mais que as 48 torres juntas.
# Uma vez por segundo e o ponto em que o conserto parece imediato para quem
# quebrou sem a conta pesar no servidor.
scoreboard players add #ta gcctf 1
execute if score #ta gcctf matches 20.. run function gcctf:cada_segundo
