# Fora da area: guarda a posicao, para saber de onde a pessoa veio.
$execute as @a unless entity @s[x=$(minx),dx=$(dx),y=$(miny),dy=$(dy),z=$(minz),dz=$(dz)] run function gckits:guardar_pos
# Dentro e em paz: imune.
$execute as @a[x=$(minx),dx=$(dx),y=$(miny),dy=$(dy),z=$(minz),dz=$(dz)] if score @s gc_combat matches 0 run effect give @s minecraft:resistance 2 4 true
# Dentro e em combate: volta para onde estava. Isso tambem cobre quem ataca de
# dentro da area — o golpe marca combate e no tick seguinte ele e expulso.
$execute as @a[x=$(minx),dx=$(dx),y=$(miny),dy=$(dy),z=$(minz),dz=$(dz)] if score @s gc_combat matches 1.. if score @s gc_x matches -30000000..30000000 run function gckits:expulsar
