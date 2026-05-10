package com.mumu17.cacl.event.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mumu17.cacl.CACL;
import com.mumu17.cacl.mixin_interface.ILinkedTypewriterBlockEntityExtension;
import com.mumu17.cacl.network.RemoteUsePacket;
import com.mumu17.cacl.registry.ModKeyBindings;
import com.mumu17.cacl.util.CuriosUtils;
import com.mumu17.cacl.util.DummyLevel;
import com.mumu17.cacl.util.LinkedTypewriterBlockEntityUtils;
import dev.simulated_team.simulated.content.blocks.redstone.linked_typewriter.LinkedTypewriterBlockEntity;
import dev.simulated_team.simulated.content.blocks.redstone.linked_typewriter.LinkedTypewriterInteractionHandler;
import dev.simulated_team.simulated.content.blocks.redstone.linked_typewriter.LinkedTypewriterItem;
import dev.simulated_team.simulated.mixin_interface.PlayerTypewriterExtension;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(
        modid = CACL.MODID,
        value = Dist.CLIENT
)
public class ClientKeyInputHandler {

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (ModKeyBindings.REMOTE_USE.consumeClick() && !isAnyKeyExceptRemoteUseDown()) {
            onRemoteUseKeyPressed();
        }
        LinkedTypewriterInteractionHandler.tick();
    }



    private static boolean isAnyKeyExceptRemoteUseDown() {
        Minecraft mc = Minecraft.getInstance();
        long window = mc.getWindow().getWindow();

        for (int key = GLFW.GLFW_KEY_SPACE; key <= GLFW.GLFW_KEY_LAST; key++) {
            if (key == ModKeyBindings.REMOTE_USE.getKey().getValue()) continue;

            if (InputConstants.isKeyDown(window, key)) {
                return true;
            }
        }
        return false;
    }



    private static void onRemoteUseKeyPressed() {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;

        if (player == null) return;

        if (mc.getConnection() == null) return;

        ItemStack linkedTypewriterStack = CuriosUtils.getEquippedLinkedTypewriter(player);

        if (linkedTypewriterStack == null || !(linkedTypewriterStack.getItem() instanceof LinkedTypewriterItem)) return;

        LinkedTypewriterBlockEntity lbe = LinkedTypewriterBlockEntityUtils.getLinkedTypewriterBlockEntityAt(player.blockPosition(), player);

        if (lbe == null) {
            LinkedTypewriterBlockEntityUtils.VirtualLinkedTypewriterContext context =
                LinkedTypewriterBlockEntityUtils.createVirtualLinkedTypewriterContext(linkedTypewriterStack, player.level(), player);
            if (context == null) return;

            lbe = context.blockEntity();
        }

        ((PlayerTypewriterExtension)player).simulated$setCurrentTypewriter(player.blockPosition());
        LinkedTypewriterInteractionHandler.associateTypewriter(lbe);
        DummyLevel dummyLevel = DummyLevel.getDummyLevelFor(player);
        if (dummyLevel != null) {
            dummyLevel.setBlockEntity(player.blockPosition(), lbe, player);
        }
        boolean check = lbe.checkAndStartUsing(player.getUUID());
        if (!check) {
            lbe.disconnectUser();
        } else {
            if (dummyLevel != null) {
                ((ILinkedTypewriterBlockEntityExtension) lbe).setClientCheck(true);
                dummyLevel.setBlockEntity(player.blockPosition(), lbe, player);
            }
        }

        mc.getConnection().send(new RemoteUsePacket());
    }
}