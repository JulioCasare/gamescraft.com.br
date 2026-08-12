# Roda como a caixa de interacao. Ela guarda o ultimo clique em interaction
# (botao direito) e attack (botao esquerdo); os dois abrem o menu.
execute if data entity @s interaction run function gcnpc:abrir
execute if data entity @s attack run function gcnpc:abrir
# Apagar o registro e o que permite o proximo clique ser visto.
execute if data entity @s interaction run data remove entity @s interaction
execute if data entity @s attack run data remove entity @s attack
