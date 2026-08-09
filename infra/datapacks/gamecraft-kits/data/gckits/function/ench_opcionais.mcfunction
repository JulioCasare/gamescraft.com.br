# Sorteia quais encantamentos cada item opcional recebe, uma vez so, e depois
# varre os espacos 2 a 8 procurando o item para aplicar.
# Machado: eficiencia 1 em 2, afiacao 1 em 3, inquebravel 1 em 2.
execute store result score #ma1 gc_r run random value 1..2
execute store result score #ma2 gc_r run random value 1..3
execute store result score #ma3 gc_r run random value 1..2
# Picareta: eficiencia 1 em 2, inquebravel 1 em 2.
execute store result score #mp1 gc_r run random value 1..2
execute store result score #mp2 gc_r run random value 1..2
# Arco: forca 1 em 2, inquebravel 1 em 2.
execute store result score #mb1 gc_r run random value 1..2
execute store result score #mb2 gc_r run random value 1..2
# Tridente: lealdade 2 em 3 (e o que faz ele voltar), inquebravel 1 em 2.
execute store result score #mt1 gc_r run random value 1..3
execute store result score #mt2 gc_r run random value 1..2
execute if score #ma1 gc_r matches 1 if items entity @s container.2 #minecraft:axes run item modify entity @s container.2 gckits:e_eficiencia
execute if score #ma2 gc_r matches 1 if items entity @s container.2 #minecraft:axes run item modify entity @s container.2 gckits:e_afiacao
execute if score #ma3 gc_r matches 1 if items entity @s container.2 #minecraft:axes run item modify entity @s container.2 gckits:e_inquebravel
execute if score #mp1 gc_r matches 1 if items entity @s container.2 #minecraft:pickaxes run item modify entity @s container.2 gckits:e_eficiencia
execute if score #mp2 gc_r matches 1 if items entity @s container.2 #minecraft:pickaxes run item modify entity @s container.2 gckits:e_inquebravel
execute if score #mb1 gc_r matches 1 if items entity @s container.2 minecraft:bow run item modify entity @s container.2 gckits:e_forca
execute if score #mb2 gc_r matches 1 if items entity @s container.2 minecraft:bow run item modify entity @s container.2 gckits:e_inquebravel
execute unless score #mt1 gc_r matches 3 if items entity @s container.2 minecraft:trident run item modify entity @s container.2 gckits:e_lealdade
execute if score #mt2 gc_r matches 1 if items entity @s container.2 minecraft:trident run item modify entity @s container.2 gckits:e_inquebravel
execute if score #ma1 gc_r matches 1 if items entity @s container.3 #minecraft:axes run item modify entity @s container.3 gckits:e_eficiencia
execute if score #ma2 gc_r matches 1 if items entity @s container.3 #minecraft:axes run item modify entity @s container.3 gckits:e_afiacao
execute if score #ma3 gc_r matches 1 if items entity @s container.3 #minecraft:axes run item modify entity @s container.3 gckits:e_inquebravel
execute if score #mp1 gc_r matches 1 if items entity @s container.3 #minecraft:pickaxes run item modify entity @s container.3 gckits:e_eficiencia
execute if score #mp2 gc_r matches 1 if items entity @s container.3 #minecraft:pickaxes run item modify entity @s container.3 gckits:e_inquebravel
execute if score #mb1 gc_r matches 1 if items entity @s container.3 minecraft:bow run item modify entity @s container.3 gckits:e_forca
execute if score #mb2 gc_r matches 1 if items entity @s container.3 minecraft:bow run item modify entity @s container.3 gckits:e_inquebravel
execute unless score #mt1 gc_r matches 3 if items entity @s container.3 minecraft:trident run item modify entity @s container.3 gckits:e_lealdade
execute if score #mt2 gc_r matches 1 if items entity @s container.3 minecraft:trident run item modify entity @s container.3 gckits:e_inquebravel
execute if score #ma1 gc_r matches 1 if items entity @s container.4 #minecraft:axes run item modify entity @s container.4 gckits:e_eficiencia
execute if score #ma2 gc_r matches 1 if items entity @s container.4 #minecraft:axes run item modify entity @s container.4 gckits:e_afiacao
execute if score #ma3 gc_r matches 1 if items entity @s container.4 #minecraft:axes run item modify entity @s container.4 gckits:e_inquebravel
execute if score #mp1 gc_r matches 1 if items entity @s container.4 #minecraft:pickaxes run item modify entity @s container.4 gckits:e_eficiencia
execute if score #mp2 gc_r matches 1 if items entity @s container.4 #minecraft:pickaxes run item modify entity @s container.4 gckits:e_inquebravel
execute if score #mb1 gc_r matches 1 if items entity @s container.4 minecraft:bow run item modify entity @s container.4 gckits:e_forca
execute if score #mb2 gc_r matches 1 if items entity @s container.4 minecraft:bow run item modify entity @s container.4 gckits:e_inquebravel
execute unless score #mt1 gc_r matches 3 if items entity @s container.4 minecraft:trident run item modify entity @s container.4 gckits:e_lealdade
execute if score #mt2 gc_r matches 1 if items entity @s container.4 minecraft:trident run item modify entity @s container.4 gckits:e_inquebravel
execute if score #ma1 gc_r matches 1 if items entity @s container.5 #minecraft:axes run item modify entity @s container.5 gckits:e_eficiencia
execute if score #ma2 gc_r matches 1 if items entity @s container.5 #minecraft:axes run item modify entity @s container.5 gckits:e_afiacao
execute if score #ma3 gc_r matches 1 if items entity @s container.5 #minecraft:axes run item modify entity @s container.5 gckits:e_inquebravel
execute if score #mp1 gc_r matches 1 if items entity @s container.5 #minecraft:pickaxes run item modify entity @s container.5 gckits:e_eficiencia
execute if score #mp2 gc_r matches 1 if items entity @s container.5 #minecraft:pickaxes run item modify entity @s container.5 gckits:e_inquebravel
execute if score #mb1 gc_r matches 1 if items entity @s container.5 minecraft:bow run item modify entity @s container.5 gckits:e_forca
execute if score #mb2 gc_r matches 1 if items entity @s container.5 minecraft:bow run item modify entity @s container.5 gckits:e_inquebravel
execute unless score #mt1 gc_r matches 3 if items entity @s container.5 minecraft:trident run item modify entity @s container.5 gckits:e_lealdade
execute if score #mt2 gc_r matches 1 if items entity @s container.5 minecraft:trident run item modify entity @s container.5 gckits:e_inquebravel
execute if score #ma1 gc_r matches 1 if items entity @s container.6 #minecraft:axes run item modify entity @s container.6 gckits:e_eficiencia
execute if score #ma2 gc_r matches 1 if items entity @s container.6 #minecraft:axes run item modify entity @s container.6 gckits:e_afiacao
execute if score #ma3 gc_r matches 1 if items entity @s container.6 #minecraft:axes run item modify entity @s container.6 gckits:e_inquebravel
execute if score #mp1 gc_r matches 1 if items entity @s container.6 #minecraft:pickaxes run item modify entity @s container.6 gckits:e_eficiencia
execute if score #mp2 gc_r matches 1 if items entity @s container.6 #minecraft:pickaxes run item modify entity @s container.6 gckits:e_inquebravel
execute if score #mb1 gc_r matches 1 if items entity @s container.6 minecraft:bow run item modify entity @s container.6 gckits:e_forca
execute if score #mb2 gc_r matches 1 if items entity @s container.6 minecraft:bow run item modify entity @s container.6 gckits:e_inquebravel
execute unless score #mt1 gc_r matches 3 if items entity @s container.6 minecraft:trident run item modify entity @s container.6 gckits:e_lealdade
execute if score #mt2 gc_r matches 1 if items entity @s container.6 minecraft:trident run item modify entity @s container.6 gckits:e_inquebravel
execute if score #ma1 gc_r matches 1 if items entity @s container.7 #minecraft:axes run item modify entity @s container.7 gckits:e_eficiencia
execute if score #ma2 gc_r matches 1 if items entity @s container.7 #minecraft:axes run item modify entity @s container.7 gckits:e_afiacao
execute if score #ma3 gc_r matches 1 if items entity @s container.7 #minecraft:axes run item modify entity @s container.7 gckits:e_inquebravel
execute if score #mp1 gc_r matches 1 if items entity @s container.7 #minecraft:pickaxes run item modify entity @s container.7 gckits:e_eficiencia
execute if score #mp2 gc_r matches 1 if items entity @s container.7 #minecraft:pickaxes run item modify entity @s container.7 gckits:e_inquebravel
execute if score #mb1 gc_r matches 1 if items entity @s container.7 minecraft:bow run item modify entity @s container.7 gckits:e_forca
execute if score #mb2 gc_r matches 1 if items entity @s container.7 minecraft:bow run item modify entity @s container.7 gckits:e_inquebravel
execute unless score #mt1 gc_r matches 3 if items entity @s container.7 minecraft:trident run item modify entity @s container.7 gckits:e_lealdade
execute if score #mt2 gc_r matches 1 if items entity @s container.7 minecraft:trident run item modify entity @s container.7 gckits:e_inquebravel
execute if score #ma1 gc_r matches 1 if items entity @s container.8 #minecraft:axes run item modify entity @s container.8 gckits:e_eficiencia
execute if score #ma2 gc_r matches 1 if items entity @s container.8 #minecraft:axes run item modify entity @s container.8 gckits:e_afiacao
execute if score #ma3 gc_r matches 1 if items entity @s container.8 #minecraft:axes run item modify entity @s container.8 gckits:e_inquebravel
execute if score #mp1 gc_r matches 1 if items entity @s container.8 #minecraft:pickaxes run item modify entity @s container.8 gckits:e_eficiencia
execute if score #mp2 gc_r matches 1 if items entity @s container.8 #minecraft:pickaxes run item modify entity @s container.8 gckits:e_inquebravel
execute if score #mb1 gc_r matches 1 if items entity @s container.8 minecraft:bow run item modify entity @s container.8 gckits:e_forca
execute if score #mb2 gc_r matches 1 if items entity @s container.8 minecraft:bow run item modify entity @s container.8 gckits:e_inquebravel
execute unless score #mt1 gc_r matches 3 if items entity @s container.8 minecraft:trident run item modify entity @s container.8 gckits:e_lealdade
execute if score #mt2 gc_r matches 1 if items entity @s container.8 minecraft:trident run item modify entity @s container.8 gckits:e_inquebravel
