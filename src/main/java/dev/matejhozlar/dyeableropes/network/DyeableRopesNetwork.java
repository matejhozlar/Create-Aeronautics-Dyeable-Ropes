package dev.matejhozlar.dyeableropes.network;

import dev.matejhozlar.dyeableropes.DyedStrandSavedData;
import dev.matejhozlar.dyeableropes.client.ClientDyedStrandColors;
import dev.simulated_team.simulated.content.blocks.rope.strand.server.ServerLevelRopeManager;
import dev.simulated_team.simulated.content.blocks.rope.strand.server.ServerRopeStrand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.joml.Vector3d;

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
                )
                .playToServer(
                        BleachStrandPayload.TYPE,
                        BleachStrandPayload.STREAM_CODEC,
                        DyeableRopesNetwork::handleBleachStrand
                );
    }

    private static void handleSetStrandColor(SetStrandColorPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            int ordinal = payload.colorOrdinal();
            if (ordinal == SetStrandColorPayload.CLEAR) {
                ClientDyedStrandColors.remove(payload.strandId());
                return;
            }
            if (ordinal < 0 || ordinal >= DyeColor.values().length) return;
            ClientDyedStrandColors.put(payload.strandId(), DyeColor.byId(ordinal));
        });
    }

    private static void handleDyeStrand(DyeStrandPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Player player = ctx.player();
            if (!(player instanceof ServerPlayer serverPlayer)) return;
            if (!(serverPlayer.level() instanceof ServerLevel serverLevel)) return;

            ItemStack stack = serverPlayer.getItemInHand(payload.hand());
            if (!(stack.getItem() instanceof DyeItem dye)) return;
            if (dye.getDyeColor().getId() != payload.colorOrdinal()) return;

            ServerLevelRopeManager ropeManager = ServerLevelRopeManager.getOrCreate(serverLevel);
            ServerRopeStrand strand = ropeManager.getStrand(payload.strandId());
            if (strand == null) return;
            if (!isWithinReach(serverPlayer, strand)) return;

            DyeColor color = dye.getDyeColor();
            DyedStrandSavedData.get(serverLevel).setColor(payload.strandId(), color);
            PacketDistributor.sendToAllPlayers(new SetStrandColorPayload(payload.strandId(), color.getId()));

            if (!serverPlayer.getAbilities().instabuild) {
                stack.shrink(1);
            }
        });
    }

    private static void handleBleachStrand(BleachStrandPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Player player = ctx.player();
            if (!(player instanceof ServerPlayer serverPlayer)) return;
            if (!(serverPlayer.level() instanceof ServerLevel serverLevel)) return;

            ItemStack stack = serverPlayer.getItemInHand(payload.hand());
            if (!stack.is(Items.WATER_BUCKET)) return;

            ServerLevelRopeManager ropeManager = ServerLevelRopeManager.getOrCreate(serverLevel);
            ServerRopeStrand strand = ropeManager.getStrand(payload.strandId());
            if (strand == null) return;
            if (!isWithinReach(serverPlayer, strand)) return;

            if (DyedStrandSavedData.get(serverLevel).removeColor(payload.strandId()) == null) return;
            PacketDistributor.sendToAllPlayers(
                    new SetStrandColorPayload(payload.strandId(), SetStrandColorPayload.CLEAR));

            if (!serverPlayer.getAbilities().instabuild) {
                serverPlayer.setItemInHand(payload.hand(), new ItemStack(Items.BUCKET));
            }
        });
    }

    private static boolean isWithinReach(ServerPlayer player, ServerRopeStrand strand) {
        Vec3 eye = player.getEyePosition();
        double reach = player.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE) + 2.0;
        double maxDistanceSq = reach * reach;
        for (Vector3d point : strand.getPoints()) {
            if (point.distanceSquared(eye.x, eye.y, eye.z) <= maxDistanceSq) {
                return true;
            }
        }
        return false;
    }

    private DyeableRopesNetwork() {}
}
