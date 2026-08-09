scoreboard players set @s build 0
execute unless entity @s[tag=gc_admin] run tellraw @s {"text":"So a administracao pode usar o modo construcao.","color":"red"}
execute if entity @s[tag=gc_admin,tag=!gc_build] run function gckits:build_ligar
execute if entity @s[tag=gc_admin,tag=gc_build] run function gckits:build_desligar
