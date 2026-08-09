# Placares que sustentam o sorteio de kit.
# gc_deaths conta mortes (criterio deathCount); gc_pending marca quem morreu e
# ainda nao renasceu; gc_r guarda os numeros sorteados; gc_s soma o custo das
# quatro pecas de armadura (4 a 20) e define a forca da arma.
# gc_left conta saidas do jogo: fica 1 enquanto o jogador esta fora, entao no
# proximo login da para saber que ele acabou de entrar.
scoreboard objectives add gc_deaths deathCount
scoreboard objectives add gc_pending dummy
scoreboard objectives add gc_r dummy
scoreboard objectives add gc_s dummy
scoreboard objectives add gc_left minecraft.custom:minecraft.leave_game

# Marca de combate propria do datapack: o plugin de combat tag nao expoe o
# estado dele para comandos, entao a zona segura precisa da sua propria conta.
# gc_dmg e gc_hurt sao contadores nativos de dano dado e recebido; qualquer
# aumento neles reinicia gc_combat, que decai um por tick (300 = 15 segundos).
scoreboard objectives add gc_dmg minecraft.custom:minecraft.damage_dealt
scoreboard objectives add gc_hurt minecraft.custom:minecraft.damage_taken
scoreboard objectives add gc_combat dummy

# Ultima posicao conhecida fora da zona segura, para devolver quem tentar
# entrar em combate.
scoreboard objectives add gc_x dummy
scoreboard objectives add gc_y dummy
scoreboard objectives add gc_z dummy

# Area segura: definida em jogo por /function gckits:canto1 e canto2. Fica no
# armazenamento, entao sobrevive a reinicio. O valor abaixo e so o padrao de
# fabrica, uma caixa de 24 blocos em volta do spawn, aplicado uma unica vez.
scoreboard objectives add gc_zone dummy
execute unless data storage gckits:zona minx run data merge storage gckits:zona {minx:44,miny:50,minz:-29,dx:24,dy:24,dz:24}

# Prazo de validade dos blocos colocados em partida, contado nos marcadores.
scoreboard objectives add gc_timer dummy

# O clone do save/reset costuma passar do teto padrao de 32768 blocos por
# comando; sem isso o comando falha calado numa arena de tamanho normal.
gamerule max_block_modifications 4000000

# Placares dos gatilhos: o jogador aciona com /trigger save e /trigger reset.
scoreboard objectives add save trigger
scoreboard objectives add reset trigger
scoreboard objectives add build trigger
