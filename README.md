# Dyeable Ropes for Create Aeronautics

<p align="center">
  <a href="https://www.curseforge.com/projects/1542130"><img src="https://img.shields.io/curseforge/dt/1542130?logo=curseforge&label=CurseForge&color=F16436&style=for-the-badge" alt="CurseForge Downloads"></a>
</p>

<p align="center">
  <a href="https://www.minecraft.net/"><img src="https://img.shields.io/badge/Minecraft-1.21.1-62B47A?logo=minecraft" alt="Minecraft"></a>
  <a href="https://neoforged.net/"><img src="https://img.shields.io/badge/NeoForge-21.1.227%2B-DC2626" alt="NeoForge"></a>
  <a href="https://opensource.org/licenses/MIT"><img src="https://img.shields.io/badge/License-MIT-blue.svg" alt="License: MIT"></a>
</p>

NeoForge addon for [Create: Aeronautics](https://modrinth.com/mod/create-aeronautics), specifically the bundled Simulated module that handles ropes. Adds 16 dye-colored rope variants, one per vanilla `DyeColor`, with colors that persist on placed strands so you can finally tell rope networks apart.

## How it works

Simulated's rope system bakes one brown rope texture into a 3D mesh that the renderer (`RopeStrandRenderer`) reuses for every strand in the world. Tinting that mesh by multiplying it against a dye color produces muddy results (a magenta dye on a dark brown rope reads as red because the texture has almost no blue channel). The fix is twofold: ship a greyscale copy of Simulated's rope mesh textures and tint that, plus keep the original brown path for uncolored ropes so vanilla Simulated rope still looks the same.

### Items and crafting

- 16 `DyedRopeItem` subclasses of Simulated's `RopeItem`, registered under `dyeable_ropes:<color>_rope`. Inheriting `useOn` gives the two-click attachment flow (and the in-world prediction outline preview) for free.
- One shapeless crafting recipe per color: `#dyeable_ropes:ropes` (a tag holding plain `simulated:rope_coupling` plus all 16 colored variants) + `#c:dyes/<color>` -> the matching colored rope. The same recipe handles first-time dyeing and recoloring.

### In-world dyeing

- Holding a dye and right-clicking a placed strand recolors it in place. The client handler (`DyeableRopesClientEvents.onRightClickItem`) runs Simulated's `ZiplineClientManager.raycastRope` against the strand's point list using the player's eye position and block-interaction range, then cancels the vanilla interaction and sends a `DyeStrandPayload` to the server.
- The server revalidates: the held item is still the dye claimed by the payload, the strand UUID still exists in `ServerLevelRopeManager`. Then it writes the color into `DyedStrandSavedData`, broadcasts `SetStrandColorPayload` to every player so render state stays consistent, and shrinks the dye stack (creative skipped).

### In-world rendering

- A scripted recolor pass (`scripts/recolor_ropes.py`) reads Simulated's `rope_particle.png`, `rope_winch/winch.png`, and `rope_winch/winch_coil.png`, luma-normalizes each, and lifts the shadow floor to `0.5` so the darkest pixels don't crush to pure black when multiplied. The outputs are gitignored because Simulated's assets are All Rights Reserved; the workflow regenerates them on every release build.
- A second script (`scripts/generate_jsons.py`) clones Simulated's rope block-model JSONs and rewrites the texture references to point at our greyscale variants.
- The four greyscale models register as Flywheel `PartialModel` handles in `DyeableRopesPartialModels`.
- `RopeStrandRendererMixin`, `RopeConnectorRendererMixin`, and `RopeWinchRendererMixin` each use MixinExtras `@ModifyExpressionValue` on the `SimPartialModels.*` field reads to swap to our greyscale equivalents when the strand has a recorded color, then use a second `@ModifyExpressionValue` on the `SuperByteBuffer.light(int)` chain to apply `.color(dye)`. Uncolored strands skip both swaps and render exactly like vanilla.

### Persistence and sync

- `DyedStrandSavedData` is a per-`ServerLevel` `SavedData` mapping strand UUID -> `DyeColor`. Written on successful strand creation (via a `RopeItemMixin` that hooks `attachRope`'s return) and cleared on strand destruction (via a `RopeStrandHolderBehaviorMixin` that hooks `destroyRope`).
- The same `destroyRope` mixin swaps the hardcoded `SimItems.ROPE_COUPLING` drop for the matching colored rope, so breaking a colored strand gives you back the colored rope, not the plain one.
- When a player newly enters tracking range of a colored strand, a second injection in `tickStrandTrackingPlayers` piggybacks on Simulated's existing strand-sync moment to also send a `SetStrandColorPayload` to that player. This covers cross-session loads, walking into range, and dimension changes without enumerating SavedData on login.

## Building

This depends on Simulated's compiled jar. Either:

- Build Simulated locally first (`gradlew :simulated:neoforge:build` in `Simulated-Project/`), or
- Drop a built `simulated-neoforge-*.jar` into `./libs/`.

The 16 item textures, 3 greyscale block textures, and 4 derived block model JSONs are all gitignored because they are derivatives of Simulated's All-Rights-Reserved assets. Regenerate them locally after cloning:

```
python scripts/recolor_ropes.py
python scripts/generate_jsons.py
```

Both scripts read from a sibling `../Simulated-Project/` checkout. Pillow is the only Python dependency (`pip install Pillow`).

Then run `gradlew build`.

## Using

To dye a rope:

| Where | How |
| --- | --- |
| Crafting table | Place any rope tagged `#dyeable_ropes:ropes` (plain Simulated rope or any colored variant) and any dye in the grid. Output is the matching colored rope. |
| In the world | Hold a dye, look at any placed strand, right-click. The strand changes color on the spot and one dye is consumed (creative skipped). |

To place a colored strand, use the colored rope item exactly like Simulated's `rope_coupling`: right-click the first holder block (chain, rope connector, rope winch) to anchor one end, right-click a second holder to attach the strand.

To revert to plain rope, break the strand and re-place it with `simulated:rope_coupling`. There is no "remove dye" recipe.

## Configuration

None at the moment. The shadow-floor used by the greyscale recolor is hardcoded in `scripts/recolor_ropes.py` (`GREYSCALE_FLOOR = 0.5`). Lower it for more shading contrast, raise it for flatter colors, regenerate, rebuild.
