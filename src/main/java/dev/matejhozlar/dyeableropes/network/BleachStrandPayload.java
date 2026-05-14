package dev.matejhozlar.dyeableropes.network;

import dev.matejhozlar.dyeableropes.DyeableRopes;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;

import java.util.UUID;

public record BleachStrandPayload(UUID strandId, InteractionHand hand) implements CustomPacketPayload {

    public static final Type<BleachStrandPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(DyeableRopes.MODID, "bleach_strand")
    );

    public static final StreamCodec<ByteBuf, BleachStrandPayload> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, BleachStrandPayload::strandId,
            DyeStrandPayload.HAND_CODEC, BleachStrandPayload::hand,
            BleachStrandPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
