package dev.matejhozlar.dyeableropes.client;

import dev.matejhozlar.dyeableropes.mixin.RopeStrandHolderBehaviorAccessor;
import dev.simulated_team.simulated.content.blocks.rope.RopeStrandHolderBehavior;
import dev.simulated_team.simulated.content.blocks.rope.strand.client.ClientRopeStrand;
import net.minecraft.world.item.DyeColor;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class ClientDyedStrandColors {

    public static final int NO_TINT = 0xFFFFFFFF;

    private static final ConcurrentMap<UUID, DyeColor> COLORS = new ConcurrentHashMap<>();

    @Nullable
    public static DyeColor get(UUID strandId) {
        return COLORS.get(strandId);
    }

    public static void put(UUID strandId, DyeColor color) {
        COLORS.put(strandId, color);
    }

    public static void remove(UUID strandId) {
        COLORS.remove(strandId);
    }

    public static void clear() {
        COLORS.clear();
    }

    /**
     * Resolves the strand UUID a holder is part of, preferring the owned client strand and
     * falling back to {@code attachedRopeID} for the non-owner end.
     */
    @Nullable
    public static UUID strandIdFor(RopeStrandHolderBehavior holder) {
        ClientRopeStrand strand = holder.getClientStrand();
        if (strand != null) {
            return strand.getUuid();
        }
        return ((RopeStrandHolderBehaviorAccessor) holder).dyeable_ropes$getAttachedRopeID();
    }

    public static int tintForHolder(RopeStrandHolderBehavior holder) {
        UUID id = strandIdFor(holder);
        if (id == null) return NO_TINT;
        DyeColor color = COLORS.get(id);
        return color != null ? color.getTextureDiffuseColor() : NO_TINT;
    }

    private ClientDyedStrandColors() {}
}
