package dev.matejhozlar.dyeableropes.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.matejhozlar.dyeableropes.client.ClientDyedStrandColors;
import dev.simulated_team.simulated.content.blocks.rope.RopeStrandHolderBehavior;
import dev.simulated_team.simulated.content.blocks.rope.strand.client.ClientRopeStrand;
import dev.simulated_team.simulated.content.blocks.rope.strand.client.RopeStrandRenderer;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.world.item.DyeColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RopeStrandRenderer.class)
public class RopeStrandRendererMixin {

    @ModifyExpressionValue(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/createmod/catnip/render/SuperByteBuffer;light(I)Lnet/createmod/catnip/render/SuperByteBuffer;"
            )
    )
    private static SuperByteBuffer dyeable_ropes$tintIfColored(
            SuperByteBuffer buffer,
            @Local(argsOnly = true) RopeStrandHolderBehavior ropeHolder
    ) {
        ClientRopeStrand strand = ropeHolder.getClientStrand();
        if (strand == null) return buffer;
        DyeColor color = ClientDyedStrandColors.get(strand.getUuid());
        if (color == null) return buffer;
        return buffer.color(color.getTextureDiffuseColor());
    }
}
