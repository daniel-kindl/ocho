# Captioned Play Store screenshots

Issue #78. Generates 6 portrait 1080×1920 frames from current production UI plus short headlines.

## Generate

```bash
pip install pillow
python tools/create-play-store-captioned-screenshots.py
```

Writes:

- `docs/play-store/screenshot-01-timer.png` — Your workout. Your timer. (`home`)
- `docs/play-store/screenshot-02-modes.png` — EMOM · Tabata · AMRAP (`home`)
- `docs/play-store/screenshot-03-setup.png` — Easy setup. Start in seconds. (`tabata-setup`)
- `docs/play-store/screenshot-04-work.png` — Glanceable while you train (`tabata-work`)
- `docs/play-store/screenshot-05-rest.png` — Know exactly when to rest (`tabata-rest`)
- `docs/play-store/screenshot-06-private.png` — Private. Offline. No account. (`finish`)

Raw captures stay in `website/public/screenshots/v3`. This script does not recapture the emulator; refresh those PNGs first if the UI changed.

`tools/update-play-store-screenshots.ps1` still produces the older uncaptioned set.
