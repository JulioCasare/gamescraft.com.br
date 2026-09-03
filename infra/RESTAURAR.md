# Levantar a rede numa máquina nova

O que a Game Craft precisa para voltar do zero está em três lugares, e nenhum
deles é o computador:

| O quê | Onde |
|---|---|
| Código, configuração e datapacks | GitHub: `JulioCasare/gamescraft.com.br` |
| Segredos (`infra/.env`) | Google Drive: `backup/gamescraft.com.br/infra/.env` |
| Os mundos e os dados dos plugins | Google Drive: `backup/gamescraft-backups/volumes/` |

O `.env` não está no GitHub de propósito: ele tem o segredo do Velocity, o token
do bot do Discord e as senhas do banco. Sem ele o proxy para de confiar nos
servidores, e todo mundo vira "not whitelisted".

## Ordem

**1. Instalar Docker Desktop e clonar o repositório.**

```bash
git clone https://github.com/JulioCasare/gamescraft.com.br.git
```

**2. Pôr o `.env` de volta** em `infra/.env`, copiado do Drive.

**3. Recriar os volumes a partir dos `.tar.gz`.**

Um volume por arquivo. O nome do volume é o nome do arquivo sem a extensão —
e ele tem de ser exatamente esse, porque é assim que o `compose.yaml` o procura:

```bash
for f in volumes/*.tar.gz; do
  v=$(basename "$f" .tar.gz)
  docker volume create "$v"
  docker run --rm -v "$v":/d -v "$PWD/volumes":/in alpine \
    sh -c "tar xzf /in/$(basename "$f") -C /d"
done
```

O `gc_maven_repo` não está no backup, e não faz falta: é só o cache do Maven, e
ele se refaz sozinho na primeira compilação do plugin.

**4. Subir a rede.**

```bash
docker compose up -d
```

## Conferir se deu certo

A ilha do MegaGames tem de ter 58 sinalizadores. Com o servidor de pé:

```bash
docker exec game-craft-beta-ctf-1 sh -c "echo 'abrir ilha' > /tmp/minecraft-console-in"
```

Depois, no jogo ou pelo console, `//pos1 -230,0,-230`, `//pos2 229,150,229` e
`//count beacon`. Se der 58, o mapa veio inteiro. Se der menos, use o zip:
`backup/gamescraft-backups/ilha_2026-08-25_0040_limpo.zip` é a versão consertada,
e é dele que toda partida nasce.

## Refazer o backup

Com os servidores parados, para os arquivos não serem copiados no meio de uma
gravação:

```bash
docker compose down
```

```powershell
$dest = "C:\Users\gamec\gamescraft-backups\volumes"
docker volume ls --format "{{.Name}}" | Where-Object { $_ -match "^(gc_|playit)" -and $_ -ne "gc_maven_repo" } | ForEach-Object {
  docker run --rm -v "${_}:/d" -v "${dest}:/out" alpine sh -c "tar czf /out/$_.tar.gz -C /d ."
}
```

E confira que cada arquivo abre antes de confiar nele — `tar tzf` tem de listar
mais que zero arquivos. Um backup que ninguém leu não é um backup.
