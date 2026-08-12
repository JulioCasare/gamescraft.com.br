# Boneco do LOBBY: e o caminho de volta, e por isso ele mora nos servidores de
# jogo, nao no lobby.
summon minecraft:mannequin ~ ~ ~ {Tags:["gcnpc","gcnpc_lobby","gcnpc_novo"],profile:"JulioCasare",hide_description:1b,immovable:1b,Invulnerable:1b,Silent:1b,PersistenceRequired:1b}
summon minecraft:text_display ~ ~2.5 ~ {Tags:["gcnpc","gcnpc_lobby","gcnpc_placa"],text:{"text":"LOBBY","color":"white","bold":true},billboard:"center",see_through:0b,background:0,alignment:"center",transformation:{scale:[1.60f,1.60f,1.60f],translation:[0f,0f,0f],left_rotation:[0f,0f,0f,1f],right_rotation:[0f,0f,0f,1f]}}
summon minecraft:text_display ~ ~2.2 ~ {Tags:["gcnpc","gcnpc_lobby","gcnpc_num"],text:{"text":"","color":"gray"},billboard:"center",see_through:0b,background:0,alignment:"center",transformation:{scale:[1.1f,1.1f,1.1f],translation:[0f,0f,0f],left_rotation:[0f,0f,0f,1f],right_rotation:[0f,0f,0f,1f]}}
summon minecraft:interaction ~ ~ ~ {Tags:["gcnpc","gcnpc_lobby"],width:1.2f,height:2.1f,response:1b}
function gcnpc:virar
tellraw @s [{"text":"Boneco do Lobby criado. ","color":"green"},{"text":"Clique nele para voltar.","color":"gray"}]
