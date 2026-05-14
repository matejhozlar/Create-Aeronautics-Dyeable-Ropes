package dev.matejhozlar.dyeableropes.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import dev.matejhozlar.dyeableropes.client.ClientDyedStrandColors;
import dev.matejhozlar.dyeableropes.client.DyeableRopesPartialModels;
import dev.simulated_team.simulated.content.blocks.rope.RopeStrandHolderBehavior;
import dev.simulated_team.simulated.content.blocks.rope.strand.client.RopeStrandRenderer;
import net.createmod.catnip.render.SuperByteBuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RopeStrandRenderer.class)
public class RopeStrandRendererMixin {

    @Inject(method = "render", at = @At("HEAD"))
    private static void dyeable_ropes$resolveTint(
            CallbackInfo ci,
            @Local(argsOnly = true) RopeStrandHolderBehavior ropeHolder,
            @Share("dyeable_ropes$tint") LocalIntRef tint
    ) {
        tint.set(ClientDyedStrandColors.tintForHolder(ropeHolder));
    }

    @ModifyExpressionValue(
            method = "render",
            at = @At(
                    value = "FIELD",
                    target = "Ldev/simulated_team/simulated/index/SimPartialModels;ROPE:Ldev/engine_room/flywheel/lib/model/baked/PartialModel;"
            )
    )
    private static PartialModel dyeable_ropes$swapRopeModel(
            PartialModel original,
            @Local(argsOnly = true) RopeStrandHolderBehavior ropeHolder
    ) {
        return ClientDyedStrandColors.hasColor(ropeHolder) ? DyeableRopesPartialModels.ROPE : original;
    }

    @ModifyExpressionValue(
            method = "render",
            at = @At(
                    value = "FIELD",
                    target = "Ldev/simulated_team/simulated/index/SimPartialModels;ROPE_KNOT:Ldev/engine_room/flywheel/lib/model/baked/PartialModel;"
            )
    )
    private static PartialModel dyeable_ropes$swapKnotModel(
            PartialModel original,
            @Local(argsOnly = true) RopeStrandHolderBehavior ropeHolder
    ) {
        return ClientDyedStrandColors.hasColor(ropeHolder) ? DyeableRopesPartialModels.ROPE_KNOT : original;
    }

    @ModifyExpressionValue(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/createmod/catnip/render/SuperByteBuffer;light(I)Lnet/createmod/catnip/render/SuperByteBuffer;"
            )
    )
    private static SuperByteBuffer dyeable_ropes$tintStrand(
            SuperByteBuffer buffer,
            @Share("dyeable_ropes$tint") LocalIntRef tint
    ) {
        return buffer.color(tint.get());
    }
}
