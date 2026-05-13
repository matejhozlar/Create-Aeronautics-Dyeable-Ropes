package dev.matejhozlar.dyeableropes.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import dev.matejhozlar.dyeableropes.client.ClientDyedStrandColors;
import dev.matejhozlar.dyeableropes.client.DyeableRopesPartialModels;
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
                    value = "FIELD",
                    target = "Ldev/simulated_team/simulated/index/SimPartialModels;ROPE_CONNECTOR_KNOT:Ldev/engine_room/flywheel/lib/model/baked/PartialModel;"
            )
    )
    private PartialModel dyeable_ropes$swapConnectorKnotModel(
            PartialModel original,
            @Local(argsOnly = true) RopeConnectorBlockEntity be
    ) {
        return ClientDyedStrandColors.hasColor(be.getRopeHolder()) ? DyeableRopesPartialModels.ROPE_CONNECTOR_KNOT : original;
    }

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
