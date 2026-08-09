# PvP — desenho do modo

O PvP é o modo âncora da rede: precisa ser jogável com 2 pessoas online. Todo o
desenho abaixo existe para reduzir o atrito entre "entrei" e "estou lutando".

## O ciclo

1. Todo mundo nasce no mesmo lugar, dentro de uma **zona segura**, já equipado
   com um **kit sorteado**.
2. Saiu da zona segura, pode bater e apanhar.
3. Voltar para a zona segura só depois de **15 segundos sem combate** — sem
   bater e sem apanhar. Morreu, volta ao spawn com um kit novo sorteado, e o
   ciclo recomeça sozinho.

Não há menu de seleção: o sorteio equipa o jogador no spawn. Isso elimina uma
tela por respawn e evita depender de menus pelo Geyser (critério 4 da
[escolha de plugins](../infra/plugins/README.md)).

## A zona segura

A área é marcada em jogo — vá até um canto e rode `/function gckits:canto1`,
depois ao canto oposto (mais alto também, para cobrir a altura) e
`/function gckits:canto2`. Fica salva no mundo e sobrevive a reinício;
`/function gckits:zona_ver` mostra a atual.

Dentro dela, quem está em paz:

- **fica imune** a qualquer dano, inclusive de quem atira de fora;
- **não quebra nem coloca bloco**, porque o servidor troca o modo para aventura
  na entrada e devolve sobrevivência na saída;
- **não mexe em molduras nem quadros** — os que estão na área ficam fixos e
  invulneráveis;
- **não machuca ninguém**: o dano de ataque é zerado por um modificador de
  atributo enquanto estiver dentro, valendo tanto para alvo dentro quanto fora.

Quem está **em combate** não entra: ao encostar na área, é devolvido à última
posição que ocupava fora dela. É a mesma regra que resolve o ataque de dentro —
o golpe marca combate, e no tick seguinte o atacante é expulso.

> O servidor de PvP roda em **survival**, não adventure: o kit tem blocos,
> baldes, TNT e teia, e nada disso pode ser colocado em adventure. O modo
> aventura existe só dentro da zona segura, aplicado e removido pelo datapack.

## O sorteio

Não existem kits prontos: **cada peça de armadura é sorteada sozinha**, então
quase nunca sai um conjunto completo — o normal é capacete de diamante com
calça de couro, e é isso que faz cada vida parecer diferente da anterior.

O equilíbrio vem de um **orçamento**. Cada peça custa conforme o material —
couro 1, ouro 2, malha 3, ferro 4, diamante 5 — e a soma das quatro (de 4 a 20)
decide o resto do kit:

| Soma da armadura | Espada e lança | Machado (quando sai) | Maçãs, se saírem |
|---|---|---|---|
| 4 a 8 (quase pelado) | Ferro ou diamante, lança com Lunge | Pedra ou ferro | 5, mais poção garantida |
| 9 a 12 | Pedra a diamante, lança com Lunge | Madeira a ferro | 4 |
| 13 a 16 | Pedra ou ferro | Madeira ou pedra | 3 |
| 17 a 20 (blindado) | Madeira ou pedra | Madeira | 2 |

O material de cada arma é sorteado **dentro da faixa**, então dois kits com a
mesma armadura ainda saem diferentes.

**Todo kit sai também com:** espada e lança (sempre as duas), 32 tábuas, 32
pedregulhos, e **escudo ou totem** — o totem em 1 de cada 8 kits, porque é uma
vida extra e vale muito mais que o escudo.

**Encantamentos: cada um é sorteado por conta própria**, e o nível também — de
I até o máximo daquele encantamento. Às vezes vem um, às vezes o outro, às
vezes os dois, às vezes nenhum:

| Item | Encantamentos possíveis |
|---|---|
| Espada | Afiação (1 em 2), Inquebrável (1 em 2) |
| Lança | Estocada (1 em 2), Afiação (1 em 3), Inquebrável (1 em 2) |
| Cada peça de armadura | Proteção (1 em 2), Inquebrável (1 em 3) |
| Arco | Força (1 em 2), Inquebrável (1 em 2) |
| Machado | Eficiência (1 em 2), Afiação (1 em 3), Inquebrável (1 em 2) |
| Picareta | Eficiência (1 em 2), Inquebrável (1 em 2) |
| Tridente | Lealdade (2 em 3), Inquebrável (1 em 2) |

**Projétil de vento:** 16 em todo kit — empurra quem chega perto e ajuda a
subir. É a ferramenta de reposicionamento que todos têm.

**Tridente com Lealdade em 1 de cada 3 kits:** arma de arremesso que volta
sozinha para a mão.

**Pérolas do ender: ou quatro, ou nenhuma.** Um kit em cada quatro leva o
punhado inteiro — o suficiente para fugir de verdade e ainda caçar alguém.

