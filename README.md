# Game Craft

Base de infraestrutura do servidor **Game Craft**: uma rede de minigames para jogadores Java e Bedrock.

> Este servidor não é um produto oficial do Minecraft, Mojang Studios ou Microsoft.

## Beta inicial

- Capacidade planejada: até 50 jogadores simultâneos.
- Edição Java: Paper, com a versão presa por modo à compatibilidade dos plugins — 26.2 nos modos gerais (para a lança existir no PvP e nos eventos) e 1.20.4 no BedWars (versão máxima do BedWars1058 25.2, com ViaVersion para os clientes novos entrarem).
- Edição Bedrock: Geyser + Floodgate no proxy Velocity.
- Minigames do primeiro marco: BedWars, Pillars of Fortune, Build Battle, Capture the Flag e PvP, mais o Lobby.
- Banco de dados: MariaDB, para permissões, estatísticas, moedas e punições quando os plugins forem escolhidos.

## Estrutura

```text
Java + Bedrock
      |
Velocity + Geyser + Floodgate
      |
 ┌────┬────────┬─────────┬──────────────┬──────┬──────┐
Lobby  BedWars  Pillars   Build Battle   CTF    PvP
      |
   MariaDB
```

Cada modo roda no seu próprio servidor Paper: um erro no BedWars não derruba o resto da rede. Os servidores internos não publicam portas para a internet; somente o proxy expõe Java (TCP) e Bedrock (UDP).

## Começar localmente

Você precisa de Docker Desktop com Docker Compose. Não exponha o computador doméstico à internet neste momento: use isto primeiro para testes na sua rede.

1. No PowerShell, dentro desta pasta, execute:

   ```powershell
   .\scripts\prepare-beta.ps1 -PublicHost "localhost"
   docker compose --env-file infra/.env -f infra/compose.yaml up -d
   ```

2. Veja a inicialização:

   ```powershell
   docker compose --env-file infra/.env -f infra/compose.yaml logs -f
   ```

3. Conecte no Java em `localhost:25565`. Para Bedrock na mesma máquina, use `127.0.0.1` e porta `19132`.

4. Para parar sem apagar mundos ou banco:

   ```powershell
   docker compose --env-file infra/.env -f infra/compose.yaml down
   ```

O primeiro boot cria arquivos de servidor em `infra/runtime/`; eles são intencionalmente ignorados pelo Git. Não faça commit de mundos, mapas, JARs ou segredos.

## Memória

O heap não é o consumo total: cada JVM usa de 25% a 40% a mais fora dele, em metaspace, GC e buffers de rede. Some ~500 MB de MariaDB e ~700 MB de sistema. Encostar o heap no limite da máquina faz o kernel matar um servidor no meio da partida.

### Perfis prontos

**8 GB de RAM — dois modos.** O limite é memória:

```ini
MEMORY_PROXY=512M
MEMORY_LOBBY=1G
MEMORY_BEDWARS=2G
MEMORY_PVP=1G
```

```bash
docker compose --env-file infra/.env -f infra/compose.yaml up -d proxy bedwars pvp
```

Nesse perfil o evento precisa ceder espaço: `./scripts/event.sh abrir --mapa NOME --liberar bedwars`.

**16 GB com 4 vCPU — dois modos.** É o perfil da operação atual: BedWars fixo mais um slot rotativo. O limite passa a ser CPU — proxy, lobby, BedWars e o rotativo são quatro threads principais em quatro núcleos, e um terceiro modo passaria a disputar:

```ini
MEMORY_PROXY=1G
MEMORY_LOBBY=1500M
MEMORY_BEDWARS=3G
MEMORY_PVP=1500M
```

Aqui o servidor de eventos sobe sem liberar nada — durante o evento os minigames ficam vazios e suas threads ociam.

Para trocar qual modo ocupa o slot rotativo, use `./scripts/rotate.sh <modo>`. Detalhes em [operação](docs/OPERATIONS.md).

**Os seis modos abertos** pedem ~11,5 GB de heap com os valores padrão de `infra/.env`, ou seja 16 GB de RAM e pelo menos 6 vCPU. Antes de chegar lá, leia a parte de população no [ROADMAP](docs/ROADMAP.md): modo aberto sem gente para encher partida só mostra fila vazia.

No Windows, o Docker Desktop usa o WSL 2 e por padrão pega apenas metade da RAM da máquina. Para fixar o limite, crie `C:\Users\<seu-usuario>\.wslconfig`:

```ini
[wsl2]
memory=16GB
processors=12
```

Depois execute `wsl --shutdown` e reabra o Docker Desktop.

Nomear os modos no `up -d` sobe só o que você pediu: o lobby e o banco entram junto como dependência do proxy.

Aumente um `MEMORY_*` somente depois que o `spark` mostrar falta real de memória. Heap grande demais não é de graça: deixa as pausas de coleta de lixo mais longas, o que aparece como travadinha durante a partida.

## Antes de convidar testadores

Leia e conclua [a checklist do beta](docs/BETA-CHECKLIST.md). Em especial, escolha plugins compatíveis, instale mapas licenciados, configure moderação e teste Java e Bedrock.

- [**Passo a passo, da compra ao beta**](docs/PASSO-A-PASSO.md) — comece por aqui
- [Instalar no servidor](docs/DEPLOY.md)
- [PvP: desenho do modo](docs/PVP.md)
- [Eventos semanais](docs/EVENTS.md)
- [Monetização](docs/MONETIZACAO.md)
- [Arquitetura](docs/ARCHITECTURE.md)
- [Operação e segurança](docs/OPERATIONS.md)
- [Escolha de plugins e mapas](infra/plugins/README.md)
- [Roadmap](docs/ROADMAP.md)

## Sair da máquina local

Para colocar a rede em um VPS, siga [docs/DEPLOY.md](docs/DEPLOY.md): usuário sem root, chave SSH, firewall, Docker e backup, na ordem certa.

No servidor Linux use `scripts/prepare-beta.sh` em vez do `.ps1`:

```bash
./scripts/prepare-beta.sh --public-host SEU_IP
```

Os dois scripts geram exatamente a mesma coisa. Nunca envie ao Git `infra/.env`, `infra/runtime/`, JARs de plugins ou mapas.
