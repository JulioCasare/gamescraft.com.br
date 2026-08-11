# Desce um bloco por vez ate achar chao com dois blocos de ar em cima. O contador
# limita a descida: sem ele, um sorteio em cima de um buraco desceria ate o fundo
# do mundo.
scoreboard players remove #mdesce gc_r 1
execute if score #mdesce gc_r matches ..0 run return fail

# Chao e o que nao pode ser substituido: ar, agua, lava e mato sao replaceable,
# entao mob nenhum nasce boiando nem afundado.
execute if block ~ ~ ~ #minecraft:air if block ~ ~1 ~ #minecraft:air unless block ~ ~-1 ~ #minecraft:replaceable run return run function gckits:mob_nascer

execute positioned ~ ~-1 ~ run function gckits:mob_chao
