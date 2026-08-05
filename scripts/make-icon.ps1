Add-Type -AssemblyName System.Drawing
Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

# Arte autorada em grade logica de 32x32 e ampliada por multiplo inteiro.
# Ampliar por multiplo exato mantem a borda dura do pixel art.
$G = 32

$C = @{
    outline   = [System.Drawing.Color]::FromArgb(255, 11, 16, 32)
    goldDark  = [System.Drawing.Color]::FromArgb(255, 150, 100, 10)
    gold      = [System.Drawing.Color]::FromArgb(255, 226, 172, 36)
    goldLight = [System.Drawing.Color]::FromArgb(255, 255, 219, 96)
    blueDark  = [System.Drawing.Color]::FromArgb(255, 24, 42, 104)
    blueSeam  = [System.Drawing.Color]::FromArgb(255, 34, 62, 140)
    blue      = [System.Drawing.Color]::FromArgb(255, 42, 78, 168)
    blueLight = [System.Drawing.Color]::FromArgb(255, 64, 108, 206)
    gemDark   = [System.Drawing.Color]::FromArgb(255, 26, 120, 152)
    gem       = [System.Drawing.Color]::FromArgb(255, 62, 200, 224)
    gemLight  = [System.Drawing.Color]::FromArgb(255, 156, 244, 252)
}

# Escudo tipo heater: ombros retos no topo e afunilamento continuo ate a ponta,
# como no logo original.
$half = @(
    0, 0,
    11, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12,
    12, 11, 11, 10, 9, 8, 7, 6, 4, 2,
    0, 0, 0, 0
)

$grid = New-Object 'string[,]' $G, $G
for ($y = 0; $y -lt $G; $y++) {
    for ($x = 0; $x -lt $G; $x++) { $grid[$x, $y] = "none" }
}

$cx = 15.5
for ($y = 0; $y -lt $G; $y++) {
    $h = $half[$y]
    if ($h -le 0) { continue }
    $left = [int][Math]::Floor($cx - $h + 0.5)
    $right = [int][Math]::Floor($cx + $h - 0.5)
    $nextH = if ($y + 1 -lt $G) { $half[$y + 1] } else { 0 }

    for ($x = $left; $x -le $right; $x++) {
        $depth = [Math]::Min([Math]::Min($x - $left, $right - $x), $y - 2)
        if ($nextH -le 0) { $depth = 0 }

        if ($depth -le 0) {
            $grid[$x, $y] = "outline"
        }
        elseif ($depth -le 2) {
            # Moldura dourada, com a luz vindo de cima e da esquerda.
            $tone = if ($y -le 4 -or ($x - $left) -le 2) { "goldLight" }
                    elseif ($y -ge 21 -or ($right - $x) -le 2) { "goldDark" }
                    else { "gold" }
            # Sulcos regulares: a moldura do logo e feita de blocos, nao lisa.
            if (($x % 4 -eq 0) -or ($y % 4 -eq 0)) {
                $tone = switch ($tone) {
                    "goldLight" { "gold" }
                    "gold"      { "goldDark" }
                    default     { "goldDark" }
                }
            }
            $grid[$x, $y] = $tone
        }
        else {
            # Emendas das tabuas: contraste baixo de proposito. Escuro demais
            # em 64px vira grade vertical em vez de textura.
            if ($y -ge 22) { $grid[$x, $y] = "blueDark" }
            elseif ((($x - 3) % 5) -eq 0) { $grid[$x, $y] = "blueSeam" }
            elseif ($y -le 5) { $grid[$x, $y] = "blueLight" }
            else { $grid[$x, $y] = "blue" }
        }
    }
}

# Gema no topo da moldura, o detalhe mais reconhecivel do logo original.
$gem = @(
    ".##.",
    "####",
    ".##."
)
function Draw-Gem {
    param($rows, [int]$ox, [int]$oy)
    for ($r = 0; $r -lt $rows.Count; $r++) {
        $line = $rows[$r]
        for ($c = 0; $c -lt $line.Length; $c++) {
            if ($line[$c] -ne '#') { continue }
            $x = $ox + $c
            $y = $oy + $r
            if ($x -lt 0 -or $x -ge $G -or $y -lt 0 -or $y -ge $G) { continue }
            $grid[$x, $y] = if ($r -eq 0) { "gemLight" }
                            elseif ($r -ge 2) { "gemDark" }
                            else { "gem" }
        }
    }
}
# Uma linha abaixo do topo: em y=2 a gema apagaria o contorno do escudo.
Draw-Gem -rows $gem -ox 14 -oy 3

$letterG = @(
    ".####.",
    "##..##",
    "##....",
    "##....",
    "##.###",
    "##..##",
    "##..##",
    "##..##",
    ".####."
)
$letterC = @(
    ".####.",
    "##..##",
    "##....",
    "##....",
    "##....",
    "##....",
    "##....",
    "##..##",
    ".####."
)

function Draw-Letter {
    param($rows, [int]$ox, [int]$oy)
    # Sombra dura embaixo e a direita destaca a letra do campo azul.
    for ($r = 0; $r -lt $rows.Count; $r++) {
        $line = $rows[$r]
        for ($c = 0; $c -lt $line.Length; $c++) {
            if ($line[$c] -ne '#') { continue }
            $sx = $ox + $c + 1
            $sy = $oy + $r + 1
            if ($sx -ge $G -or $sy -ge $G) { continue }
            if ($grid[$sx, $sy] -notmatch '^gold') { $grid[$sx, $sy] = "outline" }
        }
    }
    for ($r = 0; $r -lt $rows.Count; $r++) {
        $line = $rows[$r]
        for ($c = 0; $c -lt $line.Length; $c++) {
            if ($line[$c] -ne '#') { continue }
            $x = $ox + $c
            $y = $oy + $r
            if ($x -lt 0 -or $x -ge $G -or $y -lt 0 -or $y -ge $G) { continue }
            $grid[$x, $y] = if ($r -le 1) { "goldLight" } elseif ($r -ge 7) { "goldDark" } else { "gold" }
        }
    }
}

# 6 + 3 de folga + 6 = 15 de largura, centrado no eixo do escudo.
Draw-Letter -rows $letterG -ox 8 -oy 10
Draw-Letter -rows $letterC -ox 17 -oy 10

function Save-Icon {
    param([int]$Size, [string]$Path)
    $scale = $Size / $G
    $bmp = New-Object System.Drawing.Bitmap($Size, $Size, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
    $gfx = [System.Drawing.Graphics]::FromImage($bmp)
    $gfx.Clear([System.Drawing.Color]::Transparent)
    for ($y = 0; $y -lt $G; $y++) {
        for ($x = 0; $x -lt $G; $x++) {
            $key = $grid[$x, $y]
            if ($key -eq "none") { continue }
            $brush = New-Object System.Drawing.SolidBrush($C[$key])
            $gfx.FillRectangle($brush, $x * $scale, $y * $scale, $scale, $scale)
            $brush.Dispose()
        }
    }
    $gfx.Dispose()
    $bmp.Save($Path, [System.Drawing.Imaging.ImageFormat]::Png)
    $bmp.Dispose()
    Write-Host "Gravado: $Path ($Size x $Size)"
}

$outDir = $args[0]
Save-Icon -Size 64 -Path (Join-Path $outDir "server-icon.png")
Save-Icon -Size 512 -Path (Join-Path $outDir "icon-512.png")
