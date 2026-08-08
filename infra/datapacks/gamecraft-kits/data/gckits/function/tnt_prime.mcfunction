# Troca o bloco pela TNT ja acesa. 40 ticks = 2 segundos de pavio, tempo de
# quem colocou sair de perto e de quem esta chegando reagir.
setblock ~ ~ ~ air
summon minecraft:tnt ~0.5 ~ ~0.5 {fuse:40}
