package dev.matejhozlar.dyeableropes;

import dev.matejhozlar.dyeableropes.network.DyeableRopesNetwork;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(DyeableRopes.MODID)
public class DyeableRopes {
    public static final String MODID = "dyeable_ropes";

    public DyeableRopes(IEventBus modEventBus, ModContainer container) {
        DyeableRopesItems.ITEMS.register(modEventBus);
        modEventBus.addListener(DyeableRopesItems::onBuildCreativeTabContents);
        modEventBus.addListener(DyeableRopesNetwork::register);
    }
}
