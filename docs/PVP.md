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

## O sorteio

Não existem kits prontos: **cada peça de armadura é sorteada sozinha**, então
quase nunca sai um conjunto completo — o normal é capacete de diamante com
calça de couro, e é isso que faz cada vida parecer diferente da anterior.

O equilíbrio vem de um **orçamento**. Cada peça custa conforme o material —
couro 1, ouro 2, malha 3, ferro 4, diamante 5 — e a soma das quatro (de 4 a 20)
decide o resto do kit:

| Soma da armadura | Arma | Machado (quando sai) | Maçãs douradas |
|---|---|---|---|
| 4 a 8 (quase pelado) | Diamante com Sharpness 2 | Ferro | 5, mais poção de velocidade |
| 9 a 12 | Diamante simples | Pedra | 4 |
| 13 a 16 | Ferro | Pedra | 3 |
| 17 a 20 (blindado) | Pedra | Madeira | 2 |

**Todo kit sai também com:** 16 carnes assadas, 32 tábuas de madeira, 32
pedregulhos, e **escudo ou totem** — o totem em 1 de cada 4 kits, porque vale
bem mais que o escudo.

**Ferramentas extras** são sorteadas à parte da espada: 1 em 4 traz picareta,
1 em 4 traz machado, 1 em 4 traz os dois, 1 em 4 não traz nada. A picareta é
sempre de ferro (utilidade, dano baixo); o **machado entra um nível abaixo da
arma principal**, porque machado bate mais que espada do mesmo material e sem
isso o kit blindado ganharia dano de graça.

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
