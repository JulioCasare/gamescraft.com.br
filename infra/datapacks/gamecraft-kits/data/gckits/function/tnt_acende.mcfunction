# Disparado pela conquista invisivel gckits:colocou_bloco, toda vez que alguem
# coloca qualquer bloco. Revoga na hora para poder disparar de novo, e sai
# procurando na direcao em que o jogador esta olhando: primeiro TNT (que acende
# sozinha), depois qualquer bloco recem-posto (que ganha prazo de validade).
#
# Quem esta em modo construcao (/trigger build) escapa das duas coisas: o que
# ele coloca fica no mapa e a TNT dele nao acende sozinha.
advancement revoke @s only gckits:colocou_bloco
execute unless entity @s[tag=gc_build] anchored eyes positioned ^ ^ ^ run function gckits:tnt_raio
execute unless entity @s[tag=gc_build] anchored eyes positioned ^ ^ ^ run function gckits:bloco_raio
