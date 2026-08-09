# Marca quem esta dentro da area. A marca serve para o resto do datapack saber
# disso sem precisar repetir a caixa — inclusive o conserto do mapa, que nao
# roda perto de quem esta na area segura (o clone recoloca blocos e isso fecha
# bau, forno ou qualquer janela aberta).
$tag @a[x=$(minx),dx=$(dx),y=$(miny),dy=$(dy),z=$(minz),dz=$(dz)] add gc_dentro
$execute as @a[tag=gc_dentro] unless entity @s[x=$(minx),dx=$(dx),y=$(miny),dy=$(dy),z=$(minz),dz=$(dz)] run tag @s remove gc_dentro

# Fora da area: guarda a posicao, para saber de onde a pessoa veio.
$execute as @a unless entity @s[x=$(minx),dx=$(dx),y=$(miny),dy=$(dy),z=$(minz),dz=$(dz)] run function gckits:guardar_pos

# Acabou de entrar: vira aventura (nao quebra bloco, nao mexe em moldura) e
# perde o dano de ataque (nao machuca ninguem, nem dentro nem fora).
$execute as @a[gamemode=survival,tag=!gc_na_zona,x=$(minx),dx=$(dx),y=$(miny),dy=$(dy),z=$(minz),dz=$(dz)] run function gckits:zona_entrar

# Saiu da area: devolve sobrevivencia e o dano.
$execute as @a[tag=gc_na_zona] unless entity @s[x=$(minx),dx=$(dx),y=$(miny),dy=$(dy),z=$(minz),dz=$(dz)] run function gckits:zona_sair

# Imunidade a dano dentro da area, sem excecao. Antes so valia para quem estava
# em paz, e por isso quem tinha acabado de brigar podia ser alvejado de fora
# dentro da propria area segura.
$execute as @a[x=$(minx),dx=$(dx),y=$(miny),dy=$(dy),z=$(minz),dz=$(dz)] run effect give @s minecraft:resistance 2 4 true

# Projetil que entra na area morre no ar: flecha, tridente, projetil de vento,
# bola de fogo, neve e ovo. Sem isso, quem esta fora empurra e acerta quem
# esta dentro mesmo sem causar dano.
$kill @e[type=#minecraft:arrows,x=$(minx),dx=$(dx),y=$(miny),dy=$(dy),z=$(minz),dz=$(dz)]
$kill @e[type=minecraft:trident,x=$(minx),dx=$(dx),y=$(miny),dy=$(dy),z=$(minz),dz=$(dz)]
$kill @e[type=minecraft:wind_charge,x=$(minx),dx=$(dx),y=$(miny),dy=$(dy),z=$(minz),dz=$(dz)]
$kill @e[type=minecraft:fireball,x=$(minx),dx=$(dx),y=$(miny),dy=$(dy),z=$(minz),dz=$(dz)]
$kill @e[type=minecraft:snowball,x=$(minx),dx=$(dx),y=$(miny),dy=$(dy),z=$(minz),dz=$(dz)]
$kill @e[type=minecraft:egg,x=$(minx),dx=$(dx),y=$(miny),dy=$(dy),z=$(minz),dz=$(dz)]

# Dentro e em combate: volta para onde estava. Isso tambem cobre quem tenta
# atacar de dentro — o golpe marca combate e no tick seguinte ele e expulso.
$execute as @a[x=$(minx),dx=$(dx),y=$(miny),dy=$(dy),z=$(minz),dz=$(dz)] if score @s gc_combat matches 1.. if score @s gc_x matches -30000000..30000000 run function gckits:expulsar

# Molduras e quadros dentro da area ficam fixos e invulneraveis. Fixed impede
# girar o item e arrancar a moldura; Invulnerable impede quebrar no soco.
# Os tipos vao escritos um a um de proposito: nome de grupo de entidade que nao
# existe faz o comando inteiro falhar calado.
$execute as @e[type=minecraft:item_frame,x=$(minx),dx=$(dx),y=$(miny),dy=$(dy),z=$(minz),dz=$(dz)] run data merge entity @s {Invulnerable:1b,Fixed:1b}
$execute as @e[type=minecraft:glow_item_frame,x=$(minx),dx=$(dx),y=$(miny),dy=$(dy),z=$(minz),dz=$(dz)] run data merge entity @s {Invulnerable:1b,Fixed:1b}
$execute as @e[type=minecraft:painting,x=$(minx),dx=$(dx),y=$(miny),dy=$(dy),z=$(minz),dz=$(dz)] run data merge entity @s {Invulnerable:1b,Fixed:1b}
$execute as @e[type=minecraft:armor_stand,x=$(minx),dx=$(dx),y=$(miny),dy=$(dy),z=$(minz),dz=$(dz)] run data merge entity @s {Invulnerable:1b,NoGravity:1b,DisabledSlots:4144959}
