scoreboard players set #tick gc_zone 0
# Nada de conserto enquanto alguem esta com o modo construcao ligado, e nada
# em volta de quem esta na area segura: la ninguem quebra bloco, e o clone
# fecharia qualquer bau ou janela aberta.
execute unless entity @a[tag=gc_build] if data storage gckits:arena {salvo:1b} as @a[tag=!gc_dentro] run function gckits:reparo_perto
