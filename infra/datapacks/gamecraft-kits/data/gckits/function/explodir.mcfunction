# O estouro: fogo, som e dano por distancia. O comando damage so aceita um alvo
# por vez, por isso o execute as em volta. Quem esta na area segura tem
# resistencia e nao sente nada, como deve ser.
particle minecraft:explosion_emitter ~ ~ ~ 0 0 0 1 1 force
playsound minecraft:entity.generic.explode block @a ~ ~ ~ 4 1
execute as @a[distance=..3] run damage @s 12 minecraft:explosion
execute as @a[distance=3..6] run damage @s 5 minecraft:explosion
kill @s
