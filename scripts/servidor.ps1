# Liga e desliga servidores da rede, com trava de capacidade.
# Uso:  .\scripts\servidor.ps1 status
#       .\scripts\servidor.ps1 ligar <modo>
#       .\scripts\servidor.ps1 desligar <modo>
# Modos: lobby bedwars pvp pillars buildbattle ctf eventos
#
# A trava: no maximo 6 servidores Minecraft ligados ao mesmo tempo. O setimo
# estoura a memoria — foi testado na pratica (2026-08-07): com os 7 juntos o
# kernel mata processos e arquivos corrompem. Desligue um antes de ligar outro.

param(
    [Parameter(Position = 0)][string]$Acao,
    [Parameter(Position = 1)][string]$Modo
)

$ErrorActionPreference = "Stop"
$modos = @("lobby", "bedwars", "pvp", "pillars", "buildbattle", "ctf", "eventos")
$maximo = 6

$repositorio = Split-Path -Parent $PSScriptRoot
$composeArgs = @("compose", "--env-file", (Join-Path $repositorio "infra\.env"), "-f", (Join-Path $repositorio "infra\compose.yaml"))

function Get-Ligados {
    $nomes = docker ps --format '{{.Names}}' 2>$null
    $ligados = @()
    foreach ($m in $modos) {
        if ($nomes -contains "game-craft-beta-$m-1") { $ligados += $m }
    }
    return $ligados
}

function Show-Status {
    $ligados = Get-Ligados
    Write-Host ""
    Write-Host "Servidores da rede Game Craft:" -ForegroundColor Cyan
    foreach ($m in $modos) {
        if ($ligados -contains $m) {
            Write-Host ("  {0,-12} LIGADO" -f $m) -ForegroundColor Green
        }
        else {
            Write-Host ("  {0,-12} desligado" -f $m) -ForegroundColor DarkGray
        }
    }
    Write-Host ""
    Write-Host ("Ligados: {0} de {1} (maximo: {2})" -f $ligados.Count, $modos.Count, $maximo)
}

if (-not $Acao -or $Acao -eq "status") {
    Show-Status
    exit 0
}

if ($Acao -notin @("ligar", "desligar")) {
    Write-Host "Acao desconhecida: $Acao. Use: status, ligar <modo> ou desligar <modo>."
    exit 1
}

if (-not $Modo -or $Modo -notin $modos) {
    Write-Host "Informe um modo valido: $($modos -join ', ')"
    exit 1
}

$ligados = Get-Ligados

if ($Acao -eq "ligar") {
    if ($ligados -contains $Modo) {
        Write-Host "O $Modo ja esta ligado."
        exit 0
    }
    if ($ligados.Count -ge $maximo) {
        Write-Host ""
        Write-Host "NAO DA: ja tem $($ligados.Count) servidores ligados, o maximo que a memoria aguenta." -ForegroundColor Red
        Write-Host "Ligar o $Modo agora derrubaria a rede inteira (aconteceu em 2026-08-07)."
        Write-Host "Desligue um primeiro: .\scripts\servidor.ps1 desligar <modo>"
        Write-Host "Ligados agora: $($ligados -join ', ')"
        exit 1
    }
    Write-Host "Ligando o $Modo..."
    & docker @composeArgs up -d $Modo
    Write-Host "Pronto. O boot leva 1-2 minutos; acompanhe com: docker logs -f game-craft-beta-$Modo-1"
}
else {
    if ($ligados -notcontains $Modo) {
        Write-Host "O $Modo ja esta desligado."
        exit 0
    }
    if ($Modo -eq "lobby") {
        Write-Host "Aviso: o lobby e onde os jogadores entram - sem ele, ninguem novo conecta." -ForegroundColor Yellow
    }
    Write-Host "Desligando o $Modo (nada se perde)..."
    & docker @composeArgs stop $Modo
    Write-Host "Pronto."
}
