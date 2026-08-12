scoreboard players set @s obras 0
# A marca temporaria existe porque as duas linhas rodam no mesmo tick: sem ela,
# o mesmo comando ligava e desligava de novo em seguida.
execute if entity @s[tag=!gc_obras] run function gcctf:obras_ligar
execute if entity @s[tag=gc_obras,tag=!gc_alternar] run function gcctf:obras_desligar
tag @s remove gc_alternar
