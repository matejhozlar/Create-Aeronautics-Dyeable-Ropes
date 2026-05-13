"""Generate per-color item model JSONs, shapeless dye recipes, and the en_us lang map.

Run from the project root:
    python scripts/generate_jsons.py
"""

from __future__ import annotations

import json
from pathlib import Path

COLORS: list[str] = [
    "white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray",
    "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black",
]

MOD_ID = "dyeable_ropes"
ROPE_INPUT = "simulated:rope_coupling"

ROOT = Path(__file__).resolve().parent.parent
MODELS_DIR = ROOT / "src/main/resources/assets" / MOD_ID / "models/item"
RECIPES_DIR = ROOT / "src/main/resources/data" / MOD_ID / "recipe"
LANG_FILE = ROOT / "src/main/resources/assets" / MOD_ID / "lang/en_us.json"


def title(color: str) -> str:
    return " ".join(part.capitalize() for part in color.split("_"))


def write_json(path: Path, payload: dict) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(payload, indent=2) + "\n", encoding="utf-8", newline="\n")


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
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
