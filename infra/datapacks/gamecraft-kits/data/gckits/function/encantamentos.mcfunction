# Encantamento sorteado de verdade: o modificador gckits:encantar usa a funcao
# nativa enchant_randomly, que escolhe um encantamento valido para o item e um
# nivel aleatorio dentro do maximo dele (1 a 5 nos casos comuns).
# Nem todo kit recebe: cada peca tem a sua chance.
#
# A espada ocupa o primeiro espaco do inventario e a lanca o segundo, porque
# sao os primeiros itens entregues depois do clear.
execute store result score #e1 gc_r run random value 1..3
execute if score #e1 gc_r matches 1 run item modify entity @s container.0 gckits:encantar
execute store result score #e2 gc_r run random value 1..3
execute if score #e2 gc_r matches 1 run item modify entity @s container.1 gckits:encantar
# Uma peca de armadura tambem pode vir encantada.
execute store result score #e3 gc_r run random value 1..4
execute if score #e3 gc_r matches 1 run item modify entity @s armor.chest gckits:encantar
execute if score #e3 gc_r matches 2 run item modify entity @s armor.head gckits:encantar
