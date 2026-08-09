scoreboard players set @s reset 0
execute unless entity @s[tag=gc_admin] run tellraw @s {"text":"So a administracao pode resetar a arena.","color":"red"}
execute if entity @s[tag=gc_admin] run function gckits:reset
