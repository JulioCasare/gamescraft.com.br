# Boneco do Pilares da Fortuna.
# immovable: nao empurra nem cai. Invulnerable: nao pisca vermelho nem toma
# dano, entao bater nele nao faz nada — quem abre o menu e o clique direito.
# Silent: sem barulho de dano.
summon minecraft:mannequin ~ ~ ~ {Tags:["gcnpc","gcnpc_pilares","gcnpc_novo"],profile:"JulioCasare",CustomName:{"text":"Pilares da Fortuna","color":"gold","bold":true},CustomNameVisible:1b,immovable:1b,Invulnerable:1b,Silent:1b,PersistenceRequired:1b}
function gcnpc:virar
tellraw @s [{"text":"Boneco do Pilares da Fortuna criado. ","color":"green"},{"text":"Clique com o botao direito nele para abrir o menu.","color":"gray"}]
