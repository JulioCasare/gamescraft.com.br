# O seletor de posicao precisa dos numeros escritos, entao a busca comeca aqui.
# Sobe 8 blocos acima da altura do centro antes de descer: assim acha o telhado
# de uma construcao tanto quanto o chao liso.
$execute positioned $(x) $(cy) $(z) positioned ~ ~8 ~ run function gckits:mob_chao
