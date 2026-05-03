package com.mumu17.cacl.event.external;

import com.mumu17.cacl.CACL;
import com.mumu17.cacl.util.LinkedTypewriterExternalTrigger;
import dev.simulated_team.simulated.content.blocks.redstone.linked_typewriter.LinkedTypewriterItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * 外部イベントから虚想LinkedTypewriter を操作する実装例。
 * 実際のプロジェクトに統合する際のテンプレートとして使用してください。
 */
@EventBusSubscriber(modid = "cacl", bus = EventBusSubscriber.Bus.GAME)
public class ExternalLinkedTypewriterEventHandler {

    /**
     * プレイヤーがアイテムをRight-Clickした時に虚想LinkedTypewriter を操作。
     * ItemStack が LinkedTypewriter の場合、虚想BEに useItemOn を委譲します。
     */
    @SubscribeEvent
    public static void onPlayerInteractWithItem(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        ItemStack itemInHand = player.getItemInHand(InteractionHand.MAIN_HAND);

        // LinkedTypewriter アイテムのチェック
        if (!(itemInHand.getItem() instanceof LinkedTypewriterItem)) {
            return;
        }

        // 虚想BEに対してユーザー割り当てと使用開始を試みる
        boolean success = LinkedTypewriterExternalTrigger.triggerLinkedTypewriter(
                itemInHand,
                player.level(),
                player,
                be -> {
                    // 虚想BEに対して対話開始処理を実行
                    CACL.LOGGER.debug("虚想LinkedTypewriter を使用中: " + player.getName().getString());
                    // be.checkAndStartUsing(player, itemInHand);
                }
        );

        if (success) {
            event.setCanceled(true);  // デフォルト動作をキャンセル
        }
    }

    /**
     * プレイヤーがアイテムをShiftキー+使用した時の処理。
     * 虚想BE をシミュレーション Tick させる。
     */
    @SubscribeEvent
    public static void onPlayerShiftUse(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();

        if (!player.isShiftKeyDown()) {
            return;
        }

        ItemStack itemInHand = player.getItemInHand(InteractionHand.MAIN_HAND);

        if (!(itemInHand.getItem() instanceof LinkedTypewriterItem)) {
            return;
        }

        // 虚想BEのTickシミュレーションを実行
        LinkedTypewriterExternalTrigger.simulateVirtualTick(
                itemInHand,
                player.level(),
                player
        );
    }
}

