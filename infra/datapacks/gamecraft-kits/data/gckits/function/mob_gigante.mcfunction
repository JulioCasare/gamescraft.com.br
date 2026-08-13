# O zumbi gigante.
#
# Nao e o minecraft:giant: aquele nasce sem inteligencia nenhuma, fica parado e
# nao ataca. Este e um zumbi comum crescido pelo atributo de escala — anda,
# persegue e bate igual, so que do tamanho de tres.
#
# 100 de vida e o dobro do dano de um zumbi comum: o zumbi bate 3 no normal,
# entao 6. O veneno de quem encosta nele e tratado no tick.
summon minecraft:zombie ~ ~ ~ {Tags:["gc_mob","gc_gigante","gc_novo"],PersistenceRequired:1b,attributes:[{id:"minecraft:max_health",base:100},{id:"minecraft:scale",base:3},{id:"minecraft:attack_damage",base:6}],Health:100f}
