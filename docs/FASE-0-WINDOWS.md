# Fase 0 no Windows: a rede rodando no seu PC

Guia detalhado da [Fase 0 do passo a passo](PASSO-A-PASSO.md): montar e testar
tudo de graça no computador antes de alugar o VPS. Requisitos: Windows 10/11,
16 GB+ de RAM (24 GB dá folga para jogar na mesma máquina) e virtualização
habilitada na BIOS.

## 1. WSL 2 (~10 min + reinício)

O Docker no Windows roda sobre o WSL 2. No PowerShell **como administrador**:

```powershell
wsl --install --no-distribution
```

Reinicie quando pedir.

## 2. Docker Desktop (~15 min)

1. Baixe em <https://www.docker.com/products/docker-desktop/>.
2. Instale com **"Use WSL 2 instead of Hyper-V"** marcado.
3. Abra o Docker Desktop (pode pular a criação de conta) e teste:

```powershell
docker run --rm hello-world
```

## 3. Limitar a memória do WSL (~5 min)

Crie `C:\Users\SEU_USUARIO\.wslconfig`:

```ini
[wsl2]
memory=16GB
processors=12
```

16 GB espelha o VPS planejado — o teste local vira um ensaio fiel. Depois
execute `wsl --shutdown` e reabra o Docker Desktop.

## 4. Gerar as configurações (~2 min, uma vez só)

Na pasta do repositório:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\prepare-beta.ps1 -PublicHost "localhost"
```

## 5. Primeiro boot (~10 min na primeira vez)

```powershell
docker compose --env-file infra/.env -f infra/compose.yaml up -d proxy bedwars pvp
```

Sobe proxy, lobby, banco, BedWars e PvP. Acompanhe com `logs -f` (Ctrl+C sai
dos logs sem derrubar a rede).

## 6. Entrar e testar

- **Java, no mesmo PC:** `localhost:25565`, launcher na 26.2.
- **Bedrock, celular no mesmo Wi-Fi:** IP do PC (`ipconfig`), porta `19132`.
  Na primeira conexão, **permita o Docker no firewall em redes privadas**.
- Teste as duas edições e a troca de modo pelo lobby.

Até o ViaVersion estar na lista de plugins do BedWars, o cliente 26.2 entra no
lobby e no PvP mas **não** no BedWars (backend 1.20.4) — esperado, não é bug.

## 7. Dia a dia

Desligar sem perder nada (mundos e banco ficam em `infra/runtime/`):

```powershell
docker compose --env-file infra/.env -f infra/compose.yaml down
```

Ligar de novo: o mesmo `up -d` do passo 5, agora em segundos.

## 8. Plugins — a fase longa

Siga a Fase 5 do [passo a passo](PASSO-A-PASSO.md), tudo ainda no PC:
LuckPerms e spark primeiro; **ViaVersion + BedWars1058 25.2** no BedWars;
KitPvP conforme [PVP.md](PVP.md); e os testes obrigatórios de cada plugin
(memória estável após 5-6 partidas, menus num aparelho Bedrock real).

Quando a [checklist do beta](BETA-CHECKLIST.md) passar aqui, e só então, volte
à Fase 1 do passo a passo para contratar o VPS — com datas de teste marcadas.
