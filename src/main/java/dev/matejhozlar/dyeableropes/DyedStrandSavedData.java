package dev.matejhozlar.dyeableropes;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DyedStrandSavedData extends SavedData {

    private static final String DATA_NAME = "dyeable_ropes_strand_colors";

    private final Map<UUID, DyeColor> colors = new HashMap<>();

    public static DyedStrandSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(DyedStrandSavedData::new, DyedStrandSavedData::load, null),
                DATA_NAME
        );
    }

    @Nullable
    public DyeColor getColor(UUID strandId) {
        return colors.get(strandId);
    }

    public void setColor(UUID strandId, DyeColor color) {
        DyeColor previous = colors.put(strandId, color);
        if (previous != color) {
            setDirty();
        }
    }

    @Nullable
    public DyeColor removeColor(UUID strandId) {
        DyeColor previous = colors.remove(strandId);
        if (previous != null) {
            setDirty();
        }
        return previous;
    }

    public static DyedStrandSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        DyedStrandSavedData data = new DyedStrandSavedData();
        ListTag entries = tag.getList("Entries", Tag.TAG_COMPOUND);
        for (int i = 0; i < entries.size(); i++) {
            CompoundTag entry = entries.getCompound(i);
            if (!entry.hasUUID("UUID")) continue;
            UUID uuid = entry.getUUID("UUID");
            DyeColor color = DyeColor.byName(entry.getString("Color"), null);
            if (color != null) {
                data.colors.put(uuid, color);
            }
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag entries = new ListTag();
        for (Map.Entry<UUID, DyeColor> e : colors.entrySet()) {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("UUID", e.getKey());
            entry.putString("Color", e.getValue().getName());
            entries.add(entry);
        }
        tag.put("Entries", entries);
        return tag;
    }
}
