package dev.matejhozlar.dyeableropes.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import dev.matejhozlar.dyeableropes.DyeableRopesItems;
import dev.matejhozlar.dyeableropes.DyedStrandSavedData;
import dev.matejhozlar.dyeableropes.network.SetStrandColorPayload;
import dev.simulated_team.simulated.content.blocks.rope.RopeStrandHolderBehavior;
import dev.simulated_team.simulated.content.blocks.rope.strand.server.ServerRopeStrand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(RopeStrandHolderBehavior.class)
public abstract class RopeStrandHolderBehaviorMixin {

    @Shadow
    @Nullable
    private Level getLevel() {
        throw new AssertionError();
    }

    @Shadow
    @Nullable
    private ServerRopeStrand ownedServerStrand;

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
        UUID strandId = strand.getUUID();
        DyeColor color = DyedStrandSavedData.get(serverLevel).removeColor(strandId);
        if (color == null) {
            return original;
        }
        PacketDistributor.sendToAllPlayers(new SetStrandColorPayload(strandId, SetStrandColorPayload.CLEAR));
        Item rope = DyeableRopesItems.ROPES.get(color).get();
        return new ItemStack(rope);
    }

    @Inject(
            method = "tickStrandTrackingPlayers",
            at = @At(
                    value = "INVOKE",
                    target = "Ldev/simulated_team/simulated/content/blocks/rope/RopeStrandHolderBehavior;makeUpdatePacket()Ldev/simulated_team/simulated/network/packets/rope/ClientboundRopeDataPacket;"
            )
    )
    private void dyeable_ropes$sendColorOnTrackingStart(CallbackInfo ci, @Local ServerPlayer player) {
        if (ownedServerStrand == null) return;
        Level level = getLevel();
        if (!(level instanceof ServerLevel serverLevel)) return;
        UUID strandId = ownedServerStrand.getUUID();
        DyeColor color = DyedStrandSavedData.get(serverLevel).getColor(strandId);
        if (color != null) {
            PacketDistributor.sendToPlayer(player, new SetStrandColorPayload(strandId, color.getId()));
        }
    }
}
