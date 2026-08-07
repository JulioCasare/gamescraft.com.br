# Passo a passo: da compra do servidor ao beta

A ordem importa mais que a velocidade. Cada fase depende da anterior, e pular uma costuma custar retrabalho — ou dados perdidos.

O tempo estimado é de execução, não de espera: escolher plugin e construir mapa levam dias, não horas.

---

## Fase 0 — Tudo pronto no seu PC, de graça (semanas, sem pressa)

O VPS só recebe coisa pronta. Todo trabalho demorado acontece aqui, onde o
tempo não custa nada — se for feito depois da compra, ele queima o prazo de
reembolso em tarefa que podia ser de graça.

- [ ] Rede subindo no Docker local — guia detalhado em [FASE-0-WINDOWS.md](FASE-0-WINDOWS.md)
- [ ] Plugins escolhidos, instalados e configurados (a fase mais longa do projeto)
- [ ] Kits e regras do PvP montados conforme [PVP.md](PVP.md)
- [ ] Mapas instalados e testados
- [ ] Amigos entrando pelo Java e pelo Bedrock na sua rede local
- [ ] A [checklist do beta](BETA-CHECKLIST.md) passando em casa

Só siga para a Fase 1 com **datas marcadas** com os testadores: o prazo de
reembolso de 30 dias começa na compra, e o beta técnico precisa caber dentro
dele. No VPS você testa apenas o que só ele pode provar: latência real, gente
entrando de fora, backup e carga.

## Fase 1 — Contratar (30 min)

- [ ] Contratar **Hostinger KVM 4**: 4 vCPU, 16 GB, 200 GB NVMe
- [ ] Localização: **Brasil (São Paulo)** — confira antes de finalizar, é o item que invalida tudo se errar
- [ ] Sistema: **Ubuntu 24.04**
- [ ] Prazo: **12 meses**, não 24 — você provavelmente vai superar esse plano antes disso
- [ ] Se o assistente oferecer, cadastre sua **chave SSH** já na criação
- [ ] Guardar IP e senha de root

> São **30 dias de reembolso**. As fases 2 a 4 e a primeira medição precisam caber nesse prazo — é a sua janela para descobrir se o servidor serve, sem custo.

## Fase 2 — Deixar o servidor seguro (1–2 h)

Antes de instalar qualquer coisa. VPS novo com senha de root exposta é varrido por bots em minutos.

- [ ] Criar usuário sem root, com sudo
- [ ] Configurar chave SSH
- [ ] **Testar o acesso por chave em um segundo terminal** antes de desativar a senha
- [ ] Desativar `PermitRootLogin` e `PasswordAuthentication`
- [ ] UFW: liberar **OpenSSH primeiro**, depois 25565/tcp e 19132/udp, e só então ativar
- [ ] Instalar Docker e validar com `docker run --rm hello-world`

Comandos completos em [DEPLOY.md](DEPLOY.md).

## Fase 3 — Rede no ar (1 h)

- [ ] `git clone` do repositório
- [ ] `./scripts/prepare-beta.sh --public-host SEU_IP`
- [ ] Aplicar o perfil de memória de 16 GB do [README](../README.md)
- [ ] Subir: `docker compose --env-file infra/.env -f infra/compose.yaml up -d proxy bedwars pvp`
- [ ] Entrar pelo **Java** e confirmar que chega no lobby
- [ ] Entrar pelo **Bedrock** e confirmar o mesmo
- [ ] Trocar de modo pelo lobby, nas duas edições

Neste ponto a rede funciona, mas está vazia: Paper puro, sem minigame.

## Fase 4 — Backup (30 min)

**Antes de construir qualquer conteúdo.** A partir da próxima fase você começa a acumular trabalho que dói perder: configuração de plugin, mapas, permissões.

- [ ] Script de backup diário no cron
- [ ] **Copiar os arquivos para fora do servidor** — Drive, Backblaze, outra máquina
- [ ] **Restaurar um backup e verificar que funciona**

Backup nunca testado costuma falhar exatamente no dia em que você precisa. Backup no mesmo disco não é backup.

## Fase 5 — Plugins (dias)

A fase mais longa e a que realmente define o servidor. Instale na ordem: a base primeiro, o conteúdo depois.

