package dev.matejhozlar.dyeableropes.mixin;

import dev.simulated_team.simulated.content.blocks.rope.RopeStrandHolderBehavior;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.UUID;

@Mixin(RopeStrandHolderBehavior.class)
public interface RopeStrandHolderBehaviorAccessor {

    @Accessor("attachedRopeID")
    @Nullable
    UUID dyeable_ropes$getAttachedRopeID();
}
