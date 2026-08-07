#!/usr/bin/env bash
# Equivalente Linux de prepare-beta.ps1, para uso no servidor.
# Gera o segredo do Velocity e as configuracoes dos seis backends.
# Nada gerado aqui entra no Git: veja .gitignore.

set -euo pipefail

repository_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
infra_root="$repository_root/infra"
runtime_root="$infra_root/runtime"

public_host="localhost"
minecraft_version="26.2"

while [[ $# -gt 0 ]]; do
    case "$1" in
        --public-host)
            public_host="${2:?--public-host precisa de um valor}"
            shift 2
            ;;
        --minecraft-version)
            minecraft_version="${2:?--minecraft-version precisa de um valor}"
            shift 2
            ;;
        *)
            echo "Opcao desconhecida: $1" >&2
            echo "Uso: $0 [--public-host HOST] [--minecraft-version VERSAO]" >&2
            exit 1
            ;;
    esac
done

new_secret() {
    # base64url, sem '=' no fim: o mesmo formato gerado no Windows.
    openssl rand -base64 36 | tr '+/' '-_' | tr -d '='
}

mkdir -p "$runtime_root"

environment_path="$infra_root/.env"
environment_kept=""
if [[ -f "$environment_path" ]]; then
    environment_kept="1"
    velocity_secret="$(sed -n 's/^VELOCITY_SECRET=//p' "$environment_path" | head -n 1)"
    if [[ -z "$velocity_secret" || "$velocity_secret" == "REPLACE_ME" ]]; then
        velocity_secret="$(new_secret)"
    fi
else
    velocity_secret="$(new_secret)"
    database_password="$(new_secret)"
    database_root_password="$(new_secret)"
    sed \
        -e "s|^PUBLIC_HOST=localhost|PUBLIC_HOST=${public_host}|" \
        -e "s|^MINECRAFT_VERSION=26.2|MINECRAFT_VERSION=${minecraft_version}|" \
        -e "s|^VELOCITY_SECRET=REPLACE_ME|VELOCITY_SECRET=${velocity_secret}|" \
        -e "s|^DATABASE_PASSWORD=REPLACE_ME|DATABASE_PASSWORD=${database_password}|" \
        -e "s|^DATABASE_ROOT_PASSWORD=REPLACE_ME|DATABASE_ROOT_PASSWORD=${database_root_password}|" \
        "$infra_root/.env.example" > "$environment_path"
    chmod 600 "$environment_path"
fi

proxy_config="$runtime_root/proxy-config"
mkdir -p "$proxy_config"
sed "s|__VELOCITY_SECRET__|${velocity_secret}|" \
    "$infra_root/config/velocity/velocity.toml.template" > "$proxy_config/velocity.toml"
printf '%s' "$velocity_secret" > "$proxy_config/forwarding.secret"
chmod 600 "$proxy_config/forwarding.secret"

for server_name in lobby bedwars pillars buildbattle ctf pvp eventos; do
    mkdir -p "$runtime_root/$server_name/config"
    sed "s|__VELOCITY_SECRET__|${velocity_secret}|" \
        "$infra_root/config/paper/paper-global.yml.template" \
        > "$runtime_root/$server_name/config/paper-global.yml"
done

mkdir -p "$runtime_root/proxy" "$runtime_root/mariadb"

# Icone da lista de servidores. O Velocity le server-icon.png do proprio
# diretorio e exige exatamente 64x64; fora disso ele ignora o arquivo.
server_icon="$infra_root/branding/server-icon.png"
if [[ -f "$server_icon" ]]; then
    cp -f "$server_icon" "$runtime_root/proxy/server-icon.png"
    echo "Icone do servidor aplicado."
fi

# Mapas-modelo dos eventos. Cada evento roda sobre uma copia limpa daqui, porque
# lava sobe, explosao e bloco quebrado destroem o mapa durante a partida.
mkdir -p "$runtime_root/event-maps"

# As imagens itzg rodam como UID 1000. Se as pastas ficarem de outro dono, o
# container sobe e falha ao escrever no mundo. Ignora o erro quando o usuario
# atual ja e o dono correto e nao tem permissao para chown.
for directory in proxy proxy-config lobby bedwars pillars buildbattle ctf pvp eventos event-maps; do
    chown -R 1000:1000 "$runtime_root/$directory" 2>/dev/null || true
done

echo
echo "Ambiente beta preparado."
if [[ -n "$environment_kept" ]]; then
    echo "infra/.env ja existia e foi MANTIDO: host e versao nao foram alterados."
    echo "Confira MINECRAFT_VERSION e as demais variaveis contra infra/.env.example."
else
    echo "Host configurado: $public_host"
    echo "Versao Java configurada: $minecraft_version"
fi
echo "Segredos foram gravados apenas em infra/.env e infra/runtime/ (ignorados pelo Git)."
echo "Inicie com: docker compose --env-file infra/.env -f infra/compose.yaml up -d"
