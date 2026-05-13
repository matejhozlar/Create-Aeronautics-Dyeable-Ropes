## Version 1.0.0

### Added
- 16 dye-colored rope items, one per vanilla `DyeColor`, registered under `dyeable_ropes:<color>_rope` and inserted into Simulated's creative tab.
- Shapeless crafting recipe per color: any item tagged `dyeable_ropes:ropes` (plain Simulated rope or any colored rope) + any matching color dye, so first-time dyeing and recoloring share one recipe.
- In-world dyeing: hold a dye, look at any placed strand, right-click to recolor it on the spot. One dye consumed per recolor.
- Colored rope items behave as Simulated `RopeItem` instances, so the two-click rope-attachment flow works exactly like the plain `rope_coupling`, including the prediction outline preview while holding the rope.
- Strand colors persist across saves via a per-`ServerLevel` `DyedStrandSavedData` keyed by strand UUID, and resync to clients when they walk into range or rejoin.
- Strands, rope connector knots, and rope winch coils all render in the strand's dye color in-world. Tinting uses a luma-normalized greyscale derivative of Simulated's rope textures so the multiply produces clean colors instead of muddy brown-tinted ones.
- When a strand is destroyed, the matching colored rope is what drops back (mixin into `RopeStrandHolderBehavior.destroyRope` swaps the hardcoded `SimItems.ROPE_COUPLING` drop based on the recorded color).
