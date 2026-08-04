# Checklist para beta fechado

## Infraestrutura

- [ ] Docker e todos os oito serviços iniciam sem erro: proxy, lobby, bedwars, pillars, buildbattle, ctf, pvp e database.
- [ ] Docker tem RAM suficiente para os seis servidores Paper mais o proxy.
- [ ] Apenas 25565/TCP e 19132/UDP estão públicos.
- [ ] Backends e MariaDB não respondem diretamente fora da rede Docker.
- [ ] Backup de `infra/runtime/` foi criado e restaurado em outro local.
- [ ] Monitoramento de CPU, RAM, TPS/MSPT e disco está ativo.

## Jogabilidade

- [ ] Jogador Java entra no lobby e muda de modo.
- [ ] Jogador Bedrock entra no lobby e muda de modo.
- [ ] Nomes, skins, UUIDs e punições funcionam para ambos.
- [ ] Menus, chat, placares, loja e itens funcionam no Bedrock.
- [ ] BedWars tem ao menos uma arena testada do começo ao fim.
- [ ] Pillars of Fortune tem ao menos uma arena testada do começo ao fim.
- [ ] Build Battle tem ao menos um mapa testado: temas, votação, tempo e salvamento da construção.
- [ ] Capture the Flag tem ao menos um mapa testado: captura, retorno da bandeira e placar.
- [ ] PvP tem ao menos uma arena testada, com kits idênticos para todos.
- [ ] Voar dentro da parcela de Build Battle não gera expulsão nem falso positivo do anti-cheat.
- [ ] Sair de uma partida pelo lobby devolve o jogador ao estado correto em todos os modos.
- [ ] Todos os mapas possuem licença/autorização de uso.

## Segurança e comunidade

- [ ] Regra contra contas alternativas, trapaça, assédio e evasão de punição.
- [ ] Equipe de moderação definida e treinada para registrar ocorrências.
- [ ] Sistema de denúncia no Discord.
- [ ] Anti-cheat validado com jogadores Bedrock para reduzir falsos positivos.
- [ ] Canal de bugs no Discord e template de issue no GitHub.
- [ ] Aviso de não afiliação ao Minecraft no site e loja.

## Teste de carga

- [ ] 10 jogadores simultâneos: sem erros críticos.
- [ ] 25 jogadores simultâneos: TPS e MSPT anotados.
- [ ] 50 jogadores simultâneos: gargalos registrados e corrigidos.
- [ ] Reinício controlado de cada minigame sem derrubar o proxy.
