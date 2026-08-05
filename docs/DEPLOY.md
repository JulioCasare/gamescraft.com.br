# Instalar no servidor

Passo a passo para colocar a rede em um VPS Ubuntu 24.04 com Docker. Faça na ordem: o firewall vem antes do Docker, e o Docker antes do jogo.

Você vai precisar do IP do servidor e da senha de root que o provedor enviou.

## 1. Primeiro acesso e usuário sem root

No seu computador:

```powershell
ssh root@SEU_IP
```

Já dentro do servidor, crie um usuário comum. Trabalhar como root o tempo todo faz com que qualquer erro de comando vire um estrago permanente:

```bash
adduser gamecraft
usermod -aG sudo gamecraft
```

## 2. Chave SSH e bloqueio de senha

Senha em SSH exposto na internet é quebrada por força bruta em questão de horas. Volte para o **seu computador** e gere uma chave:

```powershell
ssh-keygen -t ed25519 -C "gamecraft"
type $env:USERPROFILE\.ssh\id_ed25519.pub
```

Copie a linha inteira que apareceu. No servidor, como o novo usuário:

```bash
su - gamecraft
mkdir -p ~/.ssh && chmod 700 ~/.ssh
nano ~/.ssh/authorized_keys   # cole a linha, salve com Ctrl+O e saia com Ctrl+X
chmod 600 ~/.ssh/authorized_keys
```

**Antes de desativar a senha, abra um segundo terminal e confirme que `ssh gamecraft@SEU_IP` funciona sem pedir senha.** Se você desativar a senha com a chave errada, perde o acesso ao servidor e só o suporte do provedor resolve.

Confirmado, desative o login por senha e o acesso direto do root:

```bash
sudo nano /etc/ssh/sshd_config
```

Ajuste estas três linhas:

```text
PermitRootLogin no
PasswordAuthentication no
PubkeyAuthentication yes
```

```bash
sudo systemctl restart ssh
```

## 3. Firewall

Libere o SSH **primeiro**. Ativar o UFW sem essa regra derruba a sua própria conexão:

```bash
sudo ufw allow OpenSSH
sudo ufw allow 25565/tcp comment 'Minecraft Java'
sudo ufw allow 19132/udp comment 'Minecraft Bedrock'
sudo ufw enable
sudo ufw status verbose
```

Só essas três portas. O MariaDB e os seis servidores Paper ficam na rede interna do Docker e não recebem conexão de fora — no [compose.yaml](../infra/compose.yaml) eles usam `expose`, não `ports`.

Uma coisa que engana muita gente: **o Docker escreve regras de iptables direto e as portas publicadas com `ports:` passam por cima do UFW.** Aqui isso não é problema porque só o proxy publica portas, e são exatamente as duas que devem ser públicas. Mas não conte com o UFW para fechar uma porta de container: se quiser fechar, tire do `compose.yaml`.

## 4. Docker

```bash
curl -fsSL https://get.docker.com | sudo sh
sudo usermod -aG docker gamecraft
```

Saia e entre de novo no SSH para o grupo valer, e confirme:

```bash
docker run --rm hello-world
```

## 5. Baixar o projeto e gerar os segredos

```bash
sudo apt update && sudo apt install -y git
cd ~
git clone https://github.com/JulioCasare/gamescraft.com.br.git
cd gamescraft.com.br
chmod +x scripts/prepare-beta.sh
./scripts/prepare-beta.sh --public-host SEU_IP
```

O script cria `infra/.env` com senhas aleatórias e grava o segredo do Velocity nos seis backends. Esses arquivos ficam só no servidor — o `.gitignore` impede que voltem para o GitHub.

Se já tiver o domínio apontado, use ele no lugar do IP em `--public-host`.

## 6. Ajustar a memória e subir

Com 8 GB de RAM, rode três modos. Edite `infra/.env`:

```ini
MEMORY_PROXY=512M
MEMORY_LOBBY=1G
MEMORY_BEDWARS=2G
MEMORY_PILLARS=1G
```

O heap não é o consumo total: cada JVM usa de 25% a 40% a mais fora do heap. Esses valores dão ~6 GB reais e deixam folga para o MariaDB e o sistema. Subir os números até encostar nos 8 GB faz o kernel matar um servidor no meio da partida.

```bash
docker compose --env-file infra/.env -f infra/compose.yaml up -d proxy bedwars pillars
docker compose --env-file infra/.env -f infra/compose.yaml logs -f
```

O primeiro boot baixa Paper, Velocity, Geyser e Floodgate e demora alguns minutos. Espere aparecer `Done` em cada servidor.

## 7. Testar

- **Java:** adicione o servidor como `SEU_IP` (porta 25565 é o padrão).
- **Bedrock:** servidor `SEU_IP`, porta `19132`.

Entre com as duas edições e troque de modo. Se o Java entra e o Bedrock não, o problema é a porta UDP — confirme a regra do UFW e pergunte ao provedor se o anti-DDoS está descartando UDP.

## 8. Backup para fora da máquina

Backup no mesmo disco não é backup. Se a VPS morrer, você perde os dois juntos.

```bash
mkdir -p ~/backups
nano ~/backup-gamecraft.sh
```

```bash
#!/usr/bin/env bash
set -euo pipefail
cd ~/gamescraft.com.br
stamp="$(date +%Y-%m-%d-%H%M)"
tar -czf ~/backups/gamecraft-$stamp.tar.gz infra/runtime infra/.env
find ~/backups -name 'gamecraft-*.tar.gz' -mtime +7 -delete
```

```bash
chmod +x ~/backup-gamecraft.sh
crontab -e     # adicione a linha abaixo
```

```text
0 5 * * * /home/gamecraft/backup-gamecraft.sh
```

Isso gera um backup diário às 5h e apaga os com mais de 7 dias. **Falta a parte mais importante:** copiar esses arquivos para fora do servidor — Google Drive, Backblaze, outro VPS, o que preferir. E testar a restauração uma vez, antes do beta. Backup nunca testado costuma não funcionar exatamente no dia em que você precisa.

## 9. Medir antes de convidar gente

Instale o [spark](https://spark.lucko.me/) nos servidores e rode durante um teste com jogadores:

```text
/spark tps
/spark profiler start
```

O número que importa é o **MSPT**: quanto tempo cada tick leva. Abaixo de 50 ms está saudável; passando disso o servidor começa a atrasar. Se o MSPT subir com poucos jogadores, o gargalo é plugin ou mapa, não hardware — aumentar o plano não resolve.

Anote os valores com 10, 25 e 50 jogadores, como pede a [checklist do beta](BETA-CHECKLIST.md).

## 10. Domínio (opcional)

Com `gamescraft.com.br` apontado para o servidor, os jogadores digitam o nome em vez do IP:

| Tipo | Nome | Valor |
|---|---|---|
| A | `@` ou `mc` | IP do servidor |

Bedrock não usa registro SRV: os jogadores precisam informar o endereço e a porta `19132` na tela de adicionar servidor.

## O que ainda falta

Com tudo isso pronto você tem a rede no ar — mas **sem os minigames**. Os arquivos em [infra/plugins/](../infra/plugins/) ainda não têm nenhuma URL, então os servidores sobem como Paper vazio.

Escolher os plugins de BedWars e Pillars, seguindo as regras de [infra/plugins/README.md](../infra/plugins/README.md), é o passo que transforma isso em servidor de verdade.
