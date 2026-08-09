# Disparado ao usar qualquer item num bloco. Serve para pegar balde: agua e
# lava nao contam como "bloco colocado" e nao disparam a outra conquista.
# Em modo construcao a agua e a lava tambem ficam.
advancement revoke @s only gckits:usou_item
execute unless entity @s[tag=gc_build] anchored eyes positioned ^ ^ ^ run function gckits:fluido_raio
