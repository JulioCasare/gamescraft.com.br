# Placares que sustentam o sorteio de kit.
# gc_deaths conta mortes (criterio deathCount); gc_pending marca quem morreu e
# ainda nao renasceu; gc_r guarda os numeros sorteados; gc_s soma o custo das
# quatro pecas de armadura (4 a 20) e define a forca da arma.
scoreboard objectives add gc_deaths deathCount
scoreboard objectives add gc_pending dummy
scoreboard objectives add gc_r dummy
scoreboard objectives add gc_s dummy
