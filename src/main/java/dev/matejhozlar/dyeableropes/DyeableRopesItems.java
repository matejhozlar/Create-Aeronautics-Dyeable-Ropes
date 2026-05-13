package dev.matejhozlar.dyeableropes;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.EnumMap;
import java.util.Map;

public final class DyeableRopesItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(DyeableRopes.MODID);

    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, DyeableRopes.MODID);

    public static final Map<DyeColor, DeferredHolder<Item, DyedRopeItem>> ROPES = new EnumMap<>(DyeColor.class);

    static {
        for (DyeColor color : DyeColor.values()) {
            ROPES.put(color, ITEMS.register(color.getName() + "_rope", () -> new DyedRopeItem(new Item.Properties(), color)));
        }
    }

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN_TAB = TABS.register(
            "main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + DyeableRopes.MODID + ".main"))
                    .icon(() -> new ItemStack(ROPES.get(DyeColor.RED).get()))
                    .displayItems((params, output) -> {
                        for (DyeColor color : DyeColor.values()) {
                            output.accept(ROPES.get(color).get());
                        }
                    })
                    .build()
    );

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
