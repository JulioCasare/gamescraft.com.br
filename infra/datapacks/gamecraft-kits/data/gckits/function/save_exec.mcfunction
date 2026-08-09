$clone from minecraft:overworld $(minx) $(miny) $(minz) $(maxx) $(maxy) $(maxz) to minecraft:overworld $(minx) $(bkpy) $(minz) replace force
tellraw @a [{"text":"[Arena] Estado salvo. ","color":"green"},{"text":"/function gckits:reset devolve tudo a este ponto.","color":"gray"}]
