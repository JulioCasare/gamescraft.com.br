# Limpa o que os jogadores deixaram na area — itens no chao, TNT no ar,
# marcadores de bloco temporario — e so entao devolve os blocos do backup.
$kill @e[type=minecraft:item,x=$(minx),dx=$(dx),y=$(miny),dy=$(dy),z=$(minz),dz=$(dz)]
$kill @e[type=minecraft:tnt,x=$(minx),dx=$(dx),y=$(miny),dy=$(dy),z=$(minz),dz=$(dz)]
$kill @e[type=minecraft:marker,tag=gc_temp,x=$(minx),dx=$(dx),y=$(miny),dy=$(dy),z=$(minz),dz=$(dz)]
$kill @e[type=minecraft:marker,tag=gc_boom,x=$(minx),dx=$(dx),y=$(miny),dy=$(dy),z=$(minz),dz=$(dz)]
$clone from minecraft:overworld $(minx) $(bkpy) $(minz) $(maxx) $(bkpymax) $(maxz) to minecraft:overworld $(minx) $(miny) $(minz) replace force
tellraw @a [{"text":"[Arena] Mapa restaurado ao ultimo save.","color":"aqua"}]
