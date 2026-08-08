# A arma sai do que sobrou do orcamento. Soma da armadura vai de 4 (tudo couro)
# a 20 (tudo diamante); quanto mais alta, mais fraca a arma e menos extras.
# #w decide se a arma vem como lanca no lugar da espada (1 em 4).
execute store result score #w gc_r run random value 1..4
execute if score @s gc_s matches ..8 run function gckits:arma_forte
execute if score @s gc_s matches 9..12 run function gckits:arma_boa
execute if score @s gc_s matches 13..16 run function gckits:arma_media
execute if score @s gc_s matches 17.. run function gckits:arma_fraca
