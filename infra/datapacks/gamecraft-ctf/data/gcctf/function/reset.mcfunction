# Devolve a area ao estado do ultimo save.
execute unless data storage gcctf:arena salvo run tellraw @s [{"text":"Nao ha nada salvo ainda. Use /save primeiro.","color":"red"}]
execute if data storage gcctf:arena salvo run function gcctf:reset_exec with storage gcctf:arena
