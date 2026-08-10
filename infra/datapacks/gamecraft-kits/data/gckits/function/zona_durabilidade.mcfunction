# Uma vez por segundo devolve a durabilidade de quem esta na area segura.
# Sem isso, o golpe que a cura anula ainda gasta armadura — quem fica parado no
# spawn ia perdendo equipamento sem nem estar lutando.
scoreboard players set #tz gc_zone 0
execute as @a[tag=gc_dentro] run function gckits:reparar_equipamento
