package dev.matejhozlar.dyeableropes;

import dev.simulated_team.simulated.content.items.rope.RopeItem.RopeItem;
import net.minecraft.world.item.DyeColor;

public class DyedRopeItem extends RopeItem {

    private final DyeColor color;

    public DyedRopeItem(Properties properties, DyeColor color) {
        super(properties);
        this.color = color;
    }

    public DyeColor getColor() {
        return color;
    }
}
