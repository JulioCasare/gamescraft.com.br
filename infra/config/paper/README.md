# Configuração comum dos servidores Paper

## `patches/velocity.json`

Liga o repasse moderno do Velocity em todo servidor de jogo, a cada arranque.

Sem ele o servidor aceita a conexão mas não recebe a identidade que o proxy
manda: o jogador chega com um UUID inventado a partir do nome. Whitelist, op e
permissões do LuckPerms passam todos a olhar para outra pessoa — e o sintoma
que aparece é "You are not whitelisted on this server", que não diz nada sobre
a causa. Foi o que aconteceu com o servidor do Among Us no dia em que nasceu.

Os outros seis servidores tinham esse ajuste porque foram configurados um a um,
à mão. Nada garantia o mesmo para o próximo — e o próximo veio.

O remendo mexe só nas três chaves do Velocity, e não substitui o arquivo
inteiro: `paper-global.yml` muda de versão para versão, e esta rede roda três
versões diferentes do jogo ao mesmo tempo (1.20.4 no Bed Wars, 1.21.11 no Among
Us, 26.2 no resto).

O segredo vem de `VELOCITY_SECRET`, no `infra/.env`, que não vai para o Git. O
prefixo `CFG_` é o que o `mc-image-helper` procura ao substituir variáveis.

### Servidor recém-criado

O `paper-global.yml` só existe depois que o servidor roda pela primeira vez,
então no primeiro arranque não há o que remendar. Suba o servidor, deixe ele
terminar, e reinicie uma vez — daí em diante o remendo vale sozinho.
