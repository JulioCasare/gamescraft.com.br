# Quem escuta o clique nao e o boneco: e uma caixa de interacao invisivel na
# frente dele. O gatilho de conquista que eu tinha usado antes so dispara quando
# o jogador usa um item na entidade — de mao vazia, nada acontecia. A caixa pega
# os dois botoes, e ainda fica na frente do boneco, entao o soco morre nela.
execute as @e[type=minecraft:interaction,tag=gcnpc] at @s run function gcnpc:clique
