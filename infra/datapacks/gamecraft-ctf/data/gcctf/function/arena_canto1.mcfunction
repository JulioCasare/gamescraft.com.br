# Primeiro canto da area que o /save guarda e o /reset devolve.
execute store result score #ax1 gcctf run data get entity @s Pos[0] 1
execute store result score #ay1 gcctf run data get entity @s Pos[1] 1
execute store result score #az1 gcctf run data get entity @s Pos[2] 1
tellraw @s [{"text":"Canto 1 marcado. ","color":"green"},{"text":"Va ao canto oposto e use /arena2.","color":"gray"}]
