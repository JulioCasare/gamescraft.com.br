# Operação e segurança

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
