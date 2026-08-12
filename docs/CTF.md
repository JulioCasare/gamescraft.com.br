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

Cinco mini torres de tijolo de pedra, de 7x7 e uns 10 blocos de altura, com sala
por dentro, porta ao norte e ameia no teto:

| torre  | posição     | chão |
|--------|-------------|------|
| centro | 0, 0        | 72   |
| leste  | 150, 0      | 70   |
| oeste  | -150, 0     | 72   |
| sul    | 0, 150      | 69   |
| norte  | 0, -150     | 69   |

No meio do teto de cada uma fica um sinalizador **aceso**, em cima de um 3x3 de
bloco de ferro — é esse quadrado que acende o feixe. O espaço acima da torre é
limpo até 30 blocos, senão uma colina cortaria o feixe.

O nascimento fica em `12 72 0`, logo ao lado da torre do centro.

## Refazer o mapa

```bash
bash scripts/ilha-ctf.sh
```

Demora uns 8 minutos. Roda em pedaços de 100x100 porque um único `//gen` do
tamanho da ilha são 10 milhões de blocos de uma vez, e o histórico do WorldEdit
estoura a memória do servidor.

Se o terreno mudar, as alturas das torres no fim do script precisam ser medidas
de novo — o comando está anotado lá.

## O que ficou guardado

O mapa `Island` que veio pronto continua no mundo `world`, e o mundo que o
servidor tinha gerado sozinho está em `world_gerado_antigo`. Para voltar a
qualquer um deles, é só trocar `LEVEL` no `compose.yaml`.
