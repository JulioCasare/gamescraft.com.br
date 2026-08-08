# Armadura fraquissima sempre leva pocao — e a compensacao dela. Os outros
# kits tem 1 chance em 3.
scoreboard players set #pz gc_r 0
execute if score @s gc_s matches ..8 run scoreboard players set #pz gc_r 1
execute store result score #pr gc_r run random value 1..3
execute if score #pr gc_r matches 1 run scoreboard players set #pz gc_r 1
execute if score #pz gc_r matches 1 run function gckits:dar_pocao
