# Prazo dos blocos colocados em partida. So corre quando ninguem esta com o
# modo construcao ligado — senao o que voce acabou de construir sumiria por
# causa de um cronometro iniciado antes de voce ligar o /build.
scoreboard players remove @e[type=minecraft:marker,tag=gc_temp] gc_timer 1
execute as @e[type=minecraft:marker,tag=gc_temp,scores={gc_timer=..0}] at @s run function gckits:sumir
