# A espada vem sempre (funcao arma). Aqui sorteia o que acompanha ela:
# picareta, machado, os dois, ou nada.
execute store result score #t gc_r run random value 1..4
execute if score #t gc_r matches 1 run function gckits:tool_picareta
execute if score #t gc_r matches 2 run function gckits:tool_machado
execute if score #t gc_r matches 3 run function gckits:tool_picareta
execute if score #t gc_r matches 3 run function gckits:tool_machado
