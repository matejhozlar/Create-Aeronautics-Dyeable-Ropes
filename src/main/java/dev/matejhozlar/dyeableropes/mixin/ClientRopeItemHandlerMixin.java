package dev.matejhozlar.dyeableropes.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.simulated_team.simulated.content.items.rope.RopeItem.ClientRopeItemHandler;
import dev.simulated_team.simulated.content.items.rope.RopeItem.RopeItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClientRopeItemHandler.class)
public class ClientRopeItemHandlerMixin {

    @ModifyExpressionValue(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/tterrag/registrate/util/entry/ItemEntry;isIn(Lnet/minecraft/world/item/ItemStack;)Z"
            )
    )
    private static boolean dyeable_ropes$includeColoredRopes(boolean original, @Local ItemStack heldItem) {
        return original || heldItem.getItem() instanceof RopeItem;
    }
}
