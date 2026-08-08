# Decide QUAIS ferramentas extras entram. O material delas e sorteado depois,
# junto com o das armas, porque depende da faixa da armadura.
# Os dois sorteios sao independentes: da para vir os dois, um so, ou nenhum.
# #tp = picareta (1 em 2), #ta = machado (2 em 3).
scoreboard players set #tp gc_r 0
scoreboard players set #ta gc_r 0
execute store result score #t gc_r run random value 1..3
execute unless score #t gc_r matches 1 run scoreboard players set #ta gc_r 1
execute store result score #t gc_r run random value 1..2
execute if score #t gc_r matches 1 run scoreboard players set #tp gc_r 1
