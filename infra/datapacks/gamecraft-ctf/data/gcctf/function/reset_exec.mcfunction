# Limpa o que ficou solto na area — item no chao, TNT no ar — e so entao devolve
# os blocos do backup.
$kill @e[type=minecraft:item,x=$(minx),dx=$(dx),y=$(miny),dy=$(dy),z=$(minz),dz=$(dz)]
$kill @e[type=minecraft:tnt,x=$(minx),dx=$(dx),y=$(miny),dy=$(dy),z=$(minz),dz=$(dz)]
$clone from minecraft:overworld $(minx) $(bkpy) $(minz) $(maxx) $(bkpymax) $(maxz) to minecraft:overworld $(minx) $(miny) $(minz) replace force
tellraw @a [{"text":"[MegaGames] Mapa restaurado ao ultimo save.","color":"aqua"}]
