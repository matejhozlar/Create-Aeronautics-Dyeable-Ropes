"""Regenerate 16 dyed rope item textures from Simulated's rope_coupling.png.

Dye RGBs are read at runtime from the decompiled `net.minecraft.world.item.DyeColor`
that NeoForge's moddev plugin caches under `~/.gradle/caches/ng_execute/`. That keeps
the colors in lock-step with whatever Minecraft version the project compiles against,
with no risk of drifting from the actual `DyeColor.getTextureDiffuseColor()` values.

The source rope texture is a dark brown, so naive luma-multiply produces muddy
colors. We min/max-normalize the source luma to [0, 1] before multiplying by the
dye RGB, with a small floor so shadows keep some color rather than crushing to
pure black.

Run from the project root:
    python scripts/recolor_ropes.py
"""

from __future__ import annotations

import argparse
import re
import sys
from pathlib import Path

from PIL import Image

# Fallback values mirrored from net.minecraft.world.item.DyeColor for environments
# without a NeoForge cache (e.g. CI). The live values from the cache take precedence.
FALLBACK_DYE_COLORS: dict[str, int] = {
    "white":      16383998,
    "orange":     16351261,
    "magenta":    13061821,
    "light_blue": 3847130,
    "yellow":     16701501,
    "lime":       8439583,
    "pink":       15961002,
    "gray":       4673362,
    "light_gray": 10329495,
    "cyan":       1481884,
    "purple":     8991416,
    "blue":       3949738,
    "brown":      8606770,
    "green":      6192150,
    "red":        11546150,
    "black":      1908001,
}

# Pattern matches lines like: WHITE(0, "white", 16383998, MapColor.SNOW, ...
DYE_ENUM_RE = re.compile(
    r"^\s*(?P<enum>[A-Z_]+)\(\s*\d+\s*,\s*\"(?P<name>\w+)\"\s*,\s*(?P<color>\d+)\s*,",
    re.MULTILINE,
)


def find_dye_color_source() -> Path | None:
    """Locate the newest decompiled DyeColor.java in the NeoForge moddev cache."""
    cache_root = Path.home() / ".gradle" / "caches" / "ng_execute"
    if not cache_root.is_dir():
        return None
    candidates = list(cache_root.glob("*/output/net/minecraft/world/item/DyeColor.java"))
    if not candidates:
        return None
    return max(candidates, key=lambda p: p.stat().st_mtime)


def parse_dye_colors(source: Path) -> dict[str, int]:
    text = source.read_text(encoding="utf-8")
    colors: dict[str, int] = {}
    for match in DYE_ENUM_RE.finditer(text):
        colors[match.group("name")] = int(match.group("color"))
    return colors


