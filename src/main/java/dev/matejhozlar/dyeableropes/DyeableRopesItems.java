package dev.matejhozlar.dyeableropes;

import dev.simulated_team.simulated.content.items.rope.RopeItem.RopeItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.EnumMap;
import java.util.Map;

public final class DyeableRopesItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(DyeableRopes.MODID);

    public static final Map<DyeColor, DeferredHolder<Item, RopeItem>> ROPES = new EnumMap<>(DyeColor.class);

    static {
        for (DyeColor color : DyeColor.values()) {
            ROPES.put(color, ITEMS.register(color.getName() + "_rope", () -> new RopeItem(new Item.Properties())));
        }
    }

    public static void onBuildCreativeTabContents(BuildCreativeModeTabContentsEvent event) {
        ResourceLocation tabId = event.getTabKey().location();
        boolean simulatedTab = "simulated".equals(tabId.getNamespace()) && "main_tab".equals(tabId.getPath());
        if (!simulatedTab) {
            return;
        }
        for (DyeColor color : DyeColor.values()) {
            event.accept(ROPES.get(color).get());
        }
    }

    private DyeableRopesItems() {}
}
