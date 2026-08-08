# Plugins e mapas

Os arquivos `*.txt` desta pasta são listas de URLs que o container baixa no primeiro boot. Eles evitam guardar JARs no Git, mas uma URL ainda é uma decisão de segurança: só inclua projetos revisados.

## Regras de escolha

1. Baixe somente de fonte oficial do projeto, Hangar, Modrinth, SpigotMC ou marketplace autorizado.
2. Registre versão, licença, URL de origem e data de teste no issue correspondente.
3. Teste em uma cópia do ambiente antes de colocar em produção.
4. Não use JARs "crackeados", enviados por desconhecidos ou sem licença clara.
5. Não publique arquivos de mapas que você não tenha direito de redistribuir.

## Decisões tomadas

- **Versões por modo.** Os backends não precisam estar todos na mesma versão: o
  Velocity roteia entre eles e o ViaVersion traduz cliente novo para servidor
  velho. O BedWars roda Paper **1.20.4** (versão máxima do plugin escolhido) com
  ViaVersion instalado; os demais modos rodam **26.2**, para a lança (1.21.9+)
  existir no PvP e nos eventos. Dentro do BedWars não há lança — o item não
  existe na 1.20.4.
- **BedWars: BedWars1058 25.2 estável, em backend Paper 1.20.4.** A 25.3-SNAPSHOT
  em 1.20.4 foi testada e **reprovada** (2026-08-08): loja abria vazia, sem
  nenhum item à venda, mais NPE em `PlayerDeathEvent` e no menu de espectador.
  Snapshot não passa nos critérios abaixo. Planos B na mesma família, se a 25.2
  também falhar: fork BedWars2023 (tomkeuper) e fork do MelonOof no Modrinth
  (este lista 1.21.11 — candidato se um dia quisermos lança no BedWars).
- **PvP: KitPvP de kit aleatório**, sem menu de seleção. O desenho completo,
  incluindo zona segura, combat tag de 15 s e o balanceamento do sorteio, está
  em [docs/PVP.md](../../docs/PVP.md).

## Decisões ainda pendentes

- Pillars of Fortune: validar a experiência Bedrock e possíveis dados de telemetria.
- Build Battle: conferir se os menus de tema, o seletor de blocos e a votação funcionam pelo Geyser.
- Capture the Flag: confirmar indicação de bandeira, cores de time e placar no Bedrock.
- PvP: escolher o plugin (ou combinação) que implementa o desenho de docs/PVP.md.
- Anti-cheat: escolher um que lide bem com as diferenças de movimento do Bedrock e com o voo criativo do Build Battle.
- Permissões, punições, chat e estatísticas: escolher uma combinação compatível entre si.

Prefira, quando possível, um único plugin que cubra vários modos: menos superfície para conflitos, uma licença só e um lugar só para atualizar. Se cada modo vier de um autor diferente, teste a combinação inteira antes de abrir o beta.

## Critérios para o plugin de minigame

Além de licença e suporte à versão do Paper do modo em questão (26.2 nos modos gerais, 1.20.4 no BedWars), quatro coisas decidem se ele serve para um servidor pequeno:

**1. Formatos de partida configuráveis.** Plugin que só faz 4×4 exige 16 jogadores para começar. Com 12 online a partida nunca sai, e quem esperou fecha o jogo. Você precisa poder rodar solo e duplas enquanto a população é pequena, e aumentar o formato conforme cresce.

**2. Teto de partidas simultâneas.** Cada partida carrega uma cópia do mapa — BedWars destrói o cenário, então não dá para reaproveitar. Plugin que cria arena sob demanda sem limite estoura a memória num pico. Precisa existir um número máximo configurável.

**3. Descarregar o mundo ao fim da partida.** É a falha mais comum e a mais difícil de ver: o consumo sobe a cada partida e nunca volta, derrubando o servidor depois de algumas horas. Um teste de 10 minutos não revela isso.

**4. Menus utilizáveis no Bedrock.** Loja, seleção de time e placar passam pelo Geyser, que renderiza inventário e formulário de forma diferente. Teste com um aparelho Bedrock de verdade, não só no Java.

**5. Mapa e geradores configuráveis.** É o que permite criar modo próprio sem programar. Um híbrido de Pillars com cama, por exemplo, é mapa de pilares mais geradores soltando blocos aleatórios — se o plugin deixar importar mapa e editar o conteúdo dos geradores, isso é configuração, não desenvolvimento. Plugin que só aceita os mapas dele te prende ao BedWars igual ao de todo mundo.

### Como testar o item 3

Rode 5 ou 6 partidas seguidas acompanhando a memória:

```bash
docker stats gamecraft-bedwars-1
```

Se o consumo sobe a cada partida e não retorna ao patamar anterior, o plugin está vazando mundos. Descarte-o: isso não tem contorno pela configuração.

O arquivo [BETA-CHECKLIST](../../docs/BETA-CHECKLIST.md) define os testes mínimos antes de abrir o acesso.
