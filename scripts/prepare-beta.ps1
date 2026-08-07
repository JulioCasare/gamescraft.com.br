[CmdletBinding()]
param(
    [string]$PublicHost = "localhost",
    [string]$MinecraftVersion = "26.2"
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$repositoryRoot = Split-Path -Parent $PSScriptRoot
$infraRoot = Join-Path $repositoryRoot "infra"
$runtimeRoot = Join-Path $infraRoot "runtime"

function Write-Utf8NoBom {
    param(
        [Parameter(Mandatory = $true)][string]$Path,
        [Parameter(Mandatory = $true)][string]$Content
    )

    $encoding = [System.Text.UTF8Encoding]::new($false)
    [System.IO.File]::WriteAllText($Path, $Content, $encoding)
}

function New-RandomSecret {
    $bytes = New-Object byte[] 36
    [System.Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($bytes)
    return [Convert]::ToBase64String($bytes).Replace("+", "-").Replace("/", "_").TrimEnd("=")
}

function Copy-Template {
    param(
        [Parameter(Mandatory = $true)][string]$TemplatePath,
        [Parameter(Mandatory = $true)][string]$DestinationPath,
        [Parameter(Mandatory = $true)][string]$VelocitySecret
    )

    $content = [System.IO.File]::ReadAllText($TemplatePath)
    $content = $content.Replace("__VELOCITY_SECRET__", $VelocitySecret)
    Write-Utf8NoBom -Path $DestinationPath -Content $content
}

New-Item -ItemType Directory -Force -Path $runtimeRoot | Out-Null

$environmentPath = Join-Path $infraRoot ".env"
if (Test-Path -LiteralPath $environmentPath) {
    $environmentText = [System.IO.File]::ReadAllText($environmentPath)
    $existingSecret = [regex]::Match($environmentText, "(?m)^VELOCITY_SECRET=(.+)$").Groups[1].Value.Trim()
    if ([string]::IsNullOrWhiteSpace($existingSecret) -or $existingSecret -eq "REPLACE_ME") {
        $velocitySecret = New-RandomSecret
    }
    else {
        $velocitySecret = $existingSecret
    }
}
else {
    $velocitySecret = New-RandomSecret
    $databasePassword = New-RandomSecret
    $databaseRootPassword = New-RandomSecret
    $environmentText = [System.IO.File]::ReadAllText((Join-Path $infraRoot ".env.example"))
    $environmentText = $environmentText.Replace("PUBLIC_HOST=localhost", "PUBLIC_HOST=$PublicHost")
    $environmentText = $environmentText.Replace("MINECRAFT_VERSION=26.2", "MINECRAFT_VERSION=$MinecraftVersion")
    $environmentText = $environmentText.Replace("VELOCITY_SECRET=REPLACE_ME", "VELOCITY_SECRET=$velocitySecret")
    $environmentText = $environmentText.Replace("DATABASE_PASSWORD=REPLACE_ME", "DATABASE_PASSWORD=$databasePassword")
    $environmentText = $environmentText.Replace("DATABASE_ROOT_PASSWORD=REPLACE_ME", "DATABASE_ROOT_PASSWORD=$databaseRootPassword")
    Write-Utf8NoBom -Path $environmentPath -Content $environmentText
}

$proxyConfig = Join-Path $runtimeRoot "proxy-config"
New-Item -ItemType Directory -Force -Path $proxyConfig | Out-Null
Copy-Template `
    -TemplatePath (Join-Path $infraRoot "config\velocity\velocity.toml.template") `
    -DestinationPath (Join-Path $proxyConfig "velocity.toml") `
    -VelocitySecret $velocitySecret
Write-Utf8NoBom -Path (Join-Path $proxyConfig "forwarding.secret") -Content $velocitySecret

$paperTemplate = Join-Path $infraRoot "config\paper\paper-global.yml.template"
foreach ($serverName in @("lobby", "bedwars", "pillars", "buildbattle", "ctf", "pvp", "eventos")) {
    $paperConfig = Join-Path $runtimeRoot "$serverName\config"
    New-Item -ItemType Directory -Force -Path $paperConfig | Out-Null
    Copy-Template `
        -TemplatePath $paperTemplate `
        -DestinationPath (Join-Path $paperConfig "paper-global.yml") `
        -VelocitySecret $velocitySecret
}

New-Item -ItemType Directory -Force -Path (Join-Path $runtimeRoot "proxy") | Out-Null
New-Item -ItemType Directory -Force -Path (Join-Path $runtimeRoot "mariadb") | Out-Null

# Icone da lista de servidores. O Velocity le server-icon.png do proprio
# diretorio e exige exatamente 64x64; fora disso ele ignora o arquivo.
$serverIcon = Join-Path $infraRoot "branding\server-icon.png"
if (Test-Path -LiteralPath $serverIcon) {
    Copy-Item -LiteralPath $serverIcon -Destination (Join-Path $runtimeRoot "proxy\server-icon.png") -Force
    Write-Host "Icone do servidor aplicado."
}

Write-Host ""
Write-Host "Ambiente beta preparado." -ForegroundColor Green
Write-Host "Host configurado: $PublicHost"
Write-Host "Versao Java configurada: $MinecraftVersion"
Write-Host "Segredos foram gravados apenas em infra/.env e infra/runtime/ (ignorados pelo Git)."
Write-Host "Inicie com: docker compose --env-file infra/.env -f infra/compose.yaml up -d"
