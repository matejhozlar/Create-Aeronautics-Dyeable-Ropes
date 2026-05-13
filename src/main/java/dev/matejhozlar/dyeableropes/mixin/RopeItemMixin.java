package dev.matejhozlar.dyeableropes.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.matejhozlar.dyeableropes.DyedRopeItem;
import dev.matejhozlar.dyeableropes.DyedStrandSavedData;
import dev.simulated_team.simulated.content.blocks.rope.RopeStrandHolderBehavior;
import dev.simulated_team.simulated.content.blocks.rope.strand.server.ServerRopeStrand;
import dev.simulated_team.simulated.content.items.rope.RopeItem.RopeItem;
import dev.simulated_team.simulated.index.SimDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RopeItem.class)
public class RopeItemMixin {

    @ModifyExpressionValue(
            method = "useOn",
            at = @At(
                    value = "INVOKE",
                    target = "Ldev/simulated_team/simulated/content/items/rope/RopeItem/RopeItem;attachRope(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;)Z"
            )
    )
    private boolean dyeable_ropes$recordStrandColor(
            boolean attached,
            @Local Level level,
            @Local ItemStack heldStack,
            @Local BlockPos clickedPos
    ) {
        if (!attached) return false;
        if (!(level instanceof ServerLevel serverLevel)) return true;
        if (!(heldStack.getItem() instanceof DyedRopeItem dyed)) return true;

        BlockPos firstConn = heldStack.get(SimDataComponents.ROPE_FIRST_CONNECTION);
        if (firstConn == null) return true;

        ServerRopeStrand strand = ownedStrandAt(level, firstConn);
        if (strand == null) {
            strand = ownedStrandAt(level, clickedPos);
        }
        if (strand != null) {
            DyedStrandSavedData.get(serverLevel).setColor(strand.getUUID(), dyed.getColor());
        }
        return true;
    }

    private static ServerRopeStrand ownedStrandAt(Level level, BlockPos pos) {
        RopeStrandHolderBehavior holder = RopeItem.getRopeHolder(level, pos);
        return holder != null ? holder.getOwnedStrand() : null;
    }
}
