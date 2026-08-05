# Eventos semanais

Evento aqui é o que **você conduz ao vivo**: a lava sobe, corrida de obstáculos, quiz, esconde-esconde. Não é abrir um dos minigames — é um servidor separado onde você monta a arena, controla o ritmo e decide o que acontece.

Por isso ele tem um servidor próprio, o `eventos`, que só sobe no dia.

## Por que um servidor separado

**O mapa é destruído.** Lava subindo, explosão, bloco quebrado — no fim do evento a arena virou ruína. Se isso acontecesse dentro do BedWars ou do lobby, você levaria dano permanente em um modo que precisa funcionar todo dia.

**As regras são outras.** O servidor de eventos roda em `survival` com PvP e voo liberado, porque o jogador precisa poder queimar, cair e morrer — e você precisa voar para montar e acompanhar de cima. Essas configurações estragariam qualquer minigame.

**Você fica em criativo.** Com op e voo, você mexe no mundo durante a partida. Isolar isso em um servidor que ninguém usa no dia a dia evita acidente no que está em produção.

## Preparar o mapa uma vez

Os mapas-modelo ficam em `infra/runtime/event-maps/`, uma pasta por evento:

```text
infra/runtime/event-maps/
├── lava-sobe/          <- pasta do mundo: level.dat, region/, ...
├── corrida/
└── esconde-esconde/
```

Monte a arena, feche o servidor, e copie a pasta `world` pronta para lá com o nome do evento. **Esse modelo nunca é tocado depois** — toda semana o evento roda sobre uma cópia dele.

Para conferir o que você tem:

```bash
./scripts/event.sh mapas
```

Esses mapas ficam fora do Git de propósito: são pesados e o `.gitignore` já bloqueia `infra/runtime/`. Inclua a pasta no backup.

## No dia do evento

**15 minutos antes**, com cópia limpa do mapa:

```bash
cd ~/gamescraft.com.br
./scripts/event.sh abrir --mapa lava-sobe --liberar bedwars --liberar pvp
```

O `--mapa` apaga o mundo do evento anterior e restaura o modelo do zero. O `--liberar` para modos do dia a dia para ceder RAM — em 8 GB isso não é opcional: subir o servidor de eventos sem liberar espaço faz o kernel matar um servidor com a sala cheia.

Como todo mundo vai estar no evento, liberar dois modos é normal. O lobby continua no ar, é por onde os jogadores chegam.

Espere aparecer `Done` no log antes de chamar o pessoal. Depois mande todos para o servidor `eventos` pelo lobby.

**No fim:**

```bash
./scripts/event.sh fechar --liberar bedwars --liberar pvp
```

Para o servidor de eventos e devolve os modos normais. Na semana seguinte você roda `abrir --mapa` de novo e o mapa volta intacto.

## Conduzir

O que decide se o evento funciona não é o mapa, é o ritmo.

**Prenda todo mundo até começar.** Jogador que chega e sai andando pela arena descobre o atalho, cai na lava antes da hora ou quebra o cenário. Congele no spawn e libere junto.

**Automatize a lava.** Subir a lava na mão, digitando comando com 40 pessoas esperando, é onde o evento trava. Um agendador de comandos executando o WorldEdit em intervalo fixo resolve — configure e teste antes, não durante.

**Decida o que fazer com o eliminado.** Ele precisa virar espectador ou voltar ao lobby. Eliminado vagando pela arena atrapalha quem ainda está jogando e some com o clima da disputa.

**Anuncie o tempo.** "Lava sobe em 30 segundos" muda completamente a tensão. Silêncio faz parecer que o evento travou.

## A semana

**Segunda ou terça — anuncie** o tipo de evento, o dia e a hora exata. Antecedência de 3 a 4 dias é o que faz alguém reservar o horário.

**Uma hora antes — lembre no Discord.** É o passo de maior impacto e o mais esquecido. Sem ele, metade não aparece.

**Depois — registre** na issue de evento do GitHub. Esse histórico é o que mostra qual formato funciona.

## O que medir

Três números, sempre os mesmos, para comparar semanas:

- **Pico de simultâneos durante o evento**, contra o pico de um dia normal.
- **Quantos ficaram até o fim.** Perder metade no meio é ritmo ruim ou bug.
- **Quantos voltaram no dia seguinte.** Este é o único que mede crescimento — evento que não traz ninguém de volta é diversão pontual.

Evento é também a forma mais barata de testar ideia de minigame antes de comprometer RAM com um modo permanente. Se um formato seu encher três semanas seguidas, ele merece virar servidor próprio.

## Monetização

Evento é bom momento para **revelar cosmético**, dar título de participação ou entregar recompensa de temporada. Não é momento para vender vantagem.

Um título de "Sobrevivente da Lava #3" vale porque foi conquistado. Vender o mesmo título esvazia os dois.

## Antes do primeiro evento

- [ ] Rodei o evento inteiro sozinho, do início ao fim, antes de convidar alguém.
- [ ] O mapa-modelo está em `event-maps/` e a cópia limpa foi testada.
- [ ] A lava (ou o mecanismo do evento) sobe sozinha, sem eu digitar comando.
- [ ] Eliminado vira espectador ou volta ao lobby.
- [ ] O anti-cheat tem exceção para a minha conta em criativo com voo.
- [ ] As regras estão escritas no Discord.
- [ ] O backup de ontem existe e está fora do servidor.

Evento com bug e sala cheia gasta a boa vontade dos testadores, que é o recurso mais escasso que você tem agora.
