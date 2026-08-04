# Plugins e mapas

Os arquivos `*.txt` desta pasta são listas de URLs que o container baixa no primeiro boot. Eles evitam guardar JARs no Git, mas uma URL ainda é uma decisão de segurança: só inclua projetos revisados.

## Regras de escolha

1. Baixe somente de fonte oficial do projeto, Hangar, Modrinth, SpigotMC ou marketplace autorizado.
2. Registre versão, licença, URL de origem e data de teste no issue correspondente.
3. Teste em uma cópia do ambiente antes de colocar em produção.
4. Não use JARs "crackeados", enviados por desconhecidos ou sem licença clara.
5. Não publique arquivos de mapas que você não tenha direito de redistribuir.

## Decisões ainda pendentes

- BedWars: escolher plugin pago ou open source com suporte a Geyser/Floodgate.
- Pillars of Fortune: validar a experiência Bedrock e possíveis dados de telemetria.
- Build Battle: conferir se os menus de tema, o seletor de blocos e a votação funcionam pelo Geyser.
- Capture the Flag: confirmar indicação de bandeira, cores de time e placar no Bedrock.
- PvP: decidir se será arena livre ou KitPvP, e manter os kits fora da loja.
- Anti-cheat: escolher um que lide bem com as diferenças de movimento do Bedrock e com o voo criativo do Build Battle.
- Permissões, punições, chat e estatísticas: escolher uma combinação compatível entre si.

Prefira, quando possível, um único plugin que cubra vários modos: menos superfície para conflitos, uma licença só e um lugar só para atualizar. Se cada modo vier de um autor diferente, teste a combinação inteira antes de abrir o beta.

O arquivo [BETA-CHECKLIST](../../docs/BETA-CHECKLIST.md) define os testes mínimos antes de abrir o acesso.
