# Kit misturado: TUDO tem material sorteado — as quatro pecas de armadura, a
# espada, a lanca e as ferramentas. O equilibrio vem do orcamento: cada peca de
# armadura custa de 1 (couro) a 5 (diamante), e a soma (4 a 20) define a faixa
# de material das armas. Armadura pesada -> armas fracas, e vice-versa.
#
# A ordem importa: as armas precisam entrar logo depois do clear, para caberem
# nos primeiros espacos do inventario — e assim gckits:encantamentos saber onde
# encontra-las.
clear @s
scoreboard players set @s gc_s 0
function gckits:slot_cabeca
function gckits:slot_peito
function gckits:slot_pernas
function gckits:slot_pes
function gckits:ferramentas
function gckits:arma
function gckits:encantamentos
function gckits:blocos
function gckits:consumiveis
function gckits:pocoes
function gckits:defesa
function gckits:aviso
