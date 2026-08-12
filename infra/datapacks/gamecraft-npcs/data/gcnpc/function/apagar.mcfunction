# Apaga o boneco mais perto e a caixa de interacao dele. Sem tirar a caixa,
# sobraria um menu abrindo no vazio.
#
# Sem "execute at @s": o seletor ja parte de onde o comando foi dado, e assim a
# funcao tambem funciona pelo console, onde @s nao e ninguem.
kill @e[type=minecraft:mannequin,tag=gcnpc,limit=1,sort=nearest,distance=..8]
kill @e[type=minecraft:interaction,tag=gcnpc,limit=1,sort=nearest,distance=..8]
tellraw @s [{"text":"Boneco mais proximo apagado.","color":"yellow"}]
