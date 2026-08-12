# Apaga o boneco mais perto de quem rodou. Sem isso, boneco errado so sai com
# /kill, que e facil demais de errar e apagar outra coisa.
execute at @s run kill @e[type=minecraft:mannequin,tag=gcnpc,limit=1,sort=nearest,distance=..8]
tellraw @s [{"text":"Boneco mais proximo apagado.","color":"yellow"}]
