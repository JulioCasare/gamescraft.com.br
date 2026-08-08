# Quem entrou agora ainda nao tem kit.
execute as @a[tag=!gc_kitted] run function gckits:novo

# Morreu: guarda a pendencia. Entregar kit com o jogador morto nao funciona —
# o inventario e limpo no respawn.
execute as @a[scores={gc_deaths=1..}] run function gckits:marcar

# Renasceu (ja esta vivo) com kit pendente: entrega agora.
execute as @a[scores={gc_pending=1}] unless entity @s[nbt={Health:0.0f}] run function gckits:entregar
