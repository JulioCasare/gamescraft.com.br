# Quem entrou agora ainda nao tem kit.
execute as @a[tag=!gc_kitted] run function gckits:novo

# Quem voltou depois de sair do jogo volta ao spawn, mas sem kit novo.
execute as @a[scores={gc_left=1..}] run function gckits:entrou

# Morreu: guarda a pendencia. Entregar kit com o jogador morto nao funciona —
# o inventario e limpo no respawn.
execute as @a[scores={gc_deaths=1..}] run function gckits:marcar

# Renasceu (ja esta vivo) com kit pendente: entrega agora.
execute as @a[scores={gc_pending=1}] unless entity @s[nbt={Health:0.0f}] run function gckits:entregar

# Deu ou levou dano: entra em combate por 15 segundos.
execute as @a[scores={gc_dmg=1..}] run function gckits:marcar_combate
execute as @a[scores={gc_hurt=1..}] run function gckits:marcar_combate
execute as @a[scores={gc_combat=1..}] run scoreboard players remove @s gc_combat 1

# Zona segura em volta do spawn.
execute as @a run function gckits:zona

# Blocos e fluidos colocados por jogador: 5 minutos e somem.
scoreboard players remove @e[type=minecraft:marker,tag=gc_temp] gc_timer 1
execute as @e[type=minecraft:marker,tag=gc_temp,scores={gc_timer=..0}] at @s run function gckits:sumir

# Pavio da TNT sem dano de bloco.
scoreboard players remove @e[type=minecraft:marker,tag=gc_boom] gc_timer 1
execute as @e[type=minecraft:marker,tag=gc_boom,scores={gc_timer=..0}] at @s run function gckits:explodir
