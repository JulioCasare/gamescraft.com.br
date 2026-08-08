# Espada e lanca vem SEMPRE. O material de cada uma e sorteado dentro da faixa
# que a armadura deixou disponivel.
execute if score @s gc_s matches ..8 run function gckits:banda_1
execute if score @s gc_s matches 9..12 run function gckits:banda_2
execute if score @s gc_s matches 13..16 run function gckits:banda_3
execute if score @s gc_s matches 17.. run function gckits:banda_4
