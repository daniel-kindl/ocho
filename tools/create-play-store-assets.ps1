param(
    [string]$RepoRoot = (Split-Path -Parent $PSScriptRoot),
    [string]$OutputDirectory = (Join-Path (Split-Path -Parent $PSScriptRoot) "docs/play-store")
)

$ErrorActionPreference = "Stop"
Add-Type -AssemblyName System.Drawing

$designRoot = Join-Path $RepoRoot "ocho-design-system/design_handoff_ocho_v2"
$androidAssets = Join-Path $designRoot "assets/android"
$screenshotRoot = Join-Path $RepoRoot "docs/screenshots"
$fontRoot = Join-Path $RepoRoot "docs/fonts"

New-Item -ItemType Directory -Force -Path $OutputDirectory | Out-Null

Copy-Item (Join-Path $androidAssets "play_store_512.png") (Join-Path $OutputDirectory "ocho-app-icon-512.png") -Force

$fonts = New-Object System.Drawing.Text.PrivateFontCollection
foreach ($fontFile in @(
    (Join-Path $fontRoot "space_grotesk.ttf"),
    (Join-Path $fontRoot "ibm_plex_sans.ttf"),
    (Join-Path $fontRoot "jetbrains_mono.ttf")
)) {
    $fonts.AddFontFile($fontFile)
}

function New-BrandFont([string]$familyName, [float]$size, [System.Drawing.FontStyle]$style = [System.Drawing.FontStyle]::Regular) {
    $family = $fonts.Families | Where-Object { $_.Name -eq $familyName } | Select-Object -First 1
    if ($null -eq $family) { throw "Font family not found: $familyName" }
    return New-Object System.Drawing.Font($family, $size, $style, [System.Drawing.GraphicsUnit]::Pixel)
}

function New-Brush([string]$hex) {
    return New-Object System.Drawing.SolidBrush([System.Drawing.ColorTranslator]::FromHtml($hex))
}

function Save-Png($bitmap, [string]$path) {
    $bitmap.Save($path, [System.Drawing.Imaging.ImageFormat]::Png)
    $bitmap.Dispose()
}

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

function Fill-RoundedRect($targetGraphics, $brush, [float]$x, [float]$y, [float]$width, [float]$height, [float]$radius) {
    $path = New-RoundedPath $x $y $width $height $radius
    $targetGraphics.FillPath($brush, $path)
    $path.Dispose()
}

function Draw-RoundedImage($targetGraphics, $image, [float]$x, [float]$y, [float]$width, [float]$height, [float]$radius) {
    $path = New-RoundedPath $x $y $width $height $radius
    $state = $targetGraphics.Save()
    $targetGraphics.SetClip($path)
    $targetGraphics.DrawImage($image, $x, $y, $width, $height)
    $targetGraphics.Restore($state)
    $path.Dispose()
}

