package dev.matejhozlar.dyeableropes.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.matejhozlar.dyeableropes.client.ClientDyedStrandColors;
import dev.simulated_team.simulated.content.blocks.rope.rope_connector.RopeConnectorBlockEntity;
import dev.simulated_team.simulated.content.blocks.rope.rope_connector.RopeConnectorRenderer;
import net.createmod.catnip.render.SuperByteBuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RopeConnectorRenderer.class)
public class RopeConnectorRendererMixin {

    @ModifyExpressionValue(
            method = "renderSafe",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/createmod/catnip/render/SuperByteBuffer;light(I)Lnet/createmod/catnip/render/SuperByteBuffer;"
            )
    )
    private SuperByteBuffer dyeable_ropes$tintConnectorKnot(
            SuperByteBuffer buffer,
            @Local(argsOnly = true) RopeConnectorBlockEntity be
    ) {
        return buffer.color(ClientDyedStrandColors.tintForHolder(be.getRopeHolder()));
    }
}
