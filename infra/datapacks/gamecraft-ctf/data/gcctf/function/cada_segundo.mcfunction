scoreboard players set #t gcctf 0
# Com alguem de obras ligado, a protecao para: senao a torre voltaria ao estado
# antigo no meio da edicao.
execute unless entity @a[tag=gc_obras] run function gcctf:verificar