# The feature graphic deliberately stays typographic and quiet: it uses the
# existing app screens as proof of the product instead of adding store badges
# or promotional claims that would not belong in Play metadata.
$feature = New-Object System.Drawing.Bitmap(1024, 500, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
$graphics = [System.Drawing.Graphics]::FromImage($feature)
$graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
$graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
$graphics.TextRenderingHint = [System.Drawing.Text.TextRenderingHint]::AntiAliasGridFit

$bg = New-Brush "#0C110F"
$panel = New-Brush "#161C1A"
$green = New-Brush "#84BE9B"
$muted = New-Brush "#93A09A"
$ink = New-Brush "#FFFFFF"
$line = New-Brush "#38423E"
$frame = New-Brush "#080B0A"
$speaker = New-Brush "#101512"
$indicator = New-Brush "#78857D"
$graphics.FillRectangle($bg, 0, 0, 1024, 500)
$graphics.FillRectangle($panel, 0, 0, 440, 500)
$graphics.FillRectangle($green, 0, 0, 6, 500)

$eyebrowFont = New-BrandFont "JetBrains Mono" 13
$titleFont = New-BrandFont "Space Grotesk" 44 ([System.Drawing.FontStyle]::Bold)
$wordmarkFont = New-BrandFont "Space Grotesk" 72 ([System.Drawing.FontStyle]::Bold)
$bodyFont = New-BrandFont "IBM Plex Sans" 18
$smallFont = New-BrandFont "JetBrains Mono" 12
$graphics.DrawString("och", $wordmarkFont, $ink, 48, 30)
$ochWidth = [int][Math]::Round($graphics.MeasureString("och", $wordmarkFont).Width)
$graphics.DrawString("o", $wordmarkFont, $green, 48 + $ochWidth - 32, 30)
$graphics.DrawString("INTERVAL TRAINING", $eyebrowFont, $green, 52, 174)
$graphics.DrawString("Work in", $titleFont, $ink, 48, 214)
$graphics.DrawString("your rhythm.", $titleFont, $green, 48, 264)
$graphics.DrawString("EMOM  ·  TABATA  ·  AMRAP", $bodyFont, $muted, 52, 355)
$graphics.DrawString("Offline timer / no account required", $smallFont, $muted, 52, 420)

$screenFiles = @("home.png", "setup.png", "work.png")
$screenX = @(548, 690, 832)
$screenY = @(78, 46, 78)
$outerW = 188
$outerH = 384
$screenXInset = 8
$screenYInset = 15
$screenW = 172
$screenH = 352
for ($i = 0; $i -lt $screenFiles.Count; $i++) {
    $screen = [System.Drawing.Image]::FromFile((Join-Path $screenshotRoot $screenFiles[$i]))
    $x = $screenX[$i]
    $y = $screenY[$i]
    $shadow = New-Brush "#050806"
    Fill-RoundedRect $graphics $shadow ($x - 5) ($y + 5) $outerW $outerH 20
    Fill-RoundedRect $graphics $frame $x $y $outerW $outerH 20
    $framePen = New-Object System.Drawing.Pen($line, 1)
    $framePath = New-RoundedPath $x $y $outerW $outerH 20
    $graphics.DrawPath($framePen, $framePath)
    $framePath.Dispose()
    $framePen.Dispose()
    Draw-RoundedImage $graphics $screen ($x + $screenXInset) ($y + $screenYInset) $screenW $screenH 14
    Fill-RoundedRect $graphics $speaker ($x + ($outerW / 2) - 20) ($y + 6) 40 3 2
    Fill-RoundedRect $graphics $indicator ($x + ($outerW / 2) - 28) ($y + $outerH - 10) 56 3 2
    $screen.Dispose()
    $shadow.Dispose()
}

$graphics.ResetClip()
$graphics.DrawString("och", $wordmarkFont, $ink, 48, 30)
$graphics.DrawString("o", $wordmarkFont, $green, 48 + $ochWidth - 32, 30)
$graphics.DrawString("INTERVAL TRAINING", $eyebrowFont, $green, 52, 174)
$graphics.DrawString("Work in", $titleFont, $ink, 48, 214)
$graphics.DrawString("your rhythm.", $titleFont, $green, 48, 264)
$graphics.DrawString("EMOM  ·  TABATA  ·  AMRAP", $bodyFont, $muted, 52, 355)
$graphics.DrawString("Offline timer / no account required", $smallFont, $muted, 52, 420)
$graphics.DrawLine((New-Object System.Drawing.Pen($line, 1)), 48, 470, 976, 470)
$graphics.Dispose()
Save-Png $feature (Join-Path $OutputDirectory "ocho-feature-graphic-1024x500.png")

$titleCard = New-Object System.Drawing.Bitmap(1080, 1920, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
$titleGraphics = [System.Drawing.Graphics]::FromImage($titleCard)
$titleGraphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
$titleGraphics.TextRenderingHint = [System.Drawing.Text.TextRenderingHint]::AntiAliasGridFit
$titleGraphics.FillRectangle($bg, 0, 0, 1080, 1920)
$titleGraphics.FillRectangle($green, 0, 0, 10, 1920)
$titleGraphics.DrawString("och", $wordmarkFont, $ink, 64, 94)
$titleOchWidth = [int][Math]::Round($titleGraphics.MeasureString("och", $wordmarkFont).Width)
$titleGraphics.DrawString("o", $wordmarkFont, $green, 64 + $titleOchWidth - 32, 94)
$titleGraphics.DrawString("INTERVAL TRAINING", $eyebrowFont, $green, 70, 600)
$titleGraphics.DrawString("Work in", $titleFont, $ink, 64, 700)
$titleGraphics.DrawString("your rhythm.", $titleFont, $green, 64, 790)
$titleGraphics.DrawLine((New-Object System.Drawing.Pen($line, 2)), 70, 1050, 1010, 1050)
$titleGraphics.DrawString("EMOM  ·  TABATA  ·  AMRAP", $bodyFont, $muted, 70, 1110)
$titleGraphics.DrawString("Offline timer / no account required", $smallFont, $muted, 70, 1430)
$titleGraphics.DrawString("01 / 05", $smallFont, $green, 70, 1770)
$titleGraphics.Dispose()
Save-Png $titleCard (Join-Path $OutputDirectory "ocho-video-title-1080x1920.png")
foreach ($resource in @($bg, $panel, $green, $muted, $ink, $line, $frame, $speaker, $indicator, $eyebrowFont, $titleFont, $wordmarkFont, $bodyFont, $smallFont)) { $resource.Dispose() }

function Save-PortraitScreenshot([string]$sourceName, [string]$targetName) {
    $source = [System.Drawing.Image]::FromFile((Join-Path $screenshotRoot $sourceName))
    $canvas = New-Object System.Drawing.Bitmap(1080, 1920, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
    $canvasGraphics = [System.Drawing.Graphics]::FromImage($canvas)
    $canvasGraphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
    $canvasGraphics.Clear([System.Drawing.ColorTranslator]::FromHtml("#0C110F"))
    $scaledHeight = 1920
    $scaledWidth = [int][Math]::Round($source.Width * ($scaledHeight / $source.Height))
    $left = [int][Math]::Round((1080 - $scaledWidth) / 2)
    $canvasGraphics.DrawImage($source, $left, 0, $scaledWidth, $scaledHeight)
    $canvasGraphics.Dispose()
    $source.Dispose()
    Save-Png $canvas (Join-Path $OutputDirectory $targetName)
}

foreach ($name in @("home.png", "setup.png", "work.png", "rest.png")) {
    Save-PortraitScreenshot $name ("screenshot-" + $name)
}
Copy-Item (Join-Path $screenshotRoot "colour-vision.png") (Join-Path $OutputDirectory "screenshot-colour-vision.png") -Force

$readme = @"
OCHO PLAY STORE ASSETS

Required uploads
----------------
ocho-app-icon-512.png              512 x 512 PNG
ocho-feature-graphic-1024x500.png  1024 x 500 PNG

Screenshots
-----------
Upload screenshot-home.png, screenshot-setup.png, screenshot-work.png, and
screenshot-rest.png as portrait phone screenshots. screenshot-colour-vision.png
is an optional landscape screenshot and can be omitted from the first listing.

Video
-----
ocho-store-preview.mp4 is generated separately because Play Console accepts
the video through a public or unlisted YouTube URL rather than a local upload.
"@
Set-Content -Path (Join-Path $OutputDirectory "README.txt") -Value $readme -Encoding UTF8

Write-Output "Created Play Store assets in $OutputDirectory"
