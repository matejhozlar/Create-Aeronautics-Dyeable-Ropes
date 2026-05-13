"""Generate per-color item model JSONs, shapeless dye recipes, the en_us lang map,
and derived greyscale rope block-model JSONs.

The block-model JSONs are cloned from Simulated's source models, with the rope
particle texture reference rewritten to point at our locally-generated greyscale
PNG. Because Simulated's assets are All Rights Reserved, the outputs land in a
gitignored directory and only exist on developer machines that have the Simulated
source checked out next door.

Run from the project root:
    python scripts/generate_jsons.py
"""

from __future__ import annotations

import json
import sys
from pathlib import Path

COLORS: list[str] = [
    "white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray",
    "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black",
]

MOD_ID = "dyeable_ropes"
ROPE_INPUT = "simulated:rope_coupling"

ROOT = Path(__file__).resolve().parent.parent
MODELS_DIR = ROOT / "src/main/resources/assets" / MOD_ID / "models/item"
BLOCK_MODELS_DIR = ROOT / "src/main/resources/assets" / MOD_ID / "models/block"
RECIPES_DIR = ROOT / "src/main/resources/data" / MOD_ID / "recipe"
LANG_FILE = ROOT / "src/main/resources/assets" / MOD_ID / "lang/en_us.json"

SIMULATED_MODELS = ROOT / "../Simulated-Project/simulated/common/src/main/resources/assets/simulated/models"

# Map of (Simulated source path -> our derived target path). All paths are
# relative to Simulated's `models/` dir on the left and our `models/block/` dir
# on the right.
DERIVED_ROPE_MODELS: dict[str, str] = {
    "block/rope/rope.json": "rope/rope_greyscale.json",
    "block/rope/knot.json": "rope/knot_greyscale.json",
    "block/rope_connector/knot.json": "rope_connector/knot_greyscale.json",
}

# Rewrite any texture reference from this prefix to our greyscale prefix.
TEXTURE_REWRITES: dict[str, str] = {
    "simulated:block/rope_particle": f"{MOD_ID}:block/rope_particle_greyscale",
    "simulated:block/rope_winch/winch": f"{MOD_ID}:block/rope_winch/winch_greyscale",
}


def title(color: str) -> str:
    return " ".join(part.capitalize() for part in color.split("_"))


def write_json(path: Path, payload: dict) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(payload, indent=2) + "\n", encoding="utf-8", newline="\n")


def rewrite_textures(payload: dict) -> None:
    textures = payload.get("textures")
    if isinstance(textures, dict):
        for key, value in list(textures.items()):
            if isinstance(value, str) and value in TEXTURE_REWRITES:
                textures[key] = TEXTURE_REWRITES[value]


def write_derived_block_models() -> int:
    if not SIMULATED_MODELS.is_dir():
        print(
            f"warning: Simulated models dir not found at {SIMULATED_MODELS}; "
            "skipping greyscale block models",
            file=sys.stderr,
        )
        return 0
    written = 0
    for source_rel, target_rel in DERIVED_ROPE_MODELS.items():
        source_path = SIMULATED_MODELS / source_rel
        if not source_path.is_file():
            print(f"warning: source not found: {source_path}", file=sys.stderr)
            continue
        payload = json.loads(source_path.read_text(encoding="utf-8"))
        rewrite_textures(payload)
        target_path = BLOCK_MODELS_DIR / target_rel
        write_json(target_path, payload)
        print(f"wrote {target_path}")
        written += 1
    return written


def main() -> int:
    lang: dict[str, str] = {}

    for color in COLORS:
        item_id = f"{color}_rope"

        write_json(
            MODELS_DIR / f"{item_id}.json",
            {
                "parent": "item/generated",
                "textures": {"layer0": f"{MOD_ID}:item/{item_id}"},
            },
        )

        write_json(
            RECIPES_DIR / f"{item_id}.json",
            {
                "type": "minecraft:crafting_shapeless",
                "category": "misc",
                "group": "dyeable_ropes",
                "ingredients": [
                    {"item": ROPE_INPUT},
                    {"tag": f"c:dyes/{color}"},
                ],
                "result": {"id": f"{MOD_ID}:{item_id}", "count": 1},
            },
        )

        lang[f"item.{MOD_ID}.{item_id}"] = f"{title(color)} Rope"

    LANG_FILE.parent.mkdir(parents=True, exist_ok=True)
    LANG_FILE.write_text(
        json.dumps(lang, indent=2) + "\n", encoding="utf-8", newline="\n"
    )
    print(f"wrote {len(COLORS)} models, {len(COLORS)} recipes, and {LANG_FILE.name}")

    derived = write_derived_block_models()
    if derived:
        print(f"wrote {derived} derived greyscale block model(s)")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
