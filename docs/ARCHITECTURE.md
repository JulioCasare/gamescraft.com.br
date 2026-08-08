# Arquitetura do beta

## Fluxo de conexão

```text
Cliente Java ─────── TCP 25565 ─┐                ┌─ Lobby
                                ├─ Velocity ─────┼─ BedWars
Cliente Bedrock ─ UDP 19132 ────┘   Geyser       ├─ Pillars of Fortune
                                    Floodgate    ├─ Build Battle
                                                 ├─ Capture the Flag
                                                 ├─ PvP
                                                 └─ Eventos (so no dia)
                                                        |
                                                     MariaDB
```

O proxy autentica jogadores Java, recebe jogadores Bedrock através do Geyser e encaminha a conexão ao Paper correto. Floodgate cria uma identidade válida para jogadores Bedrock sem exigir uma conta Java.

## Por que esta separação

- Um travamento ou reinício de BedWars não tira todo o lobby do ar.
- Cada modo ganha memória e configuração próprias (`MEMORY_*` em `infra/.env`).
- Modos com regras opostas convivem sem gambiarra: Build Battle roda em paz, sem PvP e com voo liberado, enquanto CTF e PvP ficam em dificuldade normal com PvP ativo.
- O servidor `eventos` fica isolado porque suas regras estragariam qualquer minigame: `survival`, PvP ativo, voo liberado e mapa que é destruído a cada evento. Ele não sobe com a rede — veja [EVENTS.md](EVENTS.md).
- Somente duas portas são públicas: 25565/TCP e 19132/UDP.
- O banco não é acessível pela internet.
- Velocity modern forwarding preserva UUID, IP e skins corretamente e protege contra conexões diretas aos backends quando o segredo é mantido privado.

## Versão do jogo

A versão é fixada **por modo**, presa à compatibilidade dos plugins de cada um — os backends não precisam estar todos na mesma versão, porque o Velocity roteia entre eles:

- **Modos gerais (lobby, PvP, eventos, Pillars, Build Battle, CTF): Paper 26.2.** O motivo é a lança, adicionada na 1.21.9: ela precisa existir no PvP e nos eventos.
- **BedWars: Paper 1.20.4**, a versão máxima do BedWars1058 25.2 estável (plugins de BedWars usam NMS e só rodam nas versões que suportam). O **ViaVersion instalado nesse backend** traduz o protocolo dos clientes mais novos — incluindo o Geyser, que emula um cliente Java 26.2 — para que todos entrem normalmente. Efeito colateral aceito: dentro do BedWars não existe lança.

Não atualize um backend para uma versão nova apenas porque ela foi lançada: primeiro replique o ambiente, valide os plugins do modo, o Geyser, o anti-cheat e os mapas.

### Descer um backend de versão

Aprendido na prática ao trocar 1.21.4 por 1.20.4 (2026-08-08): **configuração do Paper não volta de versão.** Ao carregar, o Paper novo reescreve `config/paper-global.yml` e `config/paper-world-defaults.yml` com os campos da versão dele; alguns aceitam o valor `default`, e o Paper antigo tenta lê-los como número e morre no boot (`NumberFormatException: For input string: "default"`). Ao descer de versão, apague os configs gerados — `config/paper-*.yml`, `spigot.yml`, `bukkit.yml`, `commands.yml`, `help.yml`, `permissions.yml` — e regenere o `paper-global.yml` a partir de `infra/config/paper/paper-global.yml.template`, que contém só o bloco do Velocity. O mundo também não volta: gere um mundo novo na versão de destino e reinstale as regiões do mapa por cima.

### Mapa de era antiga

O conversor do Paper 1.20/1.21 **não** converte mapas da era 1.12 direto — os chunks carregam vazios, sem erro nenhum no log. O caminho que funciona é converter em etapas, com containers descartáveis e `--forceUpgrade`: `1.12 → Paper 1.16.5 → versão de destino`. Confira o resultado antes de instalar: um chunk convertido tem dezenas de tipos de bloco; um chunk vazio tem só `air`, `full` e `the_void`.

## O que não entra no Git

- Mundos e mapas grandes.
- JARs de servidor e plugins.
- Logs, bancos de dados e crash reports.
- `.env`, `forwarding.secret`, tokens, senhas e IPs privados.

Faça backups dos mundos e do MariaDB fora desta máquina e teste a restauração antes do lançamento.
