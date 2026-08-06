# Checklist para beta fechado

## Quem entra: whitelist, não limite de jogadores

O `MAX_PLAYERS` de cada servidor é rede de proteção, não controle de acesso. Com 50 convidados você terá de 10 a 20 simultâneos nos melhores horários — o teto de 50 nunca vai ser atingido.

Quem fecha o beta é a **whitelist**, ligada por `BETA_WHITELIST=TRUE` em `infra/.env`. Sem ela, qualquer um que descubra o IP entra.

Para liberar um testador, em cada servidor que ele vai usar:

```bash
docker compose --env-file infra/.env -f infra/compose.yaml exec lobby rcon-cli whitelist add Fulano
```

Sem RCON habilitado, edite `infra/runtime/<servidor>/whitelist.json` e recarregue.

**Jogador Bedrock entra com prefixo.** O Floodgate acrescenta um caractere ao nome (por padrão `.`), então o nome na whitelist precisa desse prefixo. Descubra o nome exato deixando a pessoa tentar entrar uma vez e lendo o log:

```bash
docker compose --env-file infra/.env -f infra/compose.yaml logs lobby | grep -i "not white-listed"
```

Convide em ondas — 10, depois 25, depois 50 — usando a whitelist como controle. Assim um bug atinge 10 pessoas, não 50.

## Onde arrumar testadores

Cinco bastam para começar. Insistir em juntar 50 antes de abrir é o que trava servidor novo.

**Escolha o modo pelo número de gente presente**, não pelo seu preferido:

| Presentes | O que dá para testar |
|---|---|
| 2 a 4 | PvP — arena funciona com dois |
| 5 a 8 | Build Battle, BedWars solo ou duplas |
| 12+ | BedWars 4×4, Capture the Flag |

BedWars 4×4 com cinco pessoas nunca começa, e você perde a noite achando que quebrou algo.

**Onde encontrar — só público brasileiro.** Testador fora do Brasil pega 150 a 200 ms até São Paulo: ele vai achar o PvP horrível por causa da distância, não do seu servidor, e o relato dele não serve para nada.

- Comunidades de Minecraft BR no Discord, nos canais de divulgação — leia as regras antes, a maioria bane quem posta fora do lugar.
- TikTok e YouTube Shorts, com clipe curto de partida real. É como servidor BR cresce hoje.
- Grupos de WhatsApp e Telegram de Minecraft.
- Escola, trabalho, amigos de amigos — cada um traz mais um.

Para **ajuda técnica** (não para recrutar), `r/admincraft` no Reddit reúne donos de servidor e é bom lugar para perguntas como compatibilidade de plugin com Paper e Geyser. É em inglês, e a maioria das comunidades técnicas proíbe divulgar servidor — leia as regras antes de postar.

**Ofereça um título cosmético de Fundador.** Custo zero, permitido pelas regras da Mojang, e é a única coisa que não dá para conseguir depois. É o que faz alguém aguentar o servidor caindo enquanto você aprende.

**Use formulário, não convite manual.** Nick, edição e horário que costuma jogar, no Discord ou Google Forms. Você aprova no seu ritmo e controla o fluxo.

E não abra a whitelist como atalho por falta de gente: quem entra num servidor cru não volta, e ainda comenta.

## Infraestrutura

- [ ] Docker e todos os oito serviços iniciam sem erro: proxy, lobby, bedwars, pillars, buildbattle, ctf, pvp e database.
- [ ] Docker tem RAM suficiente para os seis servidores Paper mais o proxy.
- [ ] Apenas 25565/TCP e 19132/UDP estão públicos.
- [ ] Backends e MariaDB não respondem diretamente fora da rede Docker.
- [ ] Backup de `infra/runtime/` foi criado e restaurado em outro local.
- [ ] Monitoramento de CPU, RAM, TPS/MSPT e disco está ativo.

## Jogabilidade