**Baldes:** água em 1 de cada 3 kits (salva de queda, apaga fogo, empurra quem
está na beirada) e lava em 1 de cada 6 — a lava é a ferramenta mais cruel do
modo, por isso sai bem mais rara.

**Teias:** 8 unidades em 1 de cada 3 kits. Prendem o perseguidor e cortam
queda; como todo kit tem espada, quem cai na teia consegue se soltar — muda o
ritmo da luta sem travá-la.

**TNT que acende sozinha:** 4 unidades em 1 de cada 5 kits. Não precisa de
isqueiro — colocou, conta dois segundos e explode. O truque é uma conquista
invisível que dispara a cada bloco colocado e um raio de seis blocos na direção
do olhar: se o bloco recém-posto for TNT, ele vira TNT acesa na hora.

> A TNT **abre cratera no mapa**. Enquanto a arena de PvP for terreno gerado,
> isso não importa; quando houver arena construída, ou se aceita as marcas de
> guerra, ou se agenda um reset periódico da área com o WorldEdit.

**Arco e flecha são sorteados separados, de propósito:** 1 chance em 3 para o
arco, outra 1 em 3 para 16 flechas. Pode vir arco sem flecha e flecha sem arco
— e como quem morre larga o que tinha, juntar as duas metades no chão vira uma
jogada.

**Poções de agilidade ou força, para beber ou arremessar.** Quem sorteou
armadura fraca (4 a 8) leva sempre uma; os outros kits têm 1 chance em 3. O
tipo é sorteado entre as quatro combinações — a arremessável vale tanto quanto
a bebível, porque pega quem estiver em volta.

**Comida: ou maçã dourada, ou carne, nunca as duas.** No sorteio, metade dos
kits vem com 16 carnes assadas e metade com maçãs douradas — de 5 (armadura de
couro) a 2 (armadura de diamante). Maçã cura na hora e vale mais que carne, por
isso a quantidade cai conforme a armadura sobe.

**Ferramentas extras** são sorteadas à parte: 1 em 4 traz picareta, 1 em 4 traz
machado, 1 em 4 traz os dois, 1 em 4 vem só com as armas. A picareta tem
material livre (é utilidade, dano baixo em qualquer material); o **machado
entra um nível abaixo da faixa das armas**, porque machado bate mais que espada
do mesmo material e sem isso o kit blindado ganharia dano de graça.

Assim ninguém é sorteado para perder: quem recebeu armadura ruim ganha o poder
de matar rápido e fugir; quem recebeu diamante inteiro aguenta muito e machuca
pouco. A graça é "o que eu faço com isso", não "ganhei ou perdi no sorteio".

**Uma em cada quatro armas vem como lança**, no lugar da espada, com Lunge
proporcional ao nível do kit. A lança (1.21.9+) é o motivo de este backend
rodar 26.2, e o Lunge em arena é a vitrine disso.

Kits ficam **fora da loja** — regra já registrada em
[MONETIZACAO](MONETIZACAO.md). Vender kit mais forte é pay-to-win.

### Onde mexer

O sistema é um datapack em `infra/datapacks/gamecraft-kits`, não um plugin —
nenhum plugin de KitPvP suporta o Paper 26.2. Para ajustar o equilíbrio:
`slot_*.mcfunction` muda os materiais sorteados, `arma.mcfunction` muda as
faixas de orçamento, e os `arma_*.mcfunction` mudam armas e extras de cada
faixa. Depois de editar, copie para `infra/runtime/pvp/world/datapacks/` e rode
`/reload` no servidor.

## As três regras que fecham as brechas

Todo KitPvP com zona segura tem as mesmas três brechas conhecidas. As regras
abaixo não são opcionais — sem qualquer uma delas o modo quebra na primeira
noite de teste:

1. **Combat tag de 15 segundos.** Bateu ou apanhou, não entra na zona segura
   até ficar 15 s sem combate. Sem isso, a luta vira pega-pega na borda.
2. **Deslogar marcado = morte.** Quem desconecta dentro dos 15 s morre, e quem
   marcou leva o crédito. Sem isso, ninguém morre: é só deslogar antes do golpe
   final.
3. **Atacar de dentro da zona segura expulsa da proteção na hora.** Sem isso, a
   borda do spawn vira trincheira de covarde.

## Solo, não times

Arena livre, todos contra todos. Com 2 a 12 jogadores online não há população
para times, e o modo precisa funcionar no horário vazio. Formato de equipe é
assunto para depois do beta, se houver demanda.

## O que testar no beta

- Os três fechos de brecha acima, um por um, tentando burlar de propósito.
- A lança e o Lunge num aparelho Bedrock real: golpe carregado e mira por toque
  passando pelo Geyser.
- O equilíbrio do orçamento: jogue várias vidas seguidas e repare se alguma
  faixa domina. Se quem sai quase pelado ganha sempre, a arma da faixa 4–8 está
  forte demais; se blindado nunca morre, falta dano na faixa 17–20.
