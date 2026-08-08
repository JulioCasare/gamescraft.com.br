# Kit misturado: cada peca de armadura e sorteada por conta propria, entao
# quase nunca sai um conjunto completo. O equilibrio vem do orcamento: cada
# peca custa de 1 (couro) a 5 (diamante), e a arma e escolhida pelo que sobrou.
# Armadura forte recebe arma fraca; quem sai pelado leva a arma mais perigosa.
clear @s
scoreboard players set @s gc_s 0
function gckits:slot_cabeca
function gckits:slot_peito
function gckits:slot_pernas
function gckits:slot_pes
function gckits:arma
give @s minecraft:cooked_beef 6
function gckits:aviso
