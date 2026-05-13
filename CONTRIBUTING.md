# Contributing

Thanks for your interest in contributing to Dyeable Ropes for Create Aeronautics. This document covers what you need to build the mod locally, the conventions used in the repository, and how to get a change reviewed and merged.

## Reporting issues

Before opening a bug report:

1. Confirm the bug reproduces with **only** Dyeable Ropes plus the required dependencies (Create: Aeronautics, which jars-in-jars Simulated, Aeronautics, and Offroad). Crashes that only happen with a large modpack are usually not actionable until they're isolated.
2. Make sure you're on the latest released version. Compare your symptoms against [`CHANGELOG.md`](CHANGELOG.md) in case the issue is already fixed.
3. Search existing issues first.

Open new issues:

- **Bug report**: include Minecraft version, NeoForge version, mod version, and a `latest.log` or `crash-report` file. Without those, a bug report is hard to act on. For color-rendering bugs, a screenshot helps far more than a description (in-world color issues are easy to mis-describe).
- **Feature request**: describe the player-facing behavior you want, not the implementation.

Issues drafted from user reports sometimes contain wrong diagnoses or stale references. If you're proposing a fix, read the referenced code first and verify the premise holds.

## Development setup

### Prerequisites

- JDK 21 (Temurin recommended; matches CI)
- Git
- Python 3.10+ with Pillow (`pip install Pillow`) for regenerating textures and derived model JSONs
- An IDE that understands Gradle (IntelliJ IDEA is what the project is configured for; `.idea/` settings include `downloadSources = true`)

### Cloning and the Simulated dependency

Dyeable Ropes is an addon for Simulated (the physics module bundled inside Create: Aeronautics) and is built against Simulated's compiled output. You have two options:

**Option A: build Simulated as a sibling checkout (matches CI).**

```sh
# Sibling layout: both repos under the same parent directory
git clone https://github.com/Creators-of-Aeronautics/Simulated-Project.git ../Simulated-Project
(cd ../Simulated-Project && ./gradlew :simulated:neoforge:jar)
```

The build script picks the resulting jar up from `../Simulated-Project/simulated/neoforge/build/libs/` automatically.

**Option B: drop a prebuilt Simulated jar into `libs/`.**

```sh
mkdir -p libs
cp /path/to/simulated-neoforge-<version>.jar libs/
```

Either works; `libs/` takes precedence if both are present.

### Regenerating derived assets

The 16 item textures, 3 greyscale block textures, and 4 derived rope model JSONs are **gitignored**. They are derivatives of Simulated's All-Rights-Reserved textures and model JSONs, so we never commit them. Regenerate them after cloning (and again whenever Simulated's source textures change):

```sh
python scripts/recolor_ropes.py
python scripts/generate_jsons.py
```

Both scripts read from `../Simulated-Project/` (Option A above). Build will fail without these assets present.

### Building

```sh
./gradlew build
```

The output jar lands in `build/libs/dyeable_ropes-<version>.jar`.

### Running the dev client/server

```sh
./gradlew runClient
./gradlew runServer
```

Both runs use the configuration in `run/`. There is no in-game config for this mod yet; the only tunable is the greyscale shadow floor in `scripts/recolor_ropes.py` (`GREYSCALE_FLOOR`), which is baked into the textures at regen time rather than read at runtime.

## Code style

- **Java 21**, with `JavaLanguageVersion.of(21)` enforced by the build.
- **Comments**: default to writing none. Only add a comment when the *why* is non-obvious (a hidden constraint, a workaround for a specific upstream bug, a subtle invariant). Don't restate what well-named identifiers already say, and don't leave historical notes ("previously did X, now does Y") or PR back-references in source.
- **Mixins** are this mod's primary integration tool with Simulated. Keep each mixin small and targeted at one observable behavior, prefer MixinExtras (`@ModifyExpressionValue`, `@WrapOperation`) over raw `@Redirect` when the operation has multiple equally-valid hook points, and write the mixin against the public-looking API (field reads, named method INVOKEs) rather than relying on opcode offsets.
- **Match the existing structure.** Renderer interactions live in `mixin/`, the client-side color cache and partial-model registration live in `client/`, the SavedData and item classes live at the top level, and network payloads live in `network/`. New behavior touching a different Simulated render path should follow the same per-renderer mixin pattern (one mixin per target class).
- When you reference Simulated APIs (e.g. `RopeStrandHolderBehavior`, `ZiplineClientManager.raycastRope`, `SimPartialModels`), check the version your local Simulated jar exposes; the surface has changed across releases.

## Asset licensing

Simulated's textures and model JSONs are licensed All Rights Reserved. The recolor/derive scripts produce locally-stored derivatives for development convenience, but **never commit those files** and never check in a `*.png` from `src/main/resources/assets/dyeable_ropes/textures/` or a `*_greyscale.json` from `src/main/resources/assets/dyeable_ropes/models/block/`. The `.gitignore` excludes both directories; if you change script output paths, update the ignore rules to match.

## Branching

- Branch off `1.21.1` (the active development branch for Minecraft 1.21.1).
- Branch naming: `<type>/<short-slug>`, where `<type>` matches the commit type (`feat/...`, `fix/...`, `chore/...`, `refactor/...`, `docs/...`).

## Commits

Format: `type: description`

- **Types**: `feat`, `fix`, `chore`, `refactor`, `docs`, `style`, `test`, `perf`.
- **Description**: lowercase, imperative mood, no trailing period.

Examples:

```
fix: reset SuperByteBuffer tint each render to stop color leakage
feat: dye a placed strand by right-clicking with a dye
chore: bump simulated_version to 1.2.2
refactor: centralize strand-color lookup in ClientDyedStrandColors
```

## Pull requests

A good PR description covers:

- **What changed** at the player-visible level (one or two sentences).
- **Why** the change is needed: the bug it fixes, the gap in behavior, or the issue it closes.
- **How to test it in-game.** For rendering changes this is essential; reviewers can't tell from a diff alone whether a tinted rope reads as the right color. Include the colors you exercised, and ideally a screenshot.
- **Asset or recipe impact**, if any. If you change the recolor script's output (new texture path, different greyscale curve), call it out so reviewers know to regenerate before testing.

## License

By contributing, you agree that your contributions will be licensed under the [MIT License](LICENSE) that covers this project.
