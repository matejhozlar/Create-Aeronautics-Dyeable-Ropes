package dev.matejhozlar.dyeableropes.network;

import dev.matejhozlar.dyeableropes.client.ClientDyedStrandColors;
import net.minecraft.world.item.DyeColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class DyeableRopesNetwork {

    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1")
                .playToClient(
                        SetStrandColorPayload.TYPE,
                        SetStrandColorPayload.STREAM_CODEC,
                        DyeableRopesNetwork::handleSetStrandColor
                );
    }

    @OnlyIn(Dist.CLIENT)
    private static void handleSetStrandColor(SetStrandColorPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (payload.colorOrdinal() == SetStrandColorPayload.CLEAR) {
                ClientDyedStrandColors.remove(payload.strandId());
            } else {
                ClientDyedStrandColors.put(payload.strandId(), DyeColor.byId(payload.colorOrdinal()));
            }
        });
    }

    private DyeableRopesNetwork() {}
}
