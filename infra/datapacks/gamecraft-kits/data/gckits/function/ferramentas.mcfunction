# Decide QUAIS ferramentas extras entram. O material delas e sorteado depois,
# junto com o das armas, porque depende da faixa da armadura.
# #tp = picareta, #ta = machado.
scoreboard players set #tp gc_r 0
scoreboard players set #ta gc_r 0
execute store result score #t gc_r run random value 1..4
execute if score #t gc_r matches 1 run scoreboard players set #tp gc_r 1
execute if score #t gc_r matches 2 run scoreboard players set #ta gc_r 1
execute if score #t gc_r matches 3 run scoreboard players set #tp gc_r 1
execute if score #t gc_r matches 3 run scoreboard players set #ta gc_r 1
