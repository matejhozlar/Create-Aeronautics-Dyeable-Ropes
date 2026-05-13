package dev.matejhozlar.dyeableropes;

import dev.matejhozlar.dyeableropes.client.DyeableRopesPartialModels;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(value = DyeableRopes.MODID, dist = Dist.CLIENT)
public class DyeableRopesClient {

    public DyeableRopesClient(IEventBus modEventBus) {
        DyeableRopesPartialModels.init();
    }
}
