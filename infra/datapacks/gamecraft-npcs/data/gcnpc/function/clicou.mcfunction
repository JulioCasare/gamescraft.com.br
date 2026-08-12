# Roda como o jogador que clicou. A conquista e so um gatilho: precisa ser
# retirada na hora, senao nunca dispara de novo.
advancement revoke @s only gcnpc:interagiu

# O gatilho nao diz em qual boneco o jogador clicou, so que foi num deles.
# Entao marca o mais perto — e sempre o que ele tem na frente, porque o alcance
# de clique do jogo e menor que isso.
execute at @s as @e[type=minecraft:mannequin,tag=gcnpc,limit=1,sort=nearest,distance=..6] run tag @s add gcnpc_alvo

execute if entity @e[tag=gcnpc_alvo,tag=gcnpc_bwsolo] run dialog show @s gcnpc:bwsolo
execute if entity @e[tag=gcnpc_alvo,tag=gcnpc_bwduplas] run dialog show @s gcnpc:bwduplas
execute if entity @e[tag=gcnpc_alvo,tag=gcnpc_pilares] run dialog show @s gcnpc:pilares
execute if entity @e[tag=gcnpc_alvo,tag=gcnpc_pvp] run dialog show @s gcnpc:pvp
execute if entity @e[tag=gcnpc_alvo,tag=gcnpc_build] run dialog show @s gcnpc:build
execute if entity @e[tag=gcnpc_alvo,tag=gcnpc_longos] run dialog show @s gcnpc:longos
execute if entity @e[tag=gcnpc_alvo,tag=gcnpc_eventos] run dialog show @s gcnpc:eventos

tag @e[tag=gcnpc_alvo] remove gcnpc_alvo
