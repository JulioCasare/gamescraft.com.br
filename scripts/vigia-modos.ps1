# Vigia dos comandos /modo digitados no jogo.
#
# O Minecraft roda dentro de um container e nao tem como ligar ou desligar
# outro. A ponte e esta: o comando /modo <nome> on|off aparece no log do lobby,
# este script le a linha e executa o docker compose por fora, respeitando a
# mesma trava de 6 servidores de scripts/servidor.ps1. A resposta volta para o
# jogo por tellraw.
#
# Uso: powershell -ExecutionPolicy Bypass -File scripts\vigia-modos.ps1
# Encerre com Ctrl+C. Sem ele rodando, /modo nao faz nada.

$ErrorActionPreference = "Continue"
$env:Path = [Environment]::GetEnvironmentVariable("Path","Machine") + ";" + [Environment]::GetEnvironmentVariable("Path","User")

$repo = Split-Path -Parent $PSScriptRoot
$compose = @("compose", "--env-file", "$repo\infra\.env", "-f", "$repo\infra\compose.yaml")
$lobby = "game-craft-beta-lobby-1"
$modos = @("lobby", "bedwars", "pvp", "pillars", "buildbattle", "ctf", "eventos")
$maximo = 6

function Avisar($texto, $cor) {
    $json = '{"text":"' + ($texto -replace '"', "'") + '","color":"' + $cor + '"}'
    docker exec -u 1000 $lobby mc-send-to-console "tellraw @a $json" *> $null
}

function Ligados {
    $nomes = docker ps --format '{{.Names}}' 2>$null
    @($modos | Where-Object { $nomes -contains "game-craft-beta-$_-1" })
}

function Tratar($modo, $acao) {
    if ($modos -notcontains $modo) {
        Avisar "Modo desconhecido: $modo. Use: $($modos -join ', ')" "red"
        return
    }
    $on = Ligados
    switch ($acao) {
        "on" {
            if ($on -contains $modo) { Avisar "O $modo ja esta ligado." "yellow"; return }
            if ($on.Count -ge $maximo) {
                Avisar "NAO DA: ja tem $($on.Count) servidores ligados, o maximo que a memoria aguenta. Desligue um antes." "red"
                return
            }
            Avisar "Ligando o $modo... o boot leva 1 a 2 minutos." "green"
            & docker @compose up -d $modo *> $null
            Avisar "O $modo esta subindo. Entre com /server $modo quando terminar." "green"
        }
        "off" {
            if ($on -notcontains $modo) { Avisar "O $modo ja esta desligado." "yellow"; return }
            if ($modo -eq "lobby") { Avisar "Nao da para desligar o lobby: e por onde todo mundo entra." "red"; return }
            Avisar "Desligando o $modo. Nada se perde." "yellow"
            & docker @compose stop $modo *> $null
            Avisar "O $modo foi desligado." "yellow"
        }
        default {
            $lista = ($modos | ForEach-Object { if ($on -contains $_) { "$_ (ligado)" } else { $_ } }) -join ", "
            Avisar "Modos: $lista. Ligados: $($on.Count) de $maximo." "aqua"
        }
    }
}

Write-Output "Vigia dos comandos /modo ativo. Ctrl+C encerra."
Avisar "Controlador da rede conectado: /modo <nome> on|off ja funciona." "aqua"

docker logs -f --tail 0 $lobby 2>&1 | ForEach-Object {
    $linha = $_ -replace "`0", ""
    if ($linha -match "issued server command: /modo\s*(\S+)?\s*(\S+)?") {
        $modo = $Matches[1]
        $acao = $Matches[2]
        if (-not $modo) { $modo = "status" }
        if (-not $acao) { $acao = "status" }
        Write-Output "  pedido: $modo $acao"
        Tratar $modo.ToLower() $acao.ToLower()
    }
}
