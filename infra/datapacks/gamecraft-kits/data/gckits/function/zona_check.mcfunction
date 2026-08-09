# Marca quem esta dentro da area. A marca serve para o resto do datapack saber
# disso sem repetir a caixa — inclusive o conserto do mapa, que nao roda perto
# de quem esta na area segura (o clone recoloca blocos e isso fecha bau).
$tag @a[x=$(minx),dx=$(dx),y=$(miny),dy=$(dy),z=$(minz),dz=$(dz)] add gc_dentro
$execute as @a[tag=gc_dentro] unless entity @s[x=$(minx),dx=$(dx),y=$(miny),dy=$(dy),z=$(minz),dz=$(dz)] run tag @s remove gc_dentro

# Guarda de onde a pessoa veio, mas so quando ela esta bem fora da area (3
# blocos de folga). Sem essa margem, quem estava colado na borda era devolvido
# para dentro e parecia ter entrado em combate.
$execute as @a unless entity @s[x=$(minx2),dx=$(dx2),y=$(miny2),dy=$(dy2),z=$(minz2),dz=$(dz2)] run function gckits:guardar_pos

# Protecao de quem esta dentro, seja qual for o modo de jogo: dano de ataque
# zerado (nao machuca ninguem, dentro ou fora), empurrao anulado e imunidade.
# Antes so valia para quem entrou em sobrevivencia, entao quem estava em
# criativo continuava batendo de dentro.
execute as @a[tag=gc_dentro,tag=!gc_semdano] run function gckits:zona_protecao_on
execute as @a[tag=gc_semdano] unless entity @s[tag=gc_dentro] run function gckits:zona_protecao_off
$execute as @a[x=$(minx),dx=$(dx),y=$(miny),dy=$(dy),z=$(minz),dz=$(dz)] run effect give @s minecraft:resistance 2 4 true

# Acabou de entrar em sobrevivencia: vira aventura, para nao quebrar bloco nem
# mexer em moldura.
$execute as @a[gamemode=survival,tag=!gc_na_zona,x=$(minx),dx=$(dx),y=$(miny),dy=$(dy),z=$(minz),dz=$(dz)] run function gckits:zona_entrar
$execute as @a[tag=gc_na_zona] unless entity @s[x=$(minx),dx=$(dx),y=$(miny),dy=$(dy),z=$(minz),dz=$(dz)] run function gckits:zona_sair

# A barreira contra projetil. Tridente ganha Lealdade e volta para a mao de
# quem jogou; flecha perde o impulso e cai no chao ali mesmo, na beirada. O
# resto (projetil de vento, bola de fogo, neve, ovo) some no ar.
$execute as @e[type=#minecraft:arrows,x=$(minx),dx=$(dx),y=$(miny),dy=$(dy),z=$(minz),dz=$(dz)] run data merge entity @s {Motion:[0.0,0.0,0.0],crit:0b}
$execute as @e[type=minecraft:trident,x=$(minx),dx=$(dx),y=$(miny),dy=$(dy),z=$(minz),dz=$(dz)] run data merge entity @s {item:{id:"minecraft:trident",count:1,components:{"minecraft:enchantments":{"minecraft:loyalty":3}}},DealtDamage:1b}
$kill @e[type=minecraft:wind_charge,x=$(minx),dx=$(dx),y=$(miny),dy=$(dy),z=$(minz),dz=$(dz)]
$kill @e[type=minecraft:fireball,x=$(minx),dx=$(dx),y=$(miny),dy=$(dy),z=$(minz),dz=$(dz)]
$kill @e[type=minecraft:snowball,x=$(minx),dx=$(dx),y=$(miny),dy=$(dy),z=$(minz),dz=$(dz)]
$kill @e[type=minecraft:egg,x=$(minx),dx=$(dx),y=$(miny),dy=$(dy),z=$(minz),dz=$(dz)]

# Dentro e em combate: volta para onde estava.
$execute as @a[x=$(minx),dx=$(dx),y=$(miny),dy=$(dy),z=$(minz),dz=$(dz)] if score @s gc_combat matches 1.. if score @s gc_x matches -30000000..30000000 run function gckits:expulsar

# Molduras, quadros e suportes dentro da area ficam fixos e invulneraveis.
# Os tipos vao escritos um a um: nome de grupo que nao existe falha calado.
$execute as @e[type=minecraft:item_frame,x=$(minx),dx=$(dx),y=$(miny),dy=$(dy),z=$(minz),dz=$(dz)] run data merge entity @s {Invulnerable:1b,Fixed:1b}
$execute as @e[type=minecraft:glow_item_frame,x=$(minx),dx=$(dx),y=$(miny),dy=$(dy),z=$(minz),dz=$(dz)] run data merge entity @s {Invulnerable:1b,Fixed:1b}
$execute as @e[type=minecraft:painting,x=$(minx),dx=$(dx),y=$(miny),dy=$(dy),z=$(minz),dz=$(dz)] run data merge entity @s {Invulnerable:1b,Fixed:1b}
$execute as @e[type=minecraft:armor_stand,x=$(minx),dx=$(dx),y=$(miny),dy=$(dy),z=$(minz),dz=$(dz)] run data merge entity @s {Invulnerable:1b,NoGravity:1b,DisabledSlots:4144959}
