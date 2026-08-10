# Quem esta na area segura e levou dano: cura na hora, e o golpe nao conta como
# combate.
#
# A Resistencia V sozinha nao basta: confirmei em jogo que ela e aplicada
# certinho (amplificador 4) e mesmo assim o dano passa — na 26.2 ela reduz, nao
# zera. Entao a protecao real e devolver a vida no mesmo tick.
effect give @s minecraft:instant_health 1 20 true
# Zerar o contador antes do bloco de combate impede que o golpe recebido dentro
# da area marque a pessoa e a expulse da propria area segura.
scoreboard players set @s gc_hurt 0
