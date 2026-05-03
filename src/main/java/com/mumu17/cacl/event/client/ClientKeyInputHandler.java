package com.mumu17.cacl.event.client;

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
import net.neoforged.neoforge.client.event.InputEvent;

@EventBusSubscriber(
        modid = CACL.MODID,
        value = Dist.CLIENT
)
public class ClientKeyInputHandler {


    @SubscribeEvent
    public static void onKey(InputEvent.Key event) {
//        onAfterKeyPress(
//                event.getKey(),
//                event.getScanCode(),
//                event.getAction(),
//                event.getModifiers()
//        );
    }


    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (ModKeyBindings.REMOTE_USE.consumeClick()) {
            onRemoteUseKeyPressed();
        }
        LinkedTypewriterInteractionHandler.tick();
    }

    public static void onAfterKeyPress(int key, int scanCode, int action, int modifiers) {
        LinkedTypewriterInteractionHandler.onKeyPress(key, scanCode, action, modifiers);
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
        CACL.LOGGER.debug("虚想LinkedTypewriter を使用中: {}, {}", player.getName().getString(), dummyLevel != null && dummyLevel.isClientSide());
        boolean check = lbe.checkAndStartUsing(player.getUUID());
        if (!check) {
            lbe.disconnectUser();
        } else {
            if (dummyLevel != null) {
                ((ILinkedTypewriterBlockEntityExtension) lbe).setClientCheck(true);
                dummyLevel.setBlockEntity(player.blockPosition(), lbe, player);
            }
        }

        //linkedTypewriterBlockEntity.checkAndStartUsing(player.getUUID());
        mc.getConnection().send(new RemoteUsePacket());

        // LinkedTypewriterInteractionHandler.associateTypewriter(linkedTypewriterBlockEntity);

        /*Level level = player.level();
        BlockPos pos = CoordinateRecorderItem.getStoredPos(linkedTypewriterStack);

        if (pos == null) return;

        if (!level.hasChunkAt(pos)) return;

        BlockState state = level.getBlockState(pos);
        if (state.isAir()) return;

        BlockHitResult hit = new BlockHitResult(
                Vec3.atCenterOf(pos),
                Direction.UP,
                pos,
                false
        );

        state.useItemOn(ItemStack.EMPTY, level, player, InteractionHand.MAIN_HAND, hit);

        mc.getConnection().send(new RemoteUsePacket(pos));*/
    }
}