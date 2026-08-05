# Game Craft

Base de infraestrutura do servidor **Game Craft**: uma rede de minigames para jogadores Java e Bedrock.

> Este servidor não é um produto oficial do Minecraft, Mojang Studios ou Microsoft.

## Beta inicial

- Capacidade planejada: até 50 jogadores simultâneos.
- Edição Java: Paper 1.21.8, mantida fixa durante o beta para preservar a compatibilidade dos plugins.
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

Seis servidores Paper mais o proxy pedem cerca de **11,5 GB** de heap com os valores padrão de `infra/.env`, e o Docker ainda precisa de espaço para o MariaDB e para os próprios containers. Reserve **16 GB** para o Docker.

No Windows, o Docker Desktop usa o WSL 2 e por padrão pega apenas metade da RAM da máquina. Para fixar o limite, crie `C:\Users\<seu-usuario>\.wslconfig`:

```ini
[wsl2]
memory=16GB
processors=12
```

Depois execute `wsl --shutdown` e reabra o Docker Desktop.

Se preferir não subir tudo de uma vez, nomeie só os modos que vai testar — o proxy, o lobby e o banco sobem junto como dependência:

```powershell
docker compose --env-file infra/.env -f infra/compose.yaml up -d proxy bedwars
```

Para ajustar o heap de um modo, mude o `MEMORY_*` correspondente em `infra/.env`. Aumente somente depois que o `spark` mostrar falta real de memória: heap grande demais deixa as pausas de coleta de lixo mais longas.

## Antes de convidar testadores

Leia e conclua [a checklist do beta](docs/BETA-CHECKLIST.md). Em especial, escolha plugins compatíveis, instale mapas licenciados, configure moderação e teste Java e Bedrock.

- [Instalar no servidor](docs/DEPLOY.md)
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
