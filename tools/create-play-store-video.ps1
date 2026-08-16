param(
    [Parameter(Mandatory = $true)]
    [string]$FfmpegPath,
    [string]$RepoRoot = (Split-Path -Parent $PSScriptRoot),
    [string]$OutputPath = (Join-Path (Split-Path -Parent $PSScriptRoot) "docs/play-store/ocho-store-preview.mp4")
)

$ErrorActionPreference = "Stop"
$assetRoot = Join-Path $RepoRoot "docs/play-store"
$titleCard = Join-Path $assetRoot "ocho-video-title-1080x1920.png"
$homeScreenshot = Join-Path $assetRoot "screenshot-home.png"
$setup = Join-Path $assetRoot "screenshot-setup.png"
$work = Join-Path $assetRoot "screenshot-work.png"
$rest = Join-Path $assetRoot "screenshot-rest.png"

foreach ($path in @($FfmpegPath, $titleCard, $homeScreenshot, $setup, $work, $rest)) {
    if (-not (Test-Path -LiteralPath $path)) { throw "Missing video input: $path" }
}

$filter = @"
[0:v]scale=1080:1920:flags=lanczos,setsar=1,trim=duration=4,setpts=PTS-STARTPTS,fade=t=in:st=0:d=0.25,fade=t=out:st=3.75:d=0.25[v0];
[1:v]scale=1080:1920:flags=lanczos,setsar=1,trim=duration=3,setpts=PTS-STARTPTS,fade=t=in:st=0:d=0.25,fade=t=out:st=2.75:d=0.25[v1];
[2:v]scale=1080:1920:flags=lanczos,setsar=1,trim=duration=3,setpts=PTS-STARTPTS,fade=t=in:st=0:d=0.25,fade=t=out:st=2.75:d=0.25[v2];
[3:v]scale=1080:1920:flags=lanczos,setsar=1,trim=duration=3,setpts=PTS-STARTPTS,fade=t=in:st=0:d=0.25,fade=t=out:st=2.75:d=0.25[v3];
[4:v]scale=1080:1920:flags=lanczos,setsar=1,trim=duration=3,setpts=PTS-STARTPTS,fade=t=in:st=0:d=0.25,fade=t=out:st=2.75:d=0.25[v4];
[v0][v1][v2][v3][v4]concat=n=5:v=1:a=0,format=yuv420p[v]
"@.Trim()

& $FfmpegPath -y -hide_banner -loglevel error `
    -loop 1 -t 4 -i $titleCard `
    -loop 1 -t 3 -i $homeScreenshot `
    -loop 1 -t 3 -i $setup `
    -loop 1 -t 3 -i $work `
    -loop 1 -t 3 -i $rest `
    -filter_complex $filter -map "[v]" -r 30 -c:v libx264 -preset medium -crf 20 -movflags +faststart $OutputPath

Write-Output "Created $OutputPath"
