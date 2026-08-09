tag @s add gc_na_zona
# Aventura impede quebrar bloco e mexer em moldura sem precisar cancelar evento.
gamemode adventure @s
# Dano de ataque multiplicado por zero: dentro da area ninguem machuca ninguem,
# esteja o alvo dentro ou fora.
attribute @s minecraft:attack_damage modifier add gckits:zona -1 add_multiplied_total
