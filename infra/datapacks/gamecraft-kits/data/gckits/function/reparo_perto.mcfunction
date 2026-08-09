# Conserta a caixa de 8 blocos em volta de quem esta jogando. E o mesmo
# caminho do conserto por item, so que a posicao vem do jogador.
execute store result score #ix gc_zone run data get entity @s Pos[0] 1
execute store result score #iy gc_zone run data get entity @s Pos[1] 1
execute store result score #iz gc_zone run data get entity @s Pos[2] 1
function gckits:reparo_local_prep
