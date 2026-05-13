package dev.matejhozlar.dyeableropes.client;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import dev.matejhozlar.dyeableropes.DyeableRopes;
import net.minecraft.resources.ResourceLocation;

public final class DyeableRopesPartialModels {

    public static final PartialModel
            ROPE = block("rope/rope_greyscale"),
            ROPE_KNOT = block("rope/knot_greyscale"),
            ROPE_CONNECTOR_KNOT = block("rope_connector/knot_greyscale"),
            ROPE_WINCH_ROPE_COIL = block("rope_winch/rope_coil_greyscale");

    private static PartialModel block(String path) {
        return PartialModel.of(ResourceLocation.fromNamespaceAndPath(DyeableRopes.MODID, "block/" + path));
    }

    public static void init() {
    }

    private DyeableRopesPartialModels() {}
}
