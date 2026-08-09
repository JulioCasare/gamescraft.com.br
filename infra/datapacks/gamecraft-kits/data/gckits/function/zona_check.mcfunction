# Fora da area: guarda a posicao, para saber de onde a pessoa veio.
$execute as @a unless entity @s[x=$(minx),dx=$(dx),y=$(miny),dy=$(dy),z=$(minz),dz=$(dz)] run function gckits:guardar_pos

# Acabou de entrar: vira aventura (nao quebra bloco, nao mexe em moldura) e
# perde o dano de ataque (nao machuca ninguem, nem dentro nem fora).
$execute as @a[gamemode=survival,tag=!gc_na_zona,x=$(minx),dx=$(dx),y=$(miny),dy=$(dy),z=$(minz),dz=$(dz)] run function gckits:zona_entrar

# Saiu da area: devolve sobrevivencia e o dano.
$execute as @a[tag=gc_na_zona] unless entity @s[x=$(minx),dx=$(dx),y=$(miny),dy=$(dy),z=$(minz),dz=$(dz)] run function gckits:zona_sair

# Dentro e em paz: imune a dano de quem esta fora.
$execute as @a[x=$(minx),dx=$(dx),y=$(miny),dy=$(dy),z=$(minz),dz=$(dz)] if score @s gc_combat matches 0 run effect give @s minecraft:resistance 2 4 true

# Dentro e em combate: volta para onde estava. Isso tambem cobre quem tenta
# atacar de dentro — o golpe marca combate e no tick seguinte ele e expulso.
$execute as @a[x=$(minx),dx=$(dx),y=$(miny),dy=$(dy),z=$(minz),dz=$(dz)] if score @s gc_combat matches 1.. if score @s gc_x matches -30000000..30000000 run function gckits:expulsar

# Molduras e quadros dentro da area ficam fixos e invulneraveis, para ninguem
# girar nem arrancar o que estiver exposto.
$execute as @e[type=#minecraft:item_frame_types,x=$(minx),dx=$(dx),y=$(miny),dy=$(dy),z=$(minz),dz=$(dz)] run data merge entity @s {Invulnerable:1b,Fixed:1b}
$execute as @e[type=minecraft:painting,x=$(minx),dx=$(dx),y=$(miny),dy=$(dy),z=$(minz),dz=$(dz)] run data merge entity @s {Invulnerable:1b}
