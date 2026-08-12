# Centro do porto. Trocar aqui muda entrada, respawn e o resgate de quem cai
# na agua, tudo de uma vez.
# O 0 no fim e a direcao: no Minecraft o angulo 0 aponta para o sul, que e para
# onde o porto se abre. Sem isso o jogador chega olhando para onde estava antes.
tp @s -178 -9 265 0 0
# Cura de um coracao ao chegar: quem foi resgatado da agua ou caiu nao aparece
# no centro morrendo.
effect give @s minecraft:instant_health 1 0 true
