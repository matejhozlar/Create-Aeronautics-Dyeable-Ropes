package dev.matejhozlar.dyeableropes.client;

import net.minecraft.world.item.DyeColor;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class ClientDyedStrandColors {

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

    private ClientDyedStrandColors() {}
}
