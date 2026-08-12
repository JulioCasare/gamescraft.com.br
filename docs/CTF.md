# Capture the Flag

## O mapa

Uma ilha de mais ou menos 500x500 blocos no meio de um mar plano, construída à
mão pelo `scripts/ilha-ctf.sh`. O mapa não está guardado como arquivo pronto: o
script refaz ele igual a qualquer momento, inclusive no VPS.

O mundo se chama `ilha` (`LEVEL: "ilha"` no compose). Ele nasce só de mar —
bedrock, pedra até y=50 e água até y=62 — pelo gerador plano. A ilha é desenhada
por cima com o WorldEdit, rodando pelo console do servidor.

O bioma do mundo inteiro é planície, inclusive debaixo d'água. Isso é de
propósito: com bioma de oceano a grama sai puxando para o azul.

### Formato

O contorno da costa é um círculo de raio 250 com ±28 blocos de ruído Perlin por
cima, então ele não fecha redondo nem quadrado — em algumas direções a ilha
chega a x=270, em outras para antes de 240.

A altura do terreno é o nível do mar (62) mais colinas de até 14 blocos, também
de ruído. Perto da beira a subida é multiplicada por uma rampa ao quadrado, que
é o que faz a praia subir de leve em vez de virar um paredão. A faixa de areia é
uma troca final de grama e terra por areia em y=62 e 63.

### As torres

São 50 mini torres de tijolo de pedra, de 7x7 e uns 10 blocos de altura: sala
por dentro, porta ao norte, escada encostada na parede sul e ameia no teto. O
último degrau da escada ocupa o bloco do teto e vira o alçapão — sem ele não há
como chegar no sinalizador.

No meio do teto fica um sinalizador **aceso**, em cima de um 3x3 de bloco de
ferro; é esse quadrado que acende o feixe. O espaço acima de cada torre é limpo
até 30 blocos, senão uma colina cortaria o feixe.

A área de cada torre é marcada por uma linha de concreto cinza de 2 blocos de
largura, rente ao chão — o concreto entra no lugar do bloco de cima do terreno,
então não vira mureta e não atrapalha quem corre por cima.

A divisão é por proximidade: cada pedaço de chão pertence à torre mais perto
dele, e a linha passa onde duas torres empatam. É isso que faz as áreas
encostarem umas nas outras sem sobra de terreno no meio, que era o defeito da
cerca quadrada de raio fixo que existia antes.

As posições são o centro, as quatro primeiras torres e quatro anéis (8 em raio
70, 12 em 125, 12 em 175 e 13 em 205). Anel em vez de grade porque a ilha é
redonda — grade deixaria canto vazio e beirada cheia. O anel de fora para em
205: a costa chega a encolher até 222, e a cerca ainda precisa dos 12 de folga.

O nascimento fica em `12 72 0`, logo ao lado da torre do centro.

## Refazer o mapa

O terreno primeiro, as torres depois:

```bash
bash scripts/ilha-ctf.sh
```

```bash
bash scripts/torres-ctf.sh
```

```bash
bash scripts/fronteiras-ctf.sh
```

O terreno demora uns 8 minutos. Roda em pedaços de 100x100 porque um único
`//gen` do tamanho da ilha são 10 milhões de blocos de uma vez, e o histórico do
WorldEdit estoura a memória do servidor.

As torres não medem o terreno em jogo: o próprio WorldEdit reavalia a mesma
fórmula de ruído que desenhou a ilha, só que nas coordenadas do centro de cada
torre. É o que permite carimbar 50 delas sem sondar 50 terrenos, e é também por
isso que os dois scripts precisam usar a mesma fórmula — se um mudar, o outro
muda junto, senão as torres passam a flutuar ou a nascer enterradas.

O `torres-ctf.sh` pode rodar em cima de si mesmo quantas vezes quiser: cada
torre é refeita do zero no lugar.

## O único mundo do CTF

O mapa `Island` que veio pronto e o mundo que o servidor tinha gerado sozinho
foram apagados a pedido, em 2026-08-11. O volume `gc_ctf` tem só a `ilha`.

Se um dia o mapa se perder, não há backup dentro do servidor: quem refaz é o
`scripts/ilha-ctf.sh`, e é por isso que ele existe.
