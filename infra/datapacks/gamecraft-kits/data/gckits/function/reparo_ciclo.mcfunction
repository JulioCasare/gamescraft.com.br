scoreboard players set #tick gc_zone 0
# Nada de conserto enquanto alguem esta com o modo construcao ligado.
execute unless entity @a[tag=gc_build] if data storage gckits:arena {salvo:1b} as @a run function gckits:reparo_perto
