$clone from minecraft:overworld $(minx) $(miny) $(minz) $(maxx) $(maxy) $(maxz) to minecraft:overworld $(minx) $(bkpy) $(minz) replace force
data modify storage gcctf:arena salvo set value 1b
tellraw @a [{"text":"[MegaGames] Estado salvo.","color":"green"}]
