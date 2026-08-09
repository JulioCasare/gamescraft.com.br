tag @s remove gc_build
# Tira tambem o gatilho, para a linha seguinte de trigger_build nao religar.
tag @s remove gc_alternar
tellraw @s [{"text":"Modo construcao DESLIGADO. ","color":"yellow"},{"text":"Voltou a valer o prazo de 5 minutos, e o mapa volta a se consertar sozinho.","color":"gray"}]
