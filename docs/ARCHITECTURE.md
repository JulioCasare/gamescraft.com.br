# Arquitetura do beta

## Fluxo de conexão

```text
Cliente Java ─────── TCP 25565 ─┐                ┌─ Lobby
                                ├─ Velocity ─────┼─ BedWars
Cliente Bedrock ─ UDP 19132 ────┘   Geyser       ├─ Pillars of Fortune
                                    Floodgate    ├─ Build Battle
                                                 ├─ Capture the Flag
                                                 └─ PvP
                                                        |
                                                     MariaDB
```

O proxy autentica jogadores Java, recebe jogadores Bedrock através do Geyser e encaminha a conexão ao Paper correto. Floodgate cria uma identidade válida para jogadores Bedrock sem exigir uma conta Java.

## Por que esta separação

- Um travamento ou reinício de BedWars não tira todo o lobby do ar.
- Cada modo ganha memória e configuração próprias (`MEMORY_*` em `infra/.env`).
- Modos com regras opostas convivem sem gambiarra: Build Battle roda em paz, sem PvP e com voo liberado, enquanto CTF e PvP ficam em dificuldade normal com PvP ativo.
- Somente duas portas são públicas: 25565/TCP e 19132/UDP.
- O banco não é acessível pela internet.
- Velocity modern forwarding preserva UUID, IP e skins corretamente e protege contra conexões diretas aos backends quando o segredo é mantido privado.

## Versão do jogo

O repositório fixa o beta em **Paper 1.21.8**. O objetivo é ter uma base madura para plugins de minigame. Não atualize para uma versão nova apenas porque ela foi lançada: primeiro replique o ambiente, valide BedWars, Pillars, Geyser, anti-cheat e mapas.

## O que não entra no Git

- Mundos e mapas grandes.
- JARs de servidor e plugins.
- Logs, bancos de dados e crash reports.
- `.env`, `forwarding.secret`, tokens, senhas e IPs privados.

Faça backups dos mundos e do MariaDB fora desta máquina e teste a restauração antes do lançamento.
