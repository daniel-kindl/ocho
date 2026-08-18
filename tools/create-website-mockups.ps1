param(
    [string]$RepoRoot = (Split-Path -Parent $PSScriptRoot),
    [string]$ScreenshotDirectory = (Join-Path (Split-Path -Parent $PSScriptRoot) "website/public/screenshots/v3"),
    [string]$OutputDirectory = (Join-Path (Split-Path -Parent $PSScriptRoot) "website/public/screenshots/mockups")
)

$ErrorActionPreference = "Stop"
Add-Type -AssemblyName System.Drawing

New-Item -ItemType Directory -Force -Path $OutputDirectory | Out-Null

function New-RoundedPath([float]$x, [float]$y, [float]$width, [float]$height, [float]$radius) {
    $diameter = $radius * 2
    $path = New-Object System.Drawing.Drawing2D.GraphicsPath
    $path.AddArc($x, $y, $diameter, $diameter, 180, 90)
    $path.AddArc($x + $width - $diameter, $y, $diameter, $diameter, 270, 90)
    $path.AddArc($x + $width - $diameter, $y + $height - $diameter, $diameter, $diameter, 0, 90)
    $path.AddArc($x, $y + $height - $diameter, $diameter, $diameter, 90, 90)
    $path.CloseFigure()
    return $path
}

function Save-PhoneMockup([System.Drawing.Image]$source, [string]$outputPath) {
    $canvas = New-Object System.Drawing.Bitmap(420, 740, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
    $graphics = [System.Drawing.Graphics]::FromImage($canvas)
    $graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
    $graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
    $graphics.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality

    $outerX = 60
    $outerWidth = 300
    $screenX = 72
    $screenWidth = 276
    $screenHeight = [int][Math]::Round($source.Height * ($screenWidth / $source.Width))
    $outerHeight = 4 + 40 + 10 + 10 + $screenHeight
    $outerY = [int][Math]::Round((740 - $outerHeight) / 2)
    $screenY = $outerY + 42

    $shellBrush = New-Object System.Drawing.SolidBrush([System.Drawing.ColorTranslator]::FromHtml("#080B0A"))
    $borderPen = New-Object System.Drawing.Pen([System.Drawing.ColorTranslator]::FromHtml("#4B5A52"), 2)
    $speakerBrush = New-Object System.Drawing.SolidBrush([System.Drawing.ColorTranslator]::FromHtml("#101512"))

    $shellPath = New-RoundedPath $outerX $outerY $outerWidth $outerHeight 36
    $graphics.FillPath($shellBrush, $shellPath)
    $graphics.DrawPath($borderPen, $shellPath)
    $shellPath.Dispose()

    $screenPath = New-RoundedPath $screenX $screenY $screenWidth $screenHeight 25
    $state = $graphics.Save()
    $graphics.SetClip($screenPath)
    $graphics.DrawImage($source, $screenX, $screenY, $screenWidth, $screenHeight)
    $graphics.Restore($state)
    $screenPath.Dispose()

    $graphics.FillRoundedRectangle($speakerBrush, $outerX + 121, $outerY + 15, 58, 5, 2)

    $graphics.Dispose()
    $shellBrush.Dispose()
    $borderPen.Dispose()
    $speakerBrush.Dispose()
    $canvas.Save($outputPath, [System.Drawing.Imaging.ImageFormat]::Png)
    $canvas.Dispose()
}

# System.Drawing.Graphics has no built-in rounded-rectangle fill method. Add the
# small helper once so the generated assets stay raster-only and match the CSS shell.
Update-TypeData -TypeName System.Drawing.Graphics -MemberName FillRoundedRectangle -MemberType ScriptMethod -Value {
    param($brush, $x, $y, $width, $height, $radius)
    $path = New-RoundedPath $x $y $width $height $radius
    $this.FillPath($brush, $path)
    $path.Dispose()
} -Force

foreach ($sourceFile in Get-ChildItem -LiteralPath $ScreenshotDirectory -Filter *.png -File) {
    $source = [System.Drawing.Image]::FromFile($sourceFile.FullName)
    try {
        Save-PhoneMockup $source (Join-Path $OutputDirectory $sourceFile.Name)
    } finally {
        $source.Dispose()
    }
}

# Keep the newer preset captures available as portable raster assets for README
# renderers, which do not reliably resolve relative image references embedded in SVGs.
$presetScreenshotDirectory = Join-Path $RepoRoot "website/public/screenshots/v4"
foreach ($sourceFile in Get-ChildItem -LiteralPath $presetScreenshotDirectory -Filter *.png -File) {
    $source = [System.Drawing.Image]::FromFile($sourceFile.FullName)
    try {
        Save-PhoneMockup $source (Join-Path $OutputDirectory $sourceFile.Name)
    } finally {
        $source.Dispose()
    }
}

Write-Output "Created website phone mockups in $OutputDirectory"
