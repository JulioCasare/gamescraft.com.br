# Apaga o boneco mais perto e a caixa de interacao dele. Sem isso, sobrariam
# caixas invisiveis abrindo menu no vazio.
execute at @s run kill @e[type=minecraft:mannequin,tag=gcnpc,limit=1,sort=nearest,distance=..8]
execute at @s run kill @e[type=minecraft:interaction,tag=gcnpc,limit=1,sort=nearest,distance=..8]
tellraw @s [{"text":"Boneco mais proximo apagado.","color":"yellow"}]
