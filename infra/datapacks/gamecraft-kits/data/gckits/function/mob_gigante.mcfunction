# O zumbi gigante.
#
# Nao e o minecraft:giant: aquele nasce sem inteligencia nenhuma, fica parado e
# nao ataca. Este e um zumbi comum crescido pelo atributo de escala — anda,
# persegue e bate igual, so que do tamanho de tres.
#
# O dano fica o de zumbi comum de proposito, como o Julio pediu: o que ele tem
# de diferente e a vida de 100 e o veneno, tratado no tick.
summon minecraft:zombie ~ ~ ~ {Tags:["gc_mob","gc_gigante","gc_novo"],PersistenceRequired:1b,attributes:[{id:"minecraft:max_health",base:100},{id:"minecraft:scale",base:3}],Health:100f}
