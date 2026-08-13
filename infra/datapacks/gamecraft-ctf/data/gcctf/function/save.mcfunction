# Guarda o estado atual da area. Copia ela inteira para 150 blocos acima, onde
# ninguem chega, e e de la que o /reset traz tudo de volta.
execute unless data storage gcctf:arena minx run tellraw @s [{"text":"Marque a area antes: /arena1 num canto e /arena2 no oposto.","color":"red"}]
execute if data storage gcctf:arena minx run function gcctf:save_exec with storage gcctf:arena
