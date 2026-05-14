package dev.matejhozlar.dyeableropes.network;

import dev.matejhozlar.dyeableropes.DyeableRopes;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;

import java.util.UUID;

public record DyeStrandPayload(UUID strandId, int colorOrdinal, InteractionHand hand) implements CustomPacketPayload {

    public static final Type<DyeStrandPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(DyeableRopes.MODID, "dye_strand")
    );

    static final StreamCodec<ByteBuf, InteractionHand> HAND_CODEC = ByteBufCodecs.BOOL.map(
            mainHand -> mainHand ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND,
            hand -> hand == InteractionHand.MAIN_HAND
    );

    public static final StreamCodec<ByteBuf, DyeStrandPayload> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, DyeStrandPayload::strandId,
            ByteBufCodecs.VAR_INT, DyeStrandPayload::colorOrdinal,
            HAND_CODEC, DyeStrandPayload::hand,
            DyeStrandPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
