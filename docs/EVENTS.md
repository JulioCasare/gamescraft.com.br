# Eventos semanais

Um evento semanal resolve o problema central de servidor novo: você quer oferecer variedade, mas dividir 20 jogadores em 6 modos deixa todos vazios. O evento junta a população inteira em um modo, num horário marcado, sem fragmentar o dia a dia.

Também é a forma mais barata de descobrir qual modo merece virar permanente — em vez de adivinhar, você mede.

## Por que não deixar o modo sempre ligado

Um modo permanente precisa de população constante para as partidas encherem. [O ROADMAP](ROADMAP.md) traz os números: BedWars precisa de 16–20 simultâneos *no modo* para funcionar, CTF idem. Enquanto você não tiver isso, o modo aberto 24h só mostra uma fila que nunca completa — e jogador que espera cinco minutos desconecta.

No evento acontece o oposto: todo mundo chega junto, a partida enche na hora e o modo aparenta ser popular. É a mesma quantidade de gente, distribuída melhor.

## A semana

**Segunda ou terça — anuncie.** Diga o modo, o dia e a hora exata. Antecedência de 3 a 4 dias é o que faz alguém reservar o horário.

**Uma hora antes — lembre no Discord.** Sem isso metade esquece. É o passo com maior impacto e o que mais se esquece de fazer.

**15 minutos antes — abra o modo:**

```bash
cd ~/gamescraft.com.br
./scripts/event.sh abrir buildbattle --liberar pvp
```

O `--liberar` para um modo do dia a dia para dar RAM ao evento. Em 8 GB isso não é opcional: sem liberar espaço, o sistema mata um servidor no meio da partida. Acompanhe o boot até aparecer `Done` antes de chamar o pessoal.

**Durante — fique no servidor.** Você está ali para ver o que quebra, não para jogar. Anote tudo: erro, confusão de regra, jogador Bedrock que não conseguiu usar um menu.

**No fim — feche e devolva a RAM:**

```bash
./scripts/event.sh fechar buildbattle --liberar pvp
```

O mundo e as estatísticas continuam salvos em `infra/runtime/buildbattle`. Na semana seguinte tudo volta como estava.

**Depois — registre.** Abra a issue de evento no GitHub com o que aconteceu. Esse registro é o que vai decidir o 4º modo permanente.

## O que medir

Três números, sempre os mesmos, para poder comparar semanas:

- **Pico de simultâneos durante o evento**, comparado com o pico de um dia normal.
- **Quantos ficaram até o fim** — evento que perde metade no meio tem problema de ritmo ou de bug.
- **Quantos voltaram no dia seguinte.** Este é o que importa de verdade: evento que não traz ninguém de volta é entretenimento pontual, não crescimento.

Um modo que enche fácil e segura o pessoal por três semanas seguidas ganhou o direito de virar permanente — e aí vale a troca para 16 GB.

## Escolhendo o modo da semana

Alterne. Repetir o mesmo modo toda semana desgasta rápido, e o objetivo é justamente comparar.

| Modo | Bom para | Cuidado |
|---|---|---|
| Build Battle | Público misto, criativo, funciona com pouca gente | Menus e seletor de blocos podem falhar no Bedrock |
| Capture the Flag | Grupo grande, times organizados | Precisa de 16+ para não ficar vazio |
| Pillars of Fortune | Sessões curtas, boa rotatividade | Valide a jogabilidade no Bedrock antes |
| PvP | Qualquer número, sempre funciona | Melhor como modo fixo que como evento |

Se o número de confirmados no Discord estiver baixo, troque para Build Battle: é o que menos depende de população.

## Monetização

Evento é bom momento para **revelar cosmético**, dar título de participação ou entregar recompensa de temporada. Não é momento para vender vantagem.

A regra do [ROADMAP](ROADMAP.md) continua valendo: nada que altere a competição. Um título de "Campeão do Build Battle #3" tem valor porque foi conquistado — vender o mesmo título esvazia os dois.

## Antes do primeiro evento

- [ ] O modo foi testado do começo ao fim, por você, antes de convidar alguém.
- [ ] Um jogador Bedrock e um Java entraram e jogaram uma partida completa.
- [ ] As regras do modo estão escritas no Discord.
- [ ] Você sabe como reiniciar só esse modo sem derrubar a rede.
- [ ] O backup do dia anterior existe e foi copiado para fora do servidor.

Evento com bug em modo nunca testado gasta a boa vontade dos testadores — que é o recurso mais escasso que você tem agora.