- [ ] Jogador Java entra no lobby e muda de modo.
- [ ] Jogador Bedrock entra no lobby e muda de modo.
- [ ] Nomes, skins, UUIDs e punições funcionam para ambos.
- [ ] Menus, chat, placares, loja e itens funcionam no Bedrock.
- [ ] BedWars tem ao menos uma arena testada do começo ao fim.
- [ ] Pillars of Fortune tem ao menos uma arena testada do começo ao fim.
- [ ] Build Battle tem ao menos um mapa testado: temas, votação, tempo e salvamento da construção.
- [ ] Capture the Flag tem ao menos um mapa testado: captura, retorno da bandeira e placar.
- [ ] PvP tem ao menos uma arena testada, com kits idênticos para todos.
- [ ] Voar dentro da parcela de Build Battle não gera expulsão nem falso positivo do anti-cheat.
- [ ] Sair de uma partida pelo lobby devolve o jogador ao estado correto em todos os modos.
- [ ] Todos os mapas possuem licença/autorização de uso.

## Segurança e comunidade

- [ ] Regra contra contas alternativas, trapaça, assédio e evasão de punição.
- [ ] Equipe de moderação definida e treinada para registrar ocorrências.
- [ ] Sistema de denúncia no Discord.
- [ ] Anti-cheat validado com jogadores Bedrock para reduzir falsos positivos.
- [ ] Canal de bugs no Discord e template de issue no GitHub.
- [ ] Aviso de não afiliação ao Minecraft no site e loja.

## Teste de carga

- [ ] 10 jogadores simultâneos: sem erros críticos.
- [ ] 25 jogadores simultâneos: TPS e MSPT anotados.
- [ ] 50 jogadores simultâneos: gargalos registrados e corrigidos.
- [ ] Reinício controlado de cada minigame sem derrubar o proxy.

## Como saber que o beta acabou

Beta não termina por data nem por número de jogadores. Termina quando **o servidor funciona sozinho** — quando você consegue ser jogador em vez de plantão.

O sinal mais confiável é este: **você passou três noites cheias seguidas sem precisar intervir.** Sem reiniciar nada, sem corrigir configuração, sem pedir desculpa a ninguém. Se ainda está apagando incêndio, ainda é beta, mesmo que a checklist esteja toda marcada.

Os critérios objetivos:

- [ ] **Duas semanas sem queda não planejada** de qualquer servidor.
- [ ] **Nenhum bug grave aberto**: partida que não termina, item perdido, jogador preso, jogador que não consegue entrar.
- [ ] A curva de bugs novos por sessão **parou de cair** — os relatos viraram sugestão de melhoria em vez de defeito.
- [ ] Um jogador **Bedrock** e um **Java** completam o ciclo inteiro sem ajuda: entrar, jogar uma partida do início ao fim, voltar ao lobby, trocar de modo.
- [ ] Testadores **voltam sem serem chamados**. Se só aparece gente quando você avisa no Discord, o servidor ainda não segura ninguém.
- [ ] Três eventos seguidos rodaram do início ao fim sem intervenção.
- [ ] Backup restaurado e verificado com dados reais.
- [ ] Moderação, regras e canal de bug funcionando com gente de verdade usando.

### O que "sair do beta" significa na prática

É trocar uma linha em `infra/.env`:

```ini
BETA_WHITELIST=FALSE
```

E reiniciar a rede. A partir daí qualquer pessoa entra.

Antes de fazer isso, complete a **Fase 9** do [passo a passo](PASSO-A-PASSO.md). Dois itens não são opcionais: **proteção DDoS** — servidor público de PvP é alvo, e o primeiro banido com raiva testa isso — e as **obrigações legais** da [monetização](MONETIZACAO.md), se você já tiver loja.

Abrir ao público é reversível: basta voltar a variável para `TRUE`. Mas a primeira impressão de quem entrou e achou ruim não é.
