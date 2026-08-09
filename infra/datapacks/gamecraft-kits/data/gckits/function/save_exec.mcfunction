$clone from minecraft:overworld $(minx) $(miny) $(minz) $(maxx) $(maxy) $(maxz) to minecraft:overworld $(minx) $(bkpy) $(minz) replace force
data modify storage gckits:arena salvo set value 1b
tellraw @a [{"text":"[Arena] Estado salvo. ","color":"green"},{"text":"A partir de agora, bloco quebrado do mapa volta sozinho em ate 30 segundos.","color":"gray"}]
