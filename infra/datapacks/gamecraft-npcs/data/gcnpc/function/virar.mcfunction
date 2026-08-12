# Vira o boneco recem-nascido para onde o jogador estava olhando. O /summon
# nasce sempre olhando para o sul, entao a direcao e copiada depois.
execute store result entity @e[tag=gcnpc_novo,limit=1] Rotation[0] float 1 run data get entity @s Rotation[0] 1
tag @e[tag=gcnpc_novo] remove gcnpc_novo
