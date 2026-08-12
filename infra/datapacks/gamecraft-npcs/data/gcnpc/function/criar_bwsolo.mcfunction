# Boneco do Bed Wars Solo, mais a caixa de interacao que escuta o clique.
# immovable: nao empurra nem cai. Invulnerable: bater nele nao tira vida.
summon minecraft:mannequin ~ ~ ~ {Tags:["gcnpc","gcnpc_bwsolo","gcnpc_novo"],profile:"JulioCasare",CustomName:{"text":"Bed Wars Solo","color":"aqua","bold":true},CustomNameVisible:1b,immovable:1b,Invulnerable:1b,Silent:1b}
# A caixa e um pouco maior que o boneco, para ficar na frente dele: assim o
# clique bate nela e nunca no boneco.
summon minecraft:interaction ~ ~ ~ {Tags:["gcnpc","gcnpc_bwsolo"],width:1.2f,height:2.1f,response:1b}
function gcnpc:virar
tellraw @s [{"text":"Boneco do Bed Wars Solo criado. ","color":"green"},{"text":"Clique nele — qualquer botao — para abrir o menu.","color":"gray"}]
