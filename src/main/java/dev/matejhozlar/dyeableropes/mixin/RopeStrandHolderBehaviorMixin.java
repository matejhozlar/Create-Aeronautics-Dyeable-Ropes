package dev.matejhozlar.dyeableropes.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import dev.matejhozlar.dyeableropes.DyeableRopesItems;
import dev.matejhozlar.dyeableropes.DyedStrandSavedData;
import dev.simulated_team.simulated.content.blocks.rope.RopeStrandHolderBehavior;
import dev.simulated_team.simulated.content.blocks.rope.strand.server.ServerRopeStrand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(RopeStrandHolderBehavior.class)
public abstract class RopeStrandHolderBehaviorMixin {

    @Shadow
    @Nullable
    private Level getLevel() {
        throw new AssertionError();
    }

    @ModifyVariable(
            method = "destroyRope",
            at = @At("STORE"),
            ordinal = 0
    )
    private ItemStack dyeable_ropes$colorDrop(ItemStack original, @Local ServerRopeStrand strand) {
        Level level = getLevel();
        if (!(level instanceof ServerLevel serverLevel)) {
            return original;
        }
        DyeColor color = DyedStrandSavedData.get(serverLevel).removeColor(strand.getUUID());
        if (color == null) {
            return original;
        }
        Item rope = DyeableRopesItems.ROPES.get(color).get();
        return new ItemStack(rope);
    }
}
