# Pacote de placas da Game Craft

Esta ramificacao existe so para **hospedar** o `gamecraft-java.zip`, o resource
pack das plaquinhas de cargo. O servidor manda esta URL ao jogador:

    https://raw.githubusercontent.com/JulioCasare/gamescraft.com.br/pacote/gamecraft-java.zip

Antes ele era servido por um nginx dentro de casa, e a URL era um endereco da
rede local: quem entrava de fora nao alcancava e via um quadradinho no lugar da
placa.

Trocar a arte: rode `infra/packs/atualizar.ps1` no projeto, depois traga o zip
novo para ca e faca commit. O SHA-1 muda junto e o compose e atualizado pelo
proprio script — se o zip mudar e o SHA-1 nao, o cliente recusa o download sem
dizer por que.
