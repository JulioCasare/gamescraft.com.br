# Ponto unico de entrada da arena. Trocar aqui muda entrada, respawn e retorno
# de quem reconecta, tudo de uma vez.
# Chegar ao spawn encerra o combate: senao o jogador nasceria dentro da zona
# segura ainda marcado e seria expulso na hora.
tp @s -21 62 -17
scoreboard players set @s gc_combat 0
scoreboard players set @s gc_dmg 0
scoreboard players set @s gc_hurt 0
