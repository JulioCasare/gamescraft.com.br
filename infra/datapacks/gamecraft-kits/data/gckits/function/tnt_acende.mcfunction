# Disparado pela conquista invisivel gckits:colocou_bloco, toda vez que alguem
# coloca qualquer bloco. Revoga na hora para poder disparar de novo, e sai
# procurando TNT na direcao em que o jogador esta olhando.
advancement revoke @s only gckits:colocou_bloco
execute anchored eyes positioned ^ ^ ^ run function gckits:tnt_raio
