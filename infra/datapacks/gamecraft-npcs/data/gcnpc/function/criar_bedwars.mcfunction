# Boneco do Bed Wars, mais a caixa de interacao que escuta o clique.
# Um boneco so para os dois modos: o menu dele tem Solo e Duplas lado a lado.
summon minecraft:mannequin ~ ~ ~ {Tags:["gcnpc","gcnpc_bedwars","gcnpc_novo"],profile:"JulioCasare",CustomName:{"text":"Bed Wars","color":"aqua","bold":true},CustomNameVisible:1b,immovable:1b,Invulnerable:1b,Silent:1b}
summon minecraft:interaction ~ ~ ~ {Tags:["gcnpc","gcnpc_bedwars"],width:1.2f,height:2.1f,response:1b}
function gcnpc:virar
tellraw @s [{"text":"Boneco do Bed Wars criado. ","color":"green"},{"text":"Clique nele para abrir o menu.","color":"gray"}]
