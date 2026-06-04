package dev.matejhozlar.dyeableropes.client;

import dev.matejhozlar.dyeableropes.DyeableRopes;
import net.createmod.catnip.render.SpriteShiftEntry;
import net.createmod.catnip.render.SpriteShifter;
import net.minecraft.resources.ResourceLocation;

public final class DyeableRopesSpriteShifts {

    public static final SpriteShiftEntry ROPE_WINCH_COIL =
            get("block/rope_winch/winch_coil_greyscale", "block/rope_winch/winch_coil_scroll_greyscale");

    public static void init() {
    }

    private static SpriteShiftEntry get(String originalPath, String targetPath) {
        return SpriteShifter.get(
                ResourceLocation.fromNamespaceAndPath(DyeableRopes.MODID, originalPath),
                ResourceLocation.fromNamespaceAndPath(DyeableRopes.MODID, targetPath));
    }

    private DyeableRopesSpriteShifts() {}
}
