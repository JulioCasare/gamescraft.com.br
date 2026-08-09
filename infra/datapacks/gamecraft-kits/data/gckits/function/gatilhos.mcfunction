# O Minecraft nao deixa criar /save nem /reset de verdade por datapack, mas
# deixa criar gatilhos: o jogador digita /trigger save e /trigger reset.
# Precisa reabilitar a cada uso, por isso a linha roda todo tick.
scoreboard players enable @a save
scoreboard players enable @a reset
execute as @a[scores={save=1..}] run function gckits:trigger_save
execute as @a[scores={reset=1..}] run function gckits:trigger_reset
scoreboard players enable @a build
execute as @a[scores={build=1..}] run function gckits:trigger_build
