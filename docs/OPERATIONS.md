# Operação e segurança

## Como a rede opera

Três peças, e só duas ficam no ar ao mesmo tempo:

| Peça | Papel |
|---|---|
| **BedWars** | Fixo. É a atração principal e nunca sai do ar |
| **Slot rotativo** | Um entre PvP, Pillars, Build Battle e CTF |
| **Eventos** | Sobe só no dia, conduzido por você |

O motivo de não abrir tudo é CPU, não memória: cada servidor Paper precisa da thread principal dele rodando a cada 50 ms, e isso não paraleliza. Em 4 vCPU, proxy, lobby e dois minigames já ocupam os quatro núcleos. Um terceiro modo cheio faz o MSPT subir em *todos* eles, não só no novo.

A restrição mais apertada, porém, nem é técnica: com 15–20 jogadores simultâneos, dois modos já é o máximo que consegue encher partida. Cinco modos abertos viram cinco filas vazias.

### Trocar o modo rotativo

```bash
./scripts/rotate.sh              # o que está no ar agora
./scripts/rotate.sh pillars      # troca o rotativo para Pillars
```

O script para o rotativo anterior antes de subir o novo. Mundos e estatísticas de todos os modos continuam salvos em `infra/runtime/` — trocar não apaga nada, e voltar é o mesmo comando.

**Escolha o rotativo pela população esperada, não pelo seu gosto.** PvP funciona com 4 pessoas; CTF precisa de 16 para não parecer abandonado. Em semana fraca, ou quando estiver na dúvida, volte para PvP: ele é o único que nunca fica vazio.

Avise a troca no Discord. Jogador que volta e não acha o modo que estava jogando some sem reclamar.

## Antes de expor o servidor

1. Hospede em uma VPS ou host de jogos com proteção DDoS adequada para Minecraft.
2. Libere somente 25565/TCP e 19132/UDP para jogadores; mantenha SSH, painel e banco em rede privada ou VPN.
3. Nunca exponha RCON. Nesta base ele está desativado.
4. Mantenha o segredo de Velocity privado. Se ele vazar, gere outro com um ambiente novo.
5. Use senhas únicas e autenticação de dois fatores no GitHub, Discord, host e domínio.
6. Adicione uma política de privacidade, regras, contato do responsável e o aviso de não afiliação ao Minecraft no site/loja.

## Rotina de operação

- Diariamente: confirme backups e leia alertas/erros.
- Semanalmente: aplique atualizações testadas primeiro no ambiente de testes.
- A cada atualização de plugin: faça snapshot antes e execute testes Java e Bedrock.
- A cada incidente: preserve logs, anote horário, jogadores afetados e versão dos plugins.

## Comandos úteis

```powershell
# Ver serviços e saúde
docker compose --env-file infra/.env -f infra/compose.yaml ps

# Acompanhar logs
docker compose --env-file infra/.env -f infra/compose.yaml logs -f proxy

# Reiniciar apenas um modo
docker compose --env-file infra/.env -f infra/compose.yaml restart bedwars

# Parar preservando dados
docker compose --env-file infra/.env -f infra/compose.yaml down
```

Não use `down -v` em produção: isso pode apagar volumes nomeados. Esta composição usa pastas locais persistentes em `infra/runtime/`, que também precisam de backup.
