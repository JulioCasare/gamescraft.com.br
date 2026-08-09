# Disparado ao usar qualquer item num bloco. Serve para pegar balde: agua e
# lava nao contam como "bloco colocado" e nao disparam a outra conquista.
advancement revoke @s only gckits:usou_item
execute anchored eyes positioned ^ ^ ^ run function gckits:fluido_raio
