# Boneco do JOGOS LONGOS: tres pecas que nascem juntas e morrem juntas.
#
# 1. o boneco. hide_description tira o "Persona non lusor" que o jogo escreve
#    sozinho embaixo do nome de todo mannequin. immovable: nao empurra nem cai.
#    Invulnerable: bater nele nao tira vida.
summon minecraft:mannequin ~ ~ ~ {Tags:["gcnpc","gcnpc_longos","gcnpc_novo"],profile:"JulioCasare",hide_description:1b,immovable:1b,Invulnerable:1b,Silent:1b}
# 2. a placa do nome. E uma entidade de texto e nao o nome da entidade porque so
#    ela aceita tamanho: o nome comum sai sempre do mesmo tamanhinho.
#    billboard center: gira para encarar quem olha, de qualquer angulo.
summon minecraft:text_display ~ ~2.4 ~ {Tags:["gcnpc","gcnpc_longos"],text:[{"text":"JOGOS LONGOS","color":"light_purple","bold":true},{"text":"\nclique para entrar","color":"gray","bold":false}],billboard:"center",see_through:0b,background:0,alignment:"center",transformation:{scale:[2.0f,2.0f,2.0f],translation:[0f,0f,0f],left_rotation:[0f,0f,0f,1f],right_rotation:[0f,0f,0f,1f]}}
# 3. a caixa que escuta o clique, um pouco maior que o boneco e na frente dele:
#    assim o soco morre nela e nunca chega no corpo.
summon minecraft:interaction ~ ~ ~ {Tags:["gcnpc","gcnpc_longos"],width:1.2f,height:2.1f,response:1b}
function gcnpc:virar
tellraw @s [{"text":"Boneco do JOGOS LONGOS criado. ","color":"green"},{"text":"Clique nele para abrir o menu.","color":"gray"}]
