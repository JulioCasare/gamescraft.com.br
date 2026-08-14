# O pedaco que estava carregando vira o da vez, e um novo entra na fila. E isso
# que tira a espera do caminho: os chunks do proximo carregam durante a copia do
# atual, que leva bem mais tempo que o disco precisa.
data modify storage gcctf:tiles atual set from storage gcctf:tiles proximo
function gcctf:tile_pegar
scoreboard players operation #temprox gcctf = #achou gcctf
execute if score #achou gcctf matches 1 run data modify storage gcctf:tiles proximo set from storage gcctf:tiles novo
