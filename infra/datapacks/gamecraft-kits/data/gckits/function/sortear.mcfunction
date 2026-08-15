# Kit misturado: TUDO tem material sorteado — as quatro pecas de armadura, a
# espada, a lanca e as ferramentas. O equilibrio vem do orcamento: cada peca de
# armadura custa de 1 (couro) a 5 (diamante), e a soma (4 a 20) define a faixa
# de material das armas. Armadura pesada -> armas fracas, e vice-versa.
#
# A ordem importa duas vezes: as armas entram logo depois do clear, para caberem
# nos dois primeiros espacos (gckits:encantamentos conta com isso), e os
# encantamentos so entram depois de todos os itens encantaveis estarem no
# inventario.
clear @s
scoreboard players set @s gc_s 0
# Conta de pecas de diamante deste kit. Duas ja e muita defesa; a terceira e a
# quarta faziam kits que so morriam de totem, e quem pegava o outro lado do
# sorteio nao tinha o que fazer. A terceira vira ferro.
scoreboard players set #dima gc_r 0
function gckits:slot_cabeca
function gckits:slot_peito
function gckits:slot_pernas
function gckits:slot_pes
function gckits:ferramentas
function gckits:arma
function gckits:extras
function gckits:encantamentos
function gckits:blocos
function gckits:consumiveis
function gckits:pocoes
function gckits:defesa
function gckits:aviso
