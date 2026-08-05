Add-Type -AssemblyName System.Drawing
Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

# Arte autorada em grade logica de 32x32 e ampliada por blocos inteiros.
# Ampliar por multiplo exato mantem a borda dura do pixel art.
$G = 32

$C = @{
    none      = $null
    outline   = [System.Drawing.Color]::FromArgb(255, 11, 16, 32)
    goldDark  = [System.Drawing.Color]::FromArgb(255, 168, 115, 12)
    gold      = [System.Drawing.Color]::FromArgb(255, 232, 182, 42)
    goldLight = [System.Drawing.Color]::FromArgb(255, 255, 217, 90)
    blueDark  = [System.Drawing.Color]::FromArgb(255, 22, 37, 94)
    blue      = [System.Drawing.Color]::FromArgb(255, 42, 74, 158)
    blueLight = [System.Drawing.Color]::FromArgb(255, 62, 104, 200)
}

# Meia-largura do escudo por linha: reto em cima, afunilando ate a ponta.
$half = @(0,0) + (@(12) * 18) + @(12,11,11,10,9,8,7,6,5,4,2) + @(0,0)

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

    for ($x = $left; $x -le $right; $x++) {
        # Distancia ate a borda do escudo, para separar moldura de campo interno.
        $depthX = [Math]::Min($x - $left, $right - $x)
        $depthY = $y - 2
        $nextH = if ($y + 1 -lt $G) { $half[$y + 1] } else { 0 }
        $depthBottom = if ($nextH -le 0) { 0 } else { 99 }
        $depth = [Math]::Min($depthX, $depthY)
        $depth = [Math]::Min($depth, $depthBottom)

        if ($depth -le 0) {
            $grid[$x, $y] = "outline"
        }
        elseif ($depth -le 2) {
            # Luz vem de cima e da esquerda.
            if ($y -le 4 -or ($x - $left) -le 2) { $grid[$x, $y] = "goldLight" }
            elseif ($y -ge 22 -or ($right - $x) -le 2) { $grid[$x, $y] = "goldDark" }
            else { $grid[$x, $y] = "gold" }
        }
        else {
            # A troca de tom so acontece dentro do afunilamento. Em linha cheia
            # ela viraria uma emenda horizontal atravessando o escudo.
            if ($y -lt 23) { $grid[$x, $y] = "blue" }
            else { $grid[$x, $y] = "blueDark" }
        }
    }
}

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
    for ($r = 0; $r -lt $rows.Count; $r++) {
        $line = $rows[$r]
        for ($c = 0; $c -lt $line.Length; $c++) {
            if ($line[$c] -ne '#') { continue }
            $x = $ox + $c
            $y = $oy + $r
            if ($x -lt 0 -or $x -ge $G -or $y -lt 0 -or $y -ge $G) { continue }
            # Sombra dura embaixo e a direita destaca a letra do campo azul.
            $sx = $x + 1
            $sy = $y + 1
            if ($sx -lt $G -and $sy -lt $G -and $grid[$sx, $sy] -notmatch '^gold') {
                $grid[$sx, $sy] = "outline"
            }
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

# 6 + 3 de folga + 6 = 15 de largura, centrado no eixo do escudo (x 8..22).
# Verticalmente um pouco acima do centro geometrico: a ponta inferior puxa
# o olhar para baixo e compensa.
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