def load_dye_colors(verbose: bool = True) -> dict[str, tuple[int, int, int]]:
    source = find_dye_color_source()
    if source is not None:
        try:
            parsed = parse_dye_colors(source)
        except Exception as exc:
            print(f"warning: failed to parse {source} ({exc}); using fallback", file=sys.stderr)
            parsed = FALLBACK_DYE_COLORS
        else:
            if len(parsed) != len(FALLBACK_DYE_COLORS):
                print(
                    f"warning: parsed only {len(parsed)} entries from {source}; using fallback",
                    file=sys.stderr,
                )
                parsed = FALLBACK_DYE_COLORS
            elif verbose:
                print(f"using DyeColor RGBs from {source}")
    else:
        if verbose:
            print("no NeoForge cache found; using fallback DyeColor RGBs")
        parsed = FALLBACK_DYE_COLORS
    return {name: ((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF) for name, rgb in parsed.items()}


def normalize_luma(source: Image.Image) -> tuple[Image.Image, float, float]:
    """Return (alpha-preserving normalized-luma image, min_luma, max_luma).

    The returned image is RGBA where R=G=B=normalized luma in [0..255] and A is the
    source alpha. Fully transparent pixels are excluded from min/max sampling so a
    sprite on a transparent background doesn't collapse the dynamic range.
    """
    src = source.convert("RGBA")
    width, height = src.size
    pixels = src.load()

    min_luma = 255.0
    max_luma = 0.0
    for y in range(height):
        for x in range(width):
            r, g, b, a = pixels[x, y]
            if a == 0:
                continue
            luma = 0.299 * r + 0.587 * g + 0.114 * b
            if luma < min_luma:
                min_luma = luma
            if luma > max_luma:
                max_luma = luma

    if max_luma <= min_luma:
        max_luma = min_luma + 1.0

    out = Image.new("RGBA", src.size)
    out_pixels = out.load()
    span = max_luma - min_luma
    for y in range(height):
        for x in range(width):
            r, g, b, a = pixels[x, y]
            if a == 0:
                out_pixels[x, y] = (0, 0, 0, 0)
                continue
            luma = 0.299 * r + 0.587 * g + 0.114 * b
            t = (luma - min_luma) / span
            channel = int(round(t * 255))
            out_pixels[x, y] = (channel, channel, channel, a)
    return out, min_luma, max_luma


def tint(normalized: Image.Image, rgb: tuple[int, int, int], floor: float) -> Image.Image:
    """Multiply a normalized-luma image by `rgb`, with a configurable shadow floor."""
    r_mul, g_mul, b_mul = (c / 255.0 for c in rgb)
    width, height = normalized.size
    pixels = normalized.load()
    out = Image.new("RGBA", normalized.size)
    out_pixels = out.load()

    for y in range(height):
        for x in range(width):
            l, _, _, a = pixels[x, y]
            if a == 0:
                out_pixels[x, y] = (0, 0, 0, 0)
                continue
            t = (l / 255.0) * (1.0 - floor) + floor
            out_pixels[x, y] = (
                min(255, int(round(t * r_mul * 255))),
                min(255, int(round(t * g_mul * 255))),
                min(255, int(round(t * b_mul * 255))),
                a,
            )
    return out


SIMULATED_ASSETS = Path(
    "../Simulated-Project/simulated/common/src/main/resources/assets/simulated"
)

# Source path -> output path (relative to project root). The luma is normalized
# AND lifted by GREYSCALE_FLOOR so the darkest source pixels do not become pure
# black; otherwise dye_color * 0 stays 0 at render time, producing a harsh black
# pattern over every tinted rope.
GREYSCALE_BLOCK_TEXTURES: dict[Path, Path] = {
    SIMULATED_ASSETS / "textures/block/rope_particle.png":
        Path("src/main/resources/assets/dyeable_ropes/textures/block/rope_particle_greyscale.png"),
    SIMULATED_ASSETS / "textures/block/rope_winch/winch.png":
        Path("src/main/resources/assets/dyeable_ropes/textures/block/rope_winch/winch_greyscale.png"),
    SIMULATED_ASSETS / "textures/block/rope_winch/winch_coil.png":
        Path("src/main/resources/assets/dyeable_ropes/textures/block/rope_winch/winch_coil_greyscale.png"),
}

GREYSCALE_FLOOR = 0.5


def write_greyscale(source_path: Path, target: Path, floor: float = GREYSCALE_FLOOR) -> None:
    if not source_path.is_file():
        print(f"warning: greyscale source not found: {source_path}", file=sys.stderr)
        return
    normalized, lo, hi = normalize_luma(Image.open(source_path))
    if floor > 0.0:
        pixels = normalized.load()
        for y in range(normalized.height):
            for x in range(normalized.width):
                r, g, b, a = pixels[x, y]
                if a == 0:
                    continue
                lifted = int(round(floor * 255 + (1.0 - floor) * r))
                pixels[x, y] = (lifted, lifted, lifted, a)
    target.parent.mkdir(parents=True, exist_ok=True)
    normalized.save(target)
    print(f"wrote {target} (luma [{lo:.1f}, {hi:.1f}], floor={floor})")


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "--source",
        type=Path,
        default=SIMULATED_ASSETS / "textures/item/rope_coupling.png",
        help="Path to Simulated's rope_coupling.png (relative to the project root).",
    )
    parser.add_argument(
        "--out",
        type=Path,
        default=Path("src/main/resources/assets/dyeable_ropes/textures/item"),
        help="Output directory for per-color item PNGs.",
    )
    parser.add_argument(
        "--floor",
        type=float,
        default=0.15,
        help="Shadow floor in [0, 1]. 0 = darkest pixels become pure black; "
             "higher values keep more color in shadows. Default 0.15.",
    )
    args = parser.parse_args()

    src_path: Path = args.source
    if not src_path.is_file():
        print(f"Source not found: {src_path}", file=sys.stderr)
        return 1

    dye_colors = load_dye_colors()

    out_dir: Path = args.out
    out_dir.mkdir(parents=True, exist_ok=True)

    source = Image.open(src_path)
    normalized, min_luma, max_luma = normalize_luma(source)
    print(f"source luma range: [{min_luma:.1f}, {max_luma:.1f}]")

    for color_name, rgb in dye_colors.items():
        recolored = tint(normalized, rgb, args.floor)
        target = out_dir / f"{color_name}_rope.png"
        recolored.save(target)
        print(f"wrote {target}")

    for source_block_path, target_block_path in GREYSCALE_BLOCK_TEXTURES.items():
        write_greyscale(source_block_path, target_block_path)

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
