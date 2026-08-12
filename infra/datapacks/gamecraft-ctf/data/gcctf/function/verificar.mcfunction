# Uma linha por torre. A comparacao vem antes da copia para o servidor so mexer
# em bloco quando algo mudou de verdade — copiar as 50 toda hora faria os blocos
# piscarem na tela de quem esta perto.
#
# A condicao do sinalizador na copia e trava de seguranca: sem ela, uma torre
# nova sem copia feita seria "consertada" com ar e sumiria.
execute if block -10 -50 205 minecraft:beacon unless blocks -13 -60 202 -7 -50 208 -13 61 202 all run clone from minecraft:overworld -13 -60 202 -7 -50 208 to minecraft:overworld -13 61 202 replace force
execute if block -104 -50 176 minecraft:beacon unless blocks -107 -60 173 -101 -50 179 -107 62 173 all run clone from minecraft:overworld -107 -60 173 -101 -50 179 to minecraft:overworld -107 62 173 replace force
execute if block -111 -50 -133 minecraft:beacon unless blocks -114 -60 -136 -108 -50 -130 -114 60 -136 all run clone from minecraft:overworld -114 -60 -136 -108 -50 -130 to minecraft:overworld -114 60 -136 replace force
execute if block -120 -50 -31 minecraft:beacon unless blocks -123 -60 -34 -117 -50 -28 -123 61 -34 all run clone from minecraft:overworld -123 -60 -34 -117 -50 -28 to minecraft:overworld -123 61 -34 replace force
execute if block -120 -50 32 minecraft:beacon unless blocks -123 -60 29 -117 -50 35 -123 60 29 all run clone from minecraft:overworld -123 -60 29 -117 -50 35 to minecraft:overworld -123 60 29 replace force
execute if block -127 -50 -160 minecraft:beacon unless blocks -130 -60 -163 -124 -50 -157 -130 62 -163 all run clone from minecraft:overworld -130 -60 -163 -124 -50 -157 to minecraft:overworld -130 62 -163 replace force
execute if block -133 -50 112 minecraft:beacon unless blocks -136 -60 109 -130 -50 115 -136 63 109 all run clone from minecraft:overworld -136 -60 109 -130 -50 115 to minecraft:overworld -136 63 109 replace force
execute if block -150 -50 0 minecraft:beacon unless blocks -153 -60 -3 -147 -50 3 -153 63 -3 all run clone from minecraft:overworld -153 -60 -3 -147 -50 3 to minecraft:overworld -153 63 -3 replace force
execute if block -163 -50 -59 minecraft:beacon unless blocks -166 -60 -62 -160 -50 -56 -166 62 -62 all run clone from minecraft:overworld -166 -60 -62 -160 -50 -56 to minecraft:overworld -166 62 -62 replace force
execute if block -171 -50 30 minecraft:beacon unless blocks -174 -60 27 -168 -50 33 -174 63 27 all run clone from minecraft:overworld -174 -60 27 -168 -50 33 to minecraft:overworld -174 63 27 replace force
execute if block -174 -50 107 minecraft:beacon unless blocks -177 -60 104 -171 -50 110 -177 62 104 all run clone from minecraft:overworld -177 -60 104 -171 -50 110 to minecraft:overworld -177 62 104 replace force
execute if block -187 -50 -82 minecraft:beacon unless blocks -190 -60 -85 -184 -50 -79 -190 64 -85 all run clone from minecraft:overworld -190 -60 -85 -184 -50 -79 to minecraft:overworld -190 64 -85 replace force
execute if block -204 -50 14 minecraft:beacon unless blocks -207 -60 11 -201 -50 17 -207 62 11 all run clone from minecraft:overworld -207 -60 11 -201 -50 17 to minecraft:overworld -207 62 11 replace force
execute if block -26 -50 -64 minecraft:beacon unless blocks -29 -60 -67 -23 -50 -61 -29 66 -67 all run clone from minecraft:overworld -29 -60 -67 -23 -50 -61 to minecraft:overworld -29 66 -67 replace force
execute if block -26 -50 65 minecraft:beacon unless blocks -29 -60 62 -23 -50 68 -29 64 62 all run clone from minecraft:overworld -29 -60 62 -23 -50 68 to minecraft:overworld -29 64 62 replace force
execute if block -29 -50 -171 minecraft:beacon unless blocks -32 -60 -174 -26 -50 -168 -32 60 -174 all run clone from minecraft:overworld -32 -60 -174 -26 -50 -168 to minecraft:overworld -32 60 -174 replace force
execute if block -31 -50 -120 minecraft:beacon unless blocks -34 -60 -123 -28 -50 -117 -34 61 -123 all run clone from minecraft:overworld -34 -60 -123 -28 -50 -117 to minecraft:overworld -34 61 -123 replace force
execute if block -31 -50 121 minecraft:beacon unless blocks -34 -60 118 -28 -50 124 -34 62 118 all run clone from minecraft:overworld -34 -60 118 -28 -50 124 to minecraft:overworld -34 62 118 replace force
execute if block -37 -50 -200 minecraft:beacon unless blocks -40 -60 -203 -34 -50 -197 -40 61 -203 all run clone from minecraft:overworld -40 -60 -203 -34 -50 -197 to minecraft:overworld -40 61 -203 replace force
execute if block -59 -50 164 minecraft:beacon unless blocks -62 -60 161 -56 -50 167 -62 61 161 all run clone from minecraft:overworld -62 -60 161 -56 -50 167 to minecraft:overworld -62 61 161 replace force
execute if block -64 -50 -26 minecraft:beacon unless blocks -67 -60 -29 -61 -50 -23 -67 64 -29 all run clone from minecraft:overworld -67 -60 -29 -61 -50 -23 to minecraft:overworld -67 64 -29 replace force
execute if block -64 -50 27 minecraft:beacon unless blocks -67 -60 24 -61 -50 30 -67 62 24 all run clone from minecraft:overworld -67 -60 24 -61 -50 30 to minecraft:overworld -67 62 24 replace force
execute if block -87 -50 -87 minecraft:beacon unless blocks -90 -60 -90 -84 -50 -84 -90 62 -90 all run clone from minecraft:overworld -90 -60 -90 -84 -50 -84 to minecraft:overworld -90 62 -90 replace force
execute if block -87 -50 88 minecraft:beacon unless blocks -90 -60 85 -84 -50 91 -90 63 85 all run clone from minecraft:overworld -90 -60 85 -84 -50 91 to minecraft:overworld -90 63 85 replace force
execute if block 0 -50 -150 minecraft:beacon unless blocks -3 -60 -153 3 -50 -147 -3 60 -153 all run clone from minecraft:overworld -3 -60 -153 3 -50 -147 to minecraft:overworld -3 60 -153 replace force
execute if block 0 -50 0 minecraft:beacon unless blocks -3 -60 -3 3 -50 3 -3 63 -3 all run clone from minecraft:overworld -3 -60 -3 3 -50 3 to minecraft:overworld -3 63 -3 replace force
execute if block 0 -50 150 minecraft:beacon unless blocks -3 -60 147 3 -50 153 -3 60 147 all run clone from minecraft:overworld -3 -60 147 3 -50 153 to minecraft:overworld -3 60 147 replace force
execute if block 112 -50 134 minecraft:beacon unless blocks 109 -60 131 115 -50 137 109 65 131 all run clone from minecraft:overworld 109 -60 131 115 -50 137 to minecraft:overworld 109 65 131 replace force
execute if block 121 -50 -31 minecraft:beacon unless blocks 118 -60 -34 124 -50 -28 118 63 -34 all run clone from minecraft:overworld 118 -60 -34 124 -50 -28 to minecraft:overworld 118 63 -34 replace force
execute if block 121 -50 32 minecraft:beacon unless blocks 118 -60 29 124 -50 35 118 63 29 all run clone from minecraft:overworld 118 -60 29 124 -50 35 to minecraft:overworld 118 63 29 replace force
execute if block 134 -50 -111 minecraft:beacon unless blocks 131 -60 -114 137 -50 -108 131 64 -114 all run clone from minecraft:overworld 131 -60 -114 137 -50 -108 to minecraft:overworld 131 64 -114 replace force
execute if block 144 -50 -145 minecraft:beacon unless blocks 141 -60 -148 147 -50 -142 141 63 -148 all run clone from minecraft:overworld 141 -60 -148 147 -50 -142 to minecraft:overworld 141 63 -148 replace force
execute if block 150 -50 0 minecraft:beacon unless blocks 147 -60 -3 153 -50 3 147 61 -3 all run clone from minecraft:overworld 147 -60 -3 153 -50 3 to minecraft:overworld 147 61 -3 replace force
execute if block 162 -50 125 minecraft:beacon unless blocks 159 -60 122 165 -50 128 159 62 122 all run clone from minecraft:overworld 159 -60 122 165 -50 128 to minecraft:overworld 159 62 122 replace force
execute if block 164 -50 60 minecraft:beacon unless blocks 161 -60 57 167 -50 63 161 65 57 all run clone from minecraft:overworld 161 -60 57 167 -50 63 to minecraft:overworld 161 65 57 replace force
execute if block 172 -50 -29 minecraft:beacon unless blocks 169 -60 -32 175 -50 -26 169 61 -32 all run clone from minecraft:overworld 169 -60 -32 175 -50 -26 to minecraft:overworld 169 61 -32 replace force
execute if block 195 -50 -61 minecraft:beacon unless blocks 192 -60 -64 198 -50 -58 192 60 -64 all run clone from minecraft:overworld 192 -60 -64 198 -50 -58 to minecraft:overworld 192 60 -64 replace force
execute if block 202 -50 36 minecraft:beacon unless blocks 199 -60 33 205 -50 39 199 61 33 all run clone from minecraft:overworld 199 -60 33 205 -50 39 to minecraft:overworld 199 61 33 replace force
execute if block 27 -50 -64 minecraft:beacon unless blocks 24 -60 -67 30 -50 -61 24 66 -67 all run clone from minecraft:overworld 24 -60 -67 30 -50 -61 to minecraft:overworld 24 66 -67 replace force
execute if block 27 -50 65 minecraft:beacon unless blocks 24 -60 62 30 -50 68 24 63 62 all run clone from minecraft:overworld 24 -60 62 30 -50 68 to minecraft:overworld 24 63 62 replace force
execute if block 30 -50 172 minecraft:beacon unless blocks 27 -60 169 33 -50 175 27 62 169 all run clone from minecraft:overworld 27 -60 169 33 -50 175 to minecraft:overworld 27 62 169 replace force
execute if block 32 -50 -120 minecraft:beacon unless blocks 29 -60 -123 35 -50 -117 29 62 -123 all run clone from minecraft:overworld 29 -60 -123 35 -50 -117 to minecraft:overworld 29 62 -123 replace force
execute if block 32 -50 121 minecraft:beacon unless blocks 29 -60 118 35 -50 124 29 61 118 all run clone from minecraft:overworld 29 -60 118 35 -50 124 to minecraft:overworld 29 61 118 replace force
execute if block 60 -50 -163 minecraft:beacon unless blocks 57 -60 -166 63 -50 -160 57 65 -166 all run clone from minecraft:overworld 57 -60 -166 63 -50 -160 to minecraft:overworld 57 65 -166 replace force
execute if block 60 -50 -195 minecraft:beacon unless blocks 57 -60 -198 63 -50 -192 57 63 -198 all run clone from minecraft:overworld 57 -60 -198 63 -50 -192 to minecraft:overworld 57 63 -198 replace force
execute if block 65 -50 -26 minecraft:beacon unless blocks 62 -60 -29 68 -50 -23 62 66 -29 all run clone from minecraft:overworld 62 -60 -29 68 -50 -23 to minecraft:overworld 62 66 -29 replace force
execute if block 65 -50 27 minecraft:beacon unless blocks 62 -60 24 68 -50 30 62 63 24 all run clone from minecraft:overworld 62 -60 24 68 -50 30 to minecraft:overworld 62 63 24 replace force
execute if block 85 -50 186 minecraft:beacon unless blocks 82 -60 183 88 -50 189 82 62 183 all run clone from minecraft:overworld 82 -60 183 88 -50 189 to minecraft:overworld 82 62 183 replace force
execute if block 88 -50 -87 minecraft:beacon unless blocks 85 -60 -90 91 -50 -84 85 62 -90 all run clone from minecraft:overworld 85 -60 -90 91 -50 -84 to minecraft:overworld 85 62 -90 replace force
execute if block 88 -50 88 minecraft:beacon unless blocks 85 -60 85 91 -50 91 85 62 85 all run clone from minecraft:overworld 85 -60 85 91 -50 91 to minecraft:overworld 85 62 85 replace force
