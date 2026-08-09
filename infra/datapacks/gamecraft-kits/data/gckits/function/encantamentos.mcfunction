# Cada encantamento e sorteado por conta propria: as vezes vem um, as vezes o
# outro, as vezes os dois, as vezes nenhum. O nivel tambem e sorteado, de 1 ate
# o maximo do encantamento — entao sai tanto Afiacao I quanto Afiacao V.
#
# Espada e lanca tem posicao fixa (primeiros itens depois do clear). Os
# opcionais podem cair em qualquer espaco, entao a busca varre a barra.

# Espada: afiacao 1 em 2, inquebravel 1 em 2.
execute store result score #q gc_r run random value 1..2
execute if score #q gc_r matches 1 run item modify entity @s container.0 gckits:e_afiacao
execute store result score #q gc_r run random value 1..2
execute if score #q gc_r matches 1 run item modify entity @s container.0 gckits:e_inquebravel

# Lanca: estocada 1 em 2, afiacao 1 em 3, inquebravel 1 em 2.
execute store result score #q gc_r run random value 1..2
execute if score #q gc_r matches 1 run item modify entity @s container.1 gckits:e_estocada
execute store result score #q gc_r run random value 1..3
execute if score #q gc_r matches 1 run item modify entity @s container.1 gckits:e_afiacao
execute store result score #q gc_r run random value 1..2
execute if score #q gc_r matches 1 run item modify entity @s container.1 gckits:e_inquebravel

# Armadura (head): protecao 1 em 2, inquebravel 1 em 3, sorteados por peca.
execute store result score #q gc_r run random value 1..2
execute if score #q gc_r matches 1 run item modify entity @s armor.head gckits:e_protecao
execute store result score #q gc_r run random value 1..3
execute if score #q gc_r matches 1 run item modify entity @s armor.head gckits:e_inquebravel

# Armadura (chest): protecao 1 em 2, inquebravel 1 em 3, sorteados por peca.
execute store result score #q gc_r run random value 1..2
execute if score #q gc_r matches 1 run item modify entity @s armor.chest gckits:e_protecao
execute store result score #q gc_r run random value 1..3
execute if score #q gc_r matches 1 run item modify entity @s armor.chest gckits:e_inquebravel

# Armadura (legs): protecao 1 em 2, inquebravel 1 em 3, sorteados por peca.
execute store result score #q gc_r run random value 1..2
execute if score #q gc_r matches 1 run item modify entity @s armor.legs gckits:e_protecao
execute store result score #q gc_r run random value 1..3
execute if score #q gc_r matches 1 run item modify entity @s armor.legs gckits:e_inquebravel

# Armadura (feet): protecao 1 em 2, inquebravel 1 em 3, sorteados por peca.
execute store result score #q gc_r run random value 1..2
execute if score #q gc_r matches 1 run item modify entity @s armor.feet gckits:e_protecao
execute store result score #q gc_r run random value 1..3
execute if score #q gc_r matches 1 run item modify entity @s armor.feet gckits:e_inquebravel

function gckits:ench_opcionais
