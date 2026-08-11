# So repara o que existe e o que de fato gasta: aplicar o modificador num espaco
# vazio, ou num bloco empilhado na mao, gera aviso no log a cada segundo — com
# varios jogadores isso vira poluicao. max_damage so existe em item com
# durabilidade, entao ele e o filtro.
execute if items entity @s armor.head *[minecraft:max_damage] run item modify entity @s armor.head gckits:reparar
execute if items entity @s armor.chest *[minecraft:max_damage] run item modify entity @s armor.chest gckits:reparar
execute if items entity @s armor.legs *[minecraft:max_damage] run item modify entity @s armor.legs gckits:reparar
execute if items entity @s armor.feet *[minecraft:max_damage] run item modify entity @s armor.feet gckits:reparar
execute if items entity @s weapon.mainhand *[minecraft:max_damage] run item modify entity @s weapon.mainhand gckits:reparar
execute if items entity @s weapon.offhand *[minecraft:max_damage] run item modify entity @s weapon.offhand gckits:reparar
