scoreboard players set #ativo gcctf 0
# Nenhum pedaco pode ficar preso depois do fim: chunk em forceload nunca
# descarrega, e o servidor iria juntando mapa na memoria ate cair.
forceload remove all
# O selo so nasce quando a copia inteira passou. Antes ele era escrito junto com
# o comando de copia, e um /clone que falhava deixava o selo de pe: o /reset
# devolvia um backup que nunca existiu.
execute if score #modo gcctf matches 1 run data modify storage gcctf:arena salvo set value 1b
execute if score #modo gcctf matches 1 run tellraw @a [{"text":"[MegaGames] Mapa salvo.","color":"green"}]
execute if score #modo gcctf matches 2 run tellraw @a [{"text":"[MegaGames] Mapa restaurado ao ultimo save.","color":"aqua"}]
# A limpeza leva a copia de seguranca junto: sem ela o /reset tem de recusar.
execute if score #modo gcctf matches 3 run data remove storage gcctf:arena salvo
execute if score #modo gcctf matches 3 run tellraw @a [{"text":"[MegaGames] Espaco acima do mapa limpo.","color":"gold"}]
scoreboard players set #modo gcctf 0
