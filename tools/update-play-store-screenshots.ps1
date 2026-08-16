param(
    [string]$RepoRoot = (Split-Path -Parent $PSScriptRoot),
    [string]$SourceDirectory = (Join-Path (Split-Path -Parent $PSScriptRoot) "website/public/screenshots/v2"),
    [string]$OutputDirectory = (Join-Path (Split-Path -Parent $PSScriptRoot) "docs/play-store")
)

$ErrorActionPreference = "Stop"
Add-Type -AssemblyName System.Drawing

New-Item -ItemType Directory -Force -Path $OutputDirectory | Out-Null

function Save-Png($bitmap, [string]$path) {
    $bitmap.Save($path, [System.Drawing.Imaging.ImageFormat]::Png)
    $bitmap.Dispose()
}

function Save-PortraitScreenshot([string]$sourceName, [string]$targetName) {
    $sourcePath = Join-Path $SourceDirectory $sourceName
    if (-not (Test-Path -LiteralPath $sourcePath)) {
        throw "Missing current website capture: $sourcePath"
    }

    $source = [System.Drawing.Image]::FromFile($sourcePath)
    $canvas = New-Object System.Drawing.Bitmap(1080, 1920, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
    $graphics = [System.Drawing.Graphics]::FromImage($canvas)
    $graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
    $graphics.Clear([System.Drawing.ColorTranslator]::FromHtml("#0C110F"))

    $scaledHeight = 1920
    $scaledWidth = [int][Math]::Round($source.Width * ($scaledHeight / $source.Height))
    $left = [int][Math]::Round((1080 - $scaledWidth) / 2)
    $graphics.DrawImage($source, $left, 0, $scaledWidth, $scaledHeight)

    $graphics.Dispose()
    $source.Dispose()
    Save-Png $canvas (Join-Path $OutputDirectory $targetName)
}

Save-PortraitScreenshot "home.png" "screenshot-home.png"
Save-PortraitScreenshot "custom-setup.png" "screenshot-setup.png"
Save-PortraitScreenshot "tabata-work.png" "screenshot-work.png"
Save-PortraitScreenshot "tabata-rest.png" "screenshot-rest.png"

$readme = @"
OCHO PLAY STORE ASSETS

Required uploads
----------------
ocho-app-icon-512.png              512 x 512 PNG
ocho-feature-graphic-1024x500.png  1024 x 500 PNG

Screenshots
-----------
Upload screenshot-home.png, screenshot-setup.png, screenshot-work.png, and
screenshot-rest.png as portrait phone screenshots. These were captured from
the current Pixel 9a emulator flow; setup uses Custom Timer and the work/rest
screens show the live phase colors. screenshot-colour-vision.png is an optional
landscape screenshot and can be omitted from the first listing.

Video
-----
ocho-store-preview.mp4 is generated separately because Play Console accepts
the video through a public or unlisted YouTube URL rather than a local upload.
"@
Set-Content -Path (Join-Path $OutputDirectory "README.txt") -Value $readme -Encoding UTF8

Write-Output "Updated Play Store screenshots in $OutputDirectory"
