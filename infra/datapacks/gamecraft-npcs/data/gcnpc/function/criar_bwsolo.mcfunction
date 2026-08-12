# Boneco do Bed Wars Solo.
# immovable: nao empurra nem cai. Invulnerable: nao pisca vermelho nem toma
# dano, entao bater nele nao faz nada — quem abre o menu e o clique direito.
# Silent: sem barulho de dano.
summon minecraft:mannequin ~ ~ ~ {Tags:["gcnpc","gcnpc_bwsolo","gcnpc_novo"],profile:"JulioCasare",CustomName:{"text":"Bed Wars Solo","color":"aqua","bold":true},CustomNameVisible:1b,immovable:1b,Invulnerable:1b,Silent:1b,PersistenceRequired:1b}
function gcnpc:virar
tellraw @s [{"text":"Boneco do Bed Wars Solo criado. ","color":"green"},{"text":"Clique com o botao direito nele para abrir o menu.","color":"gray"}]
