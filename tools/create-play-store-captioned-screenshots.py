#!/usr/bin/env python3
"""Compose 1080x1920 Play Store frames with short headlines over live Ocho UI."""

from __future__ import annotations

import argparse
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

CANVAS_W = 1080
CANVAS_H = 1920
BG = (12, 17, 15, 255)
FG = (236, 242, 238, 255)
MUTED = (168, 184, 176, 255)
FRAME = (75, 90, 82, 255)
SHELL = (8, 11, 10, 255)

FRAMES = (
    {
        "source": "website/public/screenshots/v3/home.png",
        "out": "docs/play-store/screenshot-01-timer.png",
        "title": "Your workout.\nYour timer.",
    },
    {
        "source": "website/public/screenshots/v3/home.png",
        "out": "docs/play-store/screenshot-02-modes.png",
        "title": "EMOM \u00b7 Tabata \u00b7 AMRAP",
    },
    {
        "source": "website/public/screenshots/v3/tabata-setup.png",
        "out": "docs/play-store/screenshot-03-setup.png",
        "title": "Easy setup.\nStart in seconds.",
    },
    {
        "source": "website/public/screenshots/v3/tabata-work.png",
        "out": "docs/play-store/screenshot-04-work.png",
        "title": "Glanceable\nwhile you train",
    },
    {
        "source": "website/public/screenshots/v3/tabata-rest.png",
        "out": "docs/play-store/screenshot-05-rest.png",
        "title": "Know exactly\nwhen to rest",
    },
    {
        "source": "website/public/screenshots/v3/finish.png",
        "out": "docs/play-store/screenshot-06-private.png",
        "title": "Private. Offline.\nNo account.",
    },
)


def load_font(size: int) -> ImageFont.FreeTypeFont | ImageFont.ImageFont:
    candidates = (
        "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf",
        "/usr/share/fonts/truetype/liberation/LiberationSans-Bold.ttf",
        "C:\\Windows\\Fonts\\segoeuib.ttf",
        "C:\\Windows\\Fonts\\arialbd.ttf",
        "/System/Library/Fonts/Supplemental/Arial Bold.ttf",
    )
    for path in candidates:
        if Path(path).exists():
            return ImageFont.truetype(path, size=size)
    return ImageFont.load_default()


def rounded_mask(size: tuple[int, int], radius: int) -> Image.Image:
    mask = Image.new("L", size, 0)
    draw = ImageDraw.Draw(mask)
    draw.rounded_rectangle((0, 0, size[0] - 1, size[1] - 1), radius=radius, fill=255)
    return mask


def draw_centered_text(
    draw: ImageDraw.ImageDraw,
    text: str,
    top: int,
    font: ImageFont.ImageFont,
    fill: tuple[int, int, int, int],
) -> int:
    y = top
    for line in text.split("\n"):
        bbox = draw.textbbox((0, 0), line, font=font)
        w = bbox[2] - bbox[0]
        h = bbox[3] - bbox[1]
        draw.text(((CANVAS_W - w) / 2, y), line, font=font, fill=fill)
        y += h + 12
    return y


def compose(repo: Path, spec: dict[str, str], font: ImageFont.ImageFont) -> Path:
    source = Image.open(repo / spec["source"]).convert("RGBA")
    canvas = Image.new("RGBA", (CANVAS_W, CANVAS_H), BG)
    draw = ImageDraw.Draw(canvas)

    title_bottom = draw_centered_text(draw, spec["title"], 96, font, FG)
    subtitle_y = title_bottom + 8
    draw_centered_text(draw, "OCHO", subtitle_y, load_font(28), MUTED)

    max_w = 780
    max_h = 1380
    scale = min(max_w / source.width, max_h / source.height)
    phone_w = int(source.width * scale)
    phone_h = int(source.height * scale)
    phone = source.resize((phone_w, phone_h), Image.Resampling.LANCZOS)

    pad = 18
    shell_w = phone_w + pad * 2
    shell_h = phone_h + pad * 2 + 22
    shell = Image.new("RGBA", (shell_w, shell_h), (0, 0, 0, 0))
    shell_draw = ImageDraw.Draw(shell)
    shell_draw.rounded_rectangle((0, 0, shell_w - 1, shell_h - 1), radius=48, fill=SHELL, outline=FRAME, width=3)
    shell_draw.rounded_rectangle((shell_w / 2 - 40, 14, shell_w / 2 + 40, 24), radius=4, fill=(16, 21, 18, 255))

    clipped = Image.new("RGBA", phone.size, (0, 0, 0, 0))
    clipped.paste(phone, (0, 0), rounded_mask(phone.size, 36))
    shell.paste(clipped, (pad, 34), clipped)

    x = (CANVAS_W - shell_w) // 2
    y = max(360, (CANVAS_H - shell_h) // 2 + 40)
    canvas.paste(shell, (x, y), shell)

    out = repo / spec["out"]
    out.parent.mkdir(parents=True, exist_ok=True)
    canvas.convert("RGB").save(out, "PNG", optimize=True)
    return out


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "--repo",
        type=Path,
        default=Path(__file__).resolve().parents[1],
        help="Repository root",
    )
    args = parser.parse_args()

    font = load_font(64)
    written = [compose(args.repo, spec, font) for spec in FRAMES]
    for path in written:
        print(path)


if __name__ == "__main__":
    main()
