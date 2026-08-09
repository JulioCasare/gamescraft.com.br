# Quem reconecta volta para o spawn com o que ja tinha — nada de kit novo, para
# ninguem sair e entrar de proposito atras de um sorteio melhor.
# Quem deslogou em combate e morto pelo FoCombatTag; a morte e que gera o kit
# novo, pelo caminho normal do respawn.
scoreboard players set @s gc_left 0
execute if entity @s[name=JulioCasare] run tag @s add gc_admin
function gckits:para_o_spawn
