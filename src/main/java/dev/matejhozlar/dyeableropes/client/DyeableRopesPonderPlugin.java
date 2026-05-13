package dev.matejhozlar.dyeableropes.client;

import com.simibubi.create.foundation.ponder.CreatePonderPlugin;
import dev.matejhozlar.dyeableropes.DyeableRopes;
import dev.matejhozlar.dyeableropes.DyeableRopesItems;
import dev.matejhozlar.dyeableropes.DyedRopeItem;
import dev.simulated_team.simulated.index.SimPonderTags;
import dev.simulated_team.simulated.ponder.scenes.RopeScenes;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.List;

public class DyeableRopesPonderPlugin extends CreatePonderPlugin {

    @Override
    public String getModId() {
        return DyeableRopes.MODID;
    }

    @Override
    public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        ResourceLocation[] ropeIds = DyeableRopesItems.ROPES.values().stream()
                .map(DeferredHolder::getId)
                .toArray(ResourceLocation[]::new);

        helper.forComponents(ropeIds)
                .addStoryBoard("rope", RopeScenes::ropeIntro)
                .addStoryBoard("rope", RopeScenes::ropeConnections);
    }

    @Override
    public void registerTags(PonderTagRegistrationHelper<ResourceLocation> helper) {
        PonderTagRegistrationHelper<DyedRopeItem> itemHelper = helper.withKeyFunction(
                item -> BuiltInRegistries.ITEM.getKey(item)
        );

        List<DyedRopeItem> items = DyeableRopesItems.ROPES.values().stream()
                .map(DeferredHolder::get)
                .toList();

        var addToTag = itemHelper.addToTag(SimPonderTags.PHYSICS_BEHAVIOR);
        for (DyedRopeItem item : items) {
            addToTag.add(item);
        }
    }
}
