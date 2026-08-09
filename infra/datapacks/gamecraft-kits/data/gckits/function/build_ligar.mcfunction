tag @s add gc_build
# Cancela todo prazo pendente: o que ja estava colocado passa a fazer parte do
# mapa, em vez de sumir no meio da sua construcao.
kill @e[type=minecraft:marker,tag=gc_temp]
tellraw @s [{"text":"Modo construcao LIGADO. ","color":"green"},{"text":"O que voce colocar fica, o que ja estava para de sumir, o que voce quebrar nao volta, e a TNT nao acende sozinha.","color":"gray"}]
