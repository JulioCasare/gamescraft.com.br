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

- **Pool pequeno e curado**: 4 a 6 kits, de couro a diamante, todos com chance
  real de vitória. Nada de kit-piada que faça o jogador se sentir sorteado para
  perder.
- **Quanto pior a armadura, mais forte o resto.** O kit de couro compensa com
  itens personalizados — lança com Lunge, poção de força, projéteis. O kit de
  diamante vem com arma simples e sem extras. A graça do sorteio é "o que eu
  faço com isso", não "ganhei ou perdi no sorteio".
- **Um kit centrado na lança.** A lança (1.21.9+) é o motivo de este backend
  rodar 26.2; o Lunge em arena é a vitrine disso.
- Kits ficam **fora da loja** — regra já registrada em
  [MONETIZACAO](MONETIZACAO.md). Vender kit mais forte é pay-to-win.

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
- Cada kit do pool contra cada outro: se algum kit não ganha de ninguém ou
  ganha de todos, ajuste os itens personalizados antes de abrir.
