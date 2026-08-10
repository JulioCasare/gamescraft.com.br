# So repara o que existe: aplicar o modificador num espaco vazio gera aviso no
# log a cada segundo, e com varios jogadores isso vira poluicao.
execute if items entity @s armor.head * run item modify entity @s armor.head gckits:reparar
execute if items entity @s armor.chest * run item modify entity @s armor.chest gckits:reparar
execute if items entity @s armor.legs * run item modify entity @s armor.legs gckits:reparar
execute if items entity @s armor.feet * run item modify entity @s armor.feet gckits:reparar
execute if items entity @s weapon.mainhand * run item modify entity @s weapon.mainhand gckits:reparar
execute if items entity @s weapon.offhand * run item modify entity @s weapon.offhand gckits:reparar