**Base**

- [ ] **LuckPerms** — permissões. Quase todo plugin depende dele; instale antes dos outros
- [ ] **spark** — medição de TPS/MSPT. Sem ele você chuta em vez de medir
- [ ] **Anti-cheat** — com exceção configurada para a sua conta em criativo
- [ ] **Moderação** — ban, mute e registro de ocorrência

**Minigames**

- [ ] Plugin de **BedWars**, avaliado pelos [5 critérios](../infra/plugins/README.md) — o escolhido é o BedWars1058 25.3-SNAPSHOT (a 25.2 estável só vai até 1.20.4); os testes abaixo confirmam a escolha
- [ ] Plugin do modo **rotativo** (comece por PvP)

**Testes obrigatórios de cada plugin**

- [ ] Rodar 5–6 partidas seguidas com `docker stats` aberto — se a memória sobe e não volta, o plugin vaza mundo e deve ser descartado
- [ ] Abrir loja, seleção de time e placar **em um aparelho Bedrock de verdade**
- [ ] Confirmar que dá para rodar partidas em formato pequeno (solo/duplas)

## Fase 6 — Conteúdo (dias)

- [ ] Mapa do **lobby**, com portais ou NPCs para trocar de modo
- [ ] Pelo menos **uma arena de BedWars**, com licença ou autorização de uso
- [ ] Arena do modo rotativo
- [ ] Primeiro mapa de evento em `infra/runtime/event-maps/`
- [ ] Confirmar que todo mapa usado pode ser redistribuído legalmente

## Fase 7 — Comunidade (2–3 h)

- [ ] **Regras escritas** no Discord: trapaça, conta alternativa, assédio, evasão de punição
- [ ] Canal de bugs no Discord, além do [template de issue](../.github/ISSUE_TEMPLATE/bug_report.yml)
- [ ] Definir quem modera além de você — uma pessoa já ajuda
- [ ] Aviso de **não afiliação** à Mojang/Microsoft, no Discord e no site

## Fase 8 — Beta fechado

- [ ] **Convidar 10 pessoas primeiro, não 50**
- [ ] Medir MSPT com 10 jogadores e anotar
- [ ] Corrigir o que aparecer, por ordem de impacto
- [ ] Abrir para 25, medir de novo
- [ ] Abrir para 50, medir de novo
- [ ] Rodar o [primeiro evento semanal](EVENTS.md), depois de testá-lo sozinho
- [ ] Concluir a [checklist do beta](BETA-CHECKLIST.md)

Bug encontrado com 10 amigos custa nada. O mesmo bug com 50 testadores queima a boa vontade que você levou semanas para juntar — e ela não volta.

## Fase 9 — Antes de abrir ao público

- [ ] Domínio apontado para o servidor
- [ ] Reavaliar **proteção DDoS**: a Hostinger não oferece a de nível de jogo, e servidor público de PvP é alvo
- [ ] Loja de cosméticos, sem vantagem competitiva
- [ ] Política de privacidade, contato do responsável e regras publicadas
- [ ] Backup testado outra vez, agora com dados reais
- [ ] Decidir o 3º modo com base nos dados dos eventos — não no palpite
- [ ] **Clipes de partida real gravados e editados** (grave durante o beta com os amigos)
- [ ] Abrir **no mesmo dia** da publicação dos clipes, com horário fixo anunciado — servidor aberto sem conteúdo circulando é indistinguível de servidor fechado

Antes de abrir, releia a parte de população no [ROADMAP](ROADMAP.md): mais modos com a mesma quantidade de gente deixa todos vazios.

---

## Os erros que mais custam

**Ativar o UFW sem liberar o SSH.** Você se tranca para fora e só o suporte resolve.

**Deixar o backup para depois.** Ele parece burocracia até o dia em que salva semanas de trabalho.

**Convidar 50 pessoas de uma vez.** Testador é recurso escasso e não renovável.

**Abrir modo demais.** Cinco modos com 20 jogadores online são cinco filas vazias.

**Aumentar RAM achando que resolve lag.** Se o MSPT sobe com poucos jogadores, o problema é plugin ou mapa. Servidor maior só faz você pagar mais pelo mesmo travamento.
