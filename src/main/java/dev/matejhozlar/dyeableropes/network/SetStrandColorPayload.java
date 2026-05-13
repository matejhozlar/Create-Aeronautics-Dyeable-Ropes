package dev.matejhozlar.dyeableropes.network;

import dev.matejhozlar.dyeableropes.DyeableRopes;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public record SetStrandColorPayload(UUID strandId, int colorOrdinal) implements CustomPacketPayload {

    public static final int CLEAR = -1;

    public static final Type<SetStrandColorPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(DyeableRopes.MODID, "set_strand_color")
    );

    public static final StreamCodec<ByteBuf, SetStrandColorPayload> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, SetStrandColorPayload::strandId,
            ByteBufCodecs.VAR_INT, SetStrandColorPayload::colorOrdinal,
            SetStrandColorPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
