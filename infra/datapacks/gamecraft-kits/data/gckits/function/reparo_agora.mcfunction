scoreboard players set #tick gc_zone 0
scoreboard players set #reparar gc_zone 0
# Nada de conserto enquanto alguem esta de modo construcao ligado: e justamente
# quando o mapa precisa aceitar bloco removido de proposito.
execute if data storage gckits:arena {salvo:1b} unless entity @a[tag=gc_build] run function gckits:reparo_exec with storage gckits:arena
