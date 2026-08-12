# O gatilho precisa ser reabilitado a cada uso, por isso a linha roda todo tick.
scoreboard players enable @a obras
execute as @a[scores={obras=1..}] run function gcctf:trigger_obras

scoreboard players add #t gcctf 1
execute if score #t gcctf matches 20.. run function gcctf:cada_segundo
