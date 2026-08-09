# Alterna o modo construcao. A marca gc_alternar existe para as duas linhas
# nao dispararem em sequencia: quem desliga tira a marca junto, entao a linha
# de ligar nao encontra mais o gatilho e o comando faz uma coisa so.
scoreboard players set @s build 0
execute unless entity @s[tag=gc_admin] run tellraw @s {"text":"So a administracao pode usar o modo construcao.","color":"red"}
tag @s add gc_alternar
execute if entity @s[tag=gc_admin,tag=gc_build] run function gckits:build_desligar
execute if entity @s[tag=gc_admin,tag=gc_alternar] run function gckits:build_ligar
tag @s remove gc_alternar
