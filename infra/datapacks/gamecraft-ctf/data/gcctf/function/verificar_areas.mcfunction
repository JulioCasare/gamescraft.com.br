# As areas marcadas a mao. Aqui a trava e o placar #copiado: enquanto o guardar
# nao tiver rodado uma vez, nada e restaurado — senao a primeira passagem
# apagaria a area inteira com o vazio da copia que ainda nao existe.
execute if score #copiado gcctf matches 1 unless blocks 26 -40 -207 65 -17 -183 26 67 -207 all run clone from minecraft:overworld 26 -40 -207 65 -17 -183 to minecraft:overworld 26 67 -207 replace force
execute if score #copiado gcctf matches 1 unless blocks -26 -40 190 12 -17 214 -26 65 190 all run clone from minecraft:overworld -26 -40 190 12 -17 214 to minecraft:overworld -26 65 190 replace force
