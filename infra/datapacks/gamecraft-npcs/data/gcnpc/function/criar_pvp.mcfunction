# Boneco do PVP: quatro pecas que nascem juntas e morrem juntas.
#
# 1. o boneco. hide_description tira o "Persona non lusor" que o jogo escreve
#    sozinho embaixo do nome de todo mannequin. immovable: nao empurra nem cai.
#    Invulnerable: bater nele nao tira vida. PersistenceRequired: fica ate
#    alguem usar /npcapagar.
summon minecraft:mannequin ~ ~ ~ {Tags:["gcnpc","gcnpc_pvp","gcnpc_novo"],profile:"JulioCasare",hide_description:1b,immovable:1b,Invulnerable:1b,Silent:1b,PersistenceRequired:1b}
# 2. a placa do nome, na escala 1.60 — calculada pelo comprimento de "PVP",
#    para nome curto e nome longo darem quase a mesma largura.
summon minecraft:text_display ~ ~2.5 ~ {Tags:["gcnpc","gcnpc_pvp","gcnpc_placa"],text:{"text":"PVP","color":"red","bold":true},billboard:"center",see_through:0b,background:0,alignment:"center",transformation:{scale:[1.60f,1.60f,1.60f],translation:[0f,0f,0f],left_rotation:[0f,0f,0f,1f],right_rotation:[0f,0f,0f,1f]}}
# 3. a contagem de jogadores, em placa separada e menor. Separada porque o
#    tamanho vale para a placa inteira: junto com o nome, os dois teriam de ter o
#    mesmo tamanho. Quem escreve o numero aqui e o plugin GcMenus — o lobby nao
#    sabe quanta gente ha no outro servidor, o proxy sabe.
summon minecraft:text_display ~ ~2.2 ~ {Tags:["gcnpc","gcnpc_pvp","gcnpc_num"],text:{"text":"","color":"gray"},billboard:"center",see_through:0b,background:0,alignment:"center",transformation:{scale:[1.1f,1.1f,1.1f],translation:[0f,0f,0f],left_rotation:[0f,0f,0f,1f],right_rotation:[0f,0f,0f,1f]}}
# 4. a caixa que escuta o clique, um pouco maior que o boneco e na frente dele:
#    assim o soco morre nela e nunca chega no corpo.
summon minecraft:interaction ~ ~ ~ {Tags:["gcnpc","gcnpc_pvp"],width:1.2f,height:2.1f,response:1b}
function gcnpc:virar
tellraw @s [{"text":"Boneco do PVP criado. ","color":"green"},{"text":"Clique nele para abrir o menu.","color":"gray"}]
