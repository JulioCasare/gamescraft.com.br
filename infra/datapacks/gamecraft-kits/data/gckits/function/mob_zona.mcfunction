# A caixa da area segura vem do armazenamento, entao acompanha o /zona1 e
# /zona2 sem mexer em arquivo.
$execute if entity @s[x=$(minx),y=$(miny),z=$(minz),dx=$(dx),dy=$(dy),dz=$(dz)] run function gckits:mob_voltar with storage gckits:mob
