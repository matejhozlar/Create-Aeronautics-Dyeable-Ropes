package dev.matejhozlar.dyeableropes.client;

import com.simibubi.create.foundation.utility.RaycastHelper;
import dev.matejhozlar.dyeableropes.DyeableRopes;
import dev.matejhozlar.dyeableropes.network.DyeStrandPayload;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import dev.simulated_team.simulated.content.blocks.rope.strand.client.ClientLevelRopeManager;
import dev.simulated_team.simulated.content.blocks.rope.strand.client.ZiplineClientManager;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Vector3d;

import java.util.UUID;

@EventBusSubscriber(modid = DyeableRopes.MODID, value = Dist.CLIENT)
public final class DyeableRopesClientEvents {

    private static final double HALF_THICKNESS = 4.0 / 16.0;

    private DyeableRopesClientEvents() {}

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientDyedStrandColors.clear();
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (!event.getLevel().isClientSide()) return;

        ItemStack stack = event.getItemStack();
        if (!(stack.getItem() instanceof DyeItem dye)) return;

        Player player = event.getEntity();
        UUID strandId = raycastForStrand(player);
        if (strandId == null) return;

        event.setCanceled(true);
        event.setCancellationResult(net.minecraft.world.InteractionResult.SUCCESS);
        PacketDistributor.sendToServer(new DyeStrandPayload(strandId, dye.getDyeColor().getId(), event.getHand()));
        player.swing(event.getHand());
    }

    private static UUID raycastForStrand(Player player) {
        if (player.level() == null) return null;
        ClientLevelRopeManager ropeManager = ClientLevelRopeManager.getOrCreate(player.level());

        double reach = player.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE) + 1;
        Vector3d from = JOMLConversion.toJOML(player.getEyePosition());
        Vec3 traceTarget = RaycastHelper.getTraceTarget(player, reach, JOMLConversion.toMojang(from));
        Vector3d to = JOMLConversion.toJOML(traceTarget);

        return ZiplineClientManager.raycastRope(ropeManager, from, to, Float.MAX_VALUE, HALF_THICKNESS);
    }
}
