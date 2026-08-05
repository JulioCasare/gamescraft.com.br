Add-Type -AssemblyName System.Drawing
Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

# Recorta o escudo do logo original e descarta o entorno.
# Espadas, portal, bandeiras e grama viram ruido colorido em 64px e comem o GC;
# mascarar e o que devolve legibilidade sem redesenhar a arte.

$src = $args[0]
$outDir = $args[1]

# Regiao do escudo na arte original, em fracao da imagem.
$cropX = 673
$cropY = 845
$cropSide = 2750

$img = [System.Drawing.Image]::FromFile($src)
$crop = New-Object System.Drawing.Bitmap($cropSide, $cropSide, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
$g = [System.Drawing.Graphics]::FromImage($crop)
$g.DrawImage($img, (New-Object System.Drawing.Rectangle(0, 0, $cropSide, $cropSide)), $cropX, $cropY, $cropSide, $cropSide, [System.Drawing.GraphicsUnit]::Pixel)
$g.Dispose()

# Cor de fundo amostrada do proprio logo, para o icone nao destoar da marca.
$bg = $img.GetPixel(40, 40)
$img.Dispose()
Write-Host ("Fundo amostrado: #{0:X2}{1:X2}{2:X2}" -f $bg.R, $bg.G, $bg.B)

# Contorno do escudo em fracao do recorte, seguindo a borda externa dourada.
$outline = @(
    @(0.500, 0.042), @(0.590, 0.115), @(0.622, 0.162),
    @(0.840, 0.198), @(0.888, 0.232),
    @(0.897, 0.545), @(0.872, 0.650), @(0.812, 0.742),
    @(0.716, 0.836), @(0.594, 0.918), @(0.500, 0.962),
    @(0.406, 0.918), @(0.284, 0.836), @(0.188, 0.742),
    @(0.128, 0.650), @(0.103, 0.545),
    @(0.112, 0.232), @(0.160, 0.198),
    @(0.378, 0.162), @(0.410, 0.115)
)

function Save-Masked {
    param([int]$Size, [string]$Path)

    $bmp = New-Object System.Drawing.Bitmap($Size, $Size, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
    $gfx = [System.Drawing.Graphics]::FromImage($bmp)
    $gfx.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
    $gfx.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality
    $gfx.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
    $gfx.CompositingQuality = [System.Drawing.Drawing2D.CompositingQuality]::HighQuality

    $brush = New-Object System.Drawing.SolidBrush($bg)
    $gfx.FillRectangle($brush, 0, 0, $Size, $Size)
    $brush.Dispose()

    $points = foreach ($p in $outline) {
        New-Object System.Drawing.PointF([float]($p[0] * $Size), [float]($p[1] * $Size))
    }
    # Nome distinto de $Path: o parametro e [string] e converteria o objeto.
    $shape = New-Object System.Drawing.Drawing2D.GraphicsPath
    $shape.AddPolygon([System.Drawing.PointF[]]$points)

    $gfx.SetClip($shape)
    $gfx.DrawImage($crop, (New-Object System.Drawing.Rectangle(0, 0, $Size, $Size)))
    $gfx.ResetClip()
    $shape.Dispose()
    $gfx.Dispose()

    $bmp.Save($Path, [System.Drawing.Imaging.ImageFormat]::Png)
    $bmp.Dispose()
    Write-Host "Gravado: $Path ($Size x $Size)"
}

Save-Masked -Size 512 -Path (Join-Path $outDir "shield-512.png")
Save-Masked -Size 64  -Path (Join-Path $outDir "shield-64.png")
$crop.Dispose()
