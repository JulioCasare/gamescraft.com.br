# Roda como a caixa, na posicao dela. Quem clicou e o jogador mais proximo: o
# alcance de clique do jogo e de tres blocos, entao nao tem como ser outro.
execute if entity @s[tag=gcnpc_bwsolo] as @p[distance=..8] run dialog show @s gcnpc:bwsolo
execute if entity @s[tag=gcnpc_bwduplas] as @p[distance=..8] run dialog show @s gcnpc:bwduplas
execute if entity @s[tag=gcnpc_pilares] as @p[distance=..8] run dialog show @s gcnpc:pilares
execute if entity @s[tag=gcnpc_pvp] as @p[distance=..8] run dialog show @s gcnpc:pvp
execute if entity @s[tag=gcnpc_build] as @p[distance=..8] run dialog show @s gcnpc:build
execute if entity @s[tag=gcnpc_longos] as @p[distance=..8] run dialog show @s gcnpc:longos
execute if entity @s[tag=gcnpc_eventos] as @p[distance=..8] run dialog show @s gcnpc:eventos
