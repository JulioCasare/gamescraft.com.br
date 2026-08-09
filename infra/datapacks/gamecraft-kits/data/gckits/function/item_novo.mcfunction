# Item que acabou de cair: marca como visto (para nao contar de novo) e pede
# conserto. Item de jogador morto tambem cai aqui, mas um clone a mais nao faz
# mal — o conserto so devolve o que estava no backup.
tag @s add gc_visto
scoreboard players set #reparar gc_zone 1
