package dev.matejhozlar.dyeableropes.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.matejhozlar.dyeableropes.client.ClientDyedStrandColors;
import dev.simulated_team.simulated.content.blocks.rope.rope_winch.RopeWinchBlockEntity;
import dev.simulated_team.simulated.content.blocks.rope.rope_winch.RopeWinchRenderer;
import net.createmod.catnip.render.SuperByteBuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RopeWinchRenderer.class)
public class RopeWinchRendererMixin {

    @ModifyExpressionValue(
            method = "renderComponents",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/createmod/catnip/render/SuperByteBuffer;light(I)Lnet/createmod/catnip/render/SuperByteBuffer;"
            )
    )
    private SuperByteBuffer dyeable_ropes$tintWinchCoil(
            SuperByteBuffer buffer,
            @Local(argsOnly = true) RopeWinchBlockEntity be
    ) {
        return buffer.color(ClientDyedStrandColors.tintForHolder(be.getRopeHolder()));
    }
}
