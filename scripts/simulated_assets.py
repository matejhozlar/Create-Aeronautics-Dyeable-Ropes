"""Read Simulated's assets without a sibling Simulated-Project checkout.

Simulated has no public maven and ships only jar-in-jar'd inside Create:
Aeronautics, so its rope textures and block models are pulled straight out of
that bundle. The Gradle `extractSimulated` task drops the bundled Simulated jar
at build/extracted-simulated/simulated.jar; this prefers that, then falls back to
the create-aeronautics jar in the Gradle cache so the scripts also work standalone.

Set SIMULATED_JAR to point at a specific jar to override both.
"""

from __future__ import annotations

import io
import os
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
EXTRACTED_JAR = ROOT / "build" / "extracted-simulated" / "simulated.jar"
ASSET_PREFIX = "assets/simulated/"


class SimulatedAssets:
    """Asset reader over a Simulated jar, keyed by paths relative to assets/simulated."""

    def __init__(self, jar_bytes: bytes, origin: str):
        self._zip = zipfile.ZipFile(io.BytesIO(jar_bytes))
        self._names = set(self._zip.namelist())
        self.origin = origin

    def read_bytes(self, rel_path: str) -> bytes | None:
        name = ASSET_PREFIX + rel_path
        if name not in self._names:
            return None
        return self._zip.read(name)


def _read_aeronautics_version() -> str | None:
    props = ROOT / "gradle.properties"
    if not props.is_file():
        return None
    for line in props.read_text(encoding="utf-8").splitlines():
        if line.strip().startswith("create_aeronautics_version"):
            return line.partition("=")[2].strip() or None
    return None


def _find_aeronautics_jar() -> Path | None:
    cache = (
        Path.home() / ".gradle" / "caches" / "modules-2" / "files-2.1"
        / "maven.modrinth" / "create-aeronautics"
    )
    if not cache.is_dir():
        return None
    # Prefer the version the build is pinned to so a stale cache of some other
    # version does not win the mtime race; fall back to newest if it is absent.
    version = _read_aeronautics_version()
    if version:
        pinned = list(cache.glob(f"{version}+*/*/create-aeronautics-*.jar"))
        if pinned:
            return max(pinned, key=lambda p: p.stat().st_mtime)
    jars = list(cache.glob("*/*/create-aeronautics-*.jar"))
    if not jars:
        return None
    return max(jars, key=lambda p: p.stat().st_mtime)


def _nested_simulated_bytes(aeronautics_jar: Path) -> bytes | None:
    with zipfile.ZipFile(aeronautics_jar) as outer:
        for name in outer.namelist():
            if (
                name.startswith("META-INF/jarjar/")
                and "simulated" in name
                and name.endswith(".jar")
            ):
                return outer.read(name)
    return None


def load_simulated_assets() -> SimulatedAssets:
    override = os.environ.get("SIMULATED_JAR")
    if override:
        path = Path(override)
        return SimulatedAssets(path.read_bytes(), str(path))

    if EXTRACTED_JAR.is_file():
        return SimulatedAssets(EXTRACTED_JAR.read_bytes(), str(EXTRACTED_JAR))

    aeronautics_jar = _find_aeronautics_jar()
    if aeronautics_jar is not None:
        nested = _nested_simulated_bytes(aeronautics_jar)
        if nested is not None:
            return SimulatedAssets(nested, f"{aeronautics_jar} (bundled simulated)")

    raise SystemExit(
        "Could not locate Simulated assets. Run `gradlew extractSimulated` first, "
        "or set SIMULATED_JAR to a Simulated jar."
    )
