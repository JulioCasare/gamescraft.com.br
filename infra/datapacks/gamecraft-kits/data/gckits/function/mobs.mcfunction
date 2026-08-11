# Nasce um mob por vez, perto do meio da arena. Um por ciclo mantem a pressao
# constante sem virar horda. O ciclo em si e de 15 segundos, contado no
# cada_segundo.
scoreboard players set #ms gc_zone 0

# Nada nasce enquanto alguem esta com o modo construcao ligado.
execute if entity @a[tag=gc_build] run return 0

# So nasce mob se tem alguem jogando de verdade: em sobrevivencia e fora da
# area segura. Com o servidor vazio a arena fica limpa.
execute unless entity @a[gamemode=survival,tag=!gc_dentro] run return 0

# Teto de mobs vivos ao mesmo tempo. Filhote de cubo de magma nao entra na
# conta: um cubo grande vira quatro medios e cada medio vira mais quatro
# pequenos, entao contar todos travaria o nascimento so porque alguem matou um
# cubo.
execute store result score #nmob gc_r if entity @e[tag=gc_mob,type=!minecraft:magma_cube]
execute store result score #ncubo gc_r if entity @e[tag=gc_mob,type=minecraft:magma_cube,nbt={Size:3}]
scoreboard players operation #nmob gc_r += #ncubo gc_r
execute if score #nmob gc_r matches 15.. run return 0

# Sorteia um ponto ate 12 blocos do centro.
execute store result score #cx gc_r run data get storage gckits:mob cx
execute store result score #cz gc_r run data get storage gckits:mob cz
execute store result score #off gc_r run random value -12..12
scoreboard players operation #cx gc_r += #off gc_r
execute store result score #off gc_r run random value -12..12
scoreboard players operation #cz gc_r += #off gc_r
execute store result storage gckits:mob x int 1 run scoreboard players get #cx gc_r
execute store result storage gckits:mob z int 1 run scoreboard players get #cz gc_r

# Procura chao descendo. Se o sorteio caiu num lugar sem chao — buraco, parede,
# telhado — o ciclo passa em branco e o proximo tenta outro ponto.
scoreboard players set #mdesce gc_r 16
function gckits:mob_pos with storage gckits:mob
