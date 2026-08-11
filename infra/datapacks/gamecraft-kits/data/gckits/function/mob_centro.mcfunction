# Marca o meio da arena, onde os mobs nascem, na posicao de quem rodou. Fica no
# armazenamento, entao sobrevive a reinicio.
execute store result storage gckits:mob cx int 1 run data get entity @s Pos[0] 1
execute store result storage gckits:mob cy int 1 run data get entity @s Pos[1] 1
execute store result storage gckits:mob cz int 1 run data get entity @s Pos[2] 1
tellraw @s [{"text":"Meio da arena marcado. ","color":"green"},{"text":"Os mobs passam a nascer ate 12 blocos daqui.","color":"gray"}]
