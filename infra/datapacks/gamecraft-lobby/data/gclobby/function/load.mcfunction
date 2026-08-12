# gcl_left conta saidas do jogo: fica 1 enquanto o jogador esta fora, entao no
# proximo login da para saber que ele acabou de chegar e manda-lo ao centro.
scoreboard objectives add gcl_left minecraft.custom:minecraft.leave_game
scoreboard objectives add gcl_deaths deathCount
# Contador do bloco de uma vez por segundo.
scoreboard objectives add gcl_t dummy
