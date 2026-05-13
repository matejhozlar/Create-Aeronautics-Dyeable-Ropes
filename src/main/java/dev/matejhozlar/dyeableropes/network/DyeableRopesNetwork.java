package dev.matejhozlar.dyeableropes.network;

import dev.matejhozlar.dyeableropes.DyedStrandSavedData;
import dev.matejhozlar.dyeableropes.client.ClientDyedStrandColors;
import dev.simulated_team.simulated.content.blocks.rope.strand.server.ServerLevelRopeManager;
import dev.simulated_team.simulated.content.blocks.rope.strand.server.ServerRopeStrand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class DyeableRopesNetwork {

    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1")
                .playToClient(
                        SetStrandColorPayload.TYPE,
                        SetStrandColorPayload.STREAM_CODEC,
                        DyeableRopesNetwork::handleSetStrandColor
                )
                .playToServer(
                        DyeStrandPayload.TYPE,
                        DyeStrandPayload.STREAM_CODEC,
                        DyeableRopesNetwork::handleDyeStrand
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

    private static void handleDyeStrand(DyeStrandPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Player player = ctx.player();
            if (!(player instanceof ServerPlayer serverPlayer)) return;
            if (!(serverPlayer.level() instanceof ServerLevel serverLevel)) return;

            ItemStack stack = serverPlayer.getMainHandItem();
            if (!(stack.getItem() instanceof DyeItem dye)) return;
            if (dye.getDyeColor().getId() != payload.colorOrdinal()) return;

            ServerLevelRopeManager ropeManager = ServerLevelRopeManager.getOrCreate(serverLevel);
            ServerRopeStrand strand = ropeManager.getStrand(payload.strandId());
            if (strand == null) return;

            DyeColor color = dye.getDyeColor();
            DyedStrandSavedData.get(serverLevel).setColor(payload.strandId(), color);
            PacketDistributor.sendToAllPlayers(new SetStrandColorPayload(payload.strandId(), color.getId()));

            if (!serverPlayer.getAbilities().instabuild) {
                stack.shrink(1);
            }
        });
    }

    private DyeableRopesNetwork() {}
}
