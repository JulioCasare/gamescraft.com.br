# Apaga o boneco mais perto e as pecas que vem com ele: as duas placas e a caixa
# de clique. Sem isso sobraria nome flutuando e menu abrindo no vazio.
kill @e[type=minecraft:mannequin,tag=gcnpc,limit=1,sort=nearest,distance=..8]
kill @e[type=minecraft:text_display,tag=gcnpc_placa,limit=1,sort=nearest,distance=..8]
kill @e[type=minecraft:text_display,tag=gcnpc_num,limit=1,sort=nearest,distance=..8]
kill @e[type=minecraft:interaction,tag=gcnpc,limit=1,sort=nearest,distance=..8]
tellraw @s [{"text":"Boneco mais proximo apagado.","color":"yellow"}]
