# As areas marcadas a mao. Aqui a trava e o placar #copiado: enquanto o guardar
# nao tiver rodado uma vez, nada e restaurado — senao a primeira passagem
# apagaria a area inteira com o vazio da copia que ainda nao existe.
execute if block -26 -41 190 minecraft:bedrock unless blocks -26 -40 190 12 -17 214 -26 65 190 all run clone from minecraft:overworld -26 -40 190 12 -17 214 to minecraft:overworld -26 65 190 replace force
