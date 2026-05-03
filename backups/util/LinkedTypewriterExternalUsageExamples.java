package com.mumu17.cacl.util;

import com.mumu17.cacl.CACL;
import dev.simulated_team.simulated.content.blocks.redstone.linked_typewriter.LinkedTypewriterBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * LinkedTypewriter の虚想BE操作についての実装例。
 * 外部ハンドラから呼び出す時のワンライナー例集。
 */
public final class LinkedTypewriterExternalUsageExamples {

    private LinkedTypewriterExternalUsageExamples() {}

    // ===== 基本パターン =====

    /**
     * 例1: プレイヤーが持つItemStackの虚想BEに対して、
     * ユーザー割り当てと状態保存を一度に行う。
     */
    public static void example_setUserAndSave(Player player, ItemStack stack, Level level) {
        LinkedTypewriterExternalTrigger.setVirtualCurrentUser(
                stack,
                level,
                player,
                player.getUUID()
        );
    }

    /**
     * 例2: カスタム操作を実行して、状態をItemStackに保存。
     * ラムダ式で操作を定義。
     */
    public static void example_customOperation(Player player, ItemStack stack, Level level) {
        LinkedTypewriterExternalTrigger.triggerLinkedTypewriter(
                stack,
                level,
                player,
                be -> {
                    // 虚想BE上の任意の操作
                    // be.checkAndStartUsing(player, stack);  // 例: ユーザー接続チェック
                    // be.powered = true;  // 例: BlockEntity フィールド操作
                    CACL.LOGGER.debug("仮想BEに対してカスタム操作を実行");
                }
        );
    }

    /**
     * 例3: 虚想BEのシミュレーションTick を実行。
     * サーバー/クライアント側でのTick処理。
     */
    public static void example_simulateTick(Player player, ItemStack stack, Level level) {
        LinkedTypewriterExternalTrigger.simulateVirtualTick(stack, level, player);
    }

    // ===== 位置指定パターン =====

    /**
     * 例4: 指定された位置に虚想BEを配置してから操作。
     * 複数の異なる位置で虚想BEを管理したい場合に有効。
     */
    public static void example_positionedOperation(
            Player player,
            ItemStack stack,
            Level level,
            net.minecraft.core.BlockPos customPos
    ) {
        LinkedTypewriterExternalTrigger.triggerLinkedTypewriterAt(
                stack,
                level,
                player,
                customPos,
                be -> {
                    CACL.LOGGER.debug("位置 " + be.getBlockPos() + " に虚想BEを配置");
                    // be.checkAndStartUsing(player, stack);
                }
        );
    }

    // ===== コンテキスト経由の複数操作パターン =====

    /**
     * 例5: VirtualLinkedTypewriterContext を保持して、
     * 複数回の操作を同じコンテキスト上で実行。
     * 重い初期化を1回だけ行いたい場合に有効。
     */
    public static void example_multipleOperationsWithContext(
            Player player,
            ItemStack stack,
            Level level,
            net.minecraft.core.BlockPos pos
    ) {
        var context = LinkedTypewriterExternalTrigger.getVirtualContext(
                stack, level, player, pos
        );

        if (context != null) {
            LinkedTypewriterBlockEntity be = context.blockEntity();
            var ext = (com.mumu17.cacl.mixin_interface.ILinkedTypewriterBlockEntityExtension) be;

            // 操作1: ユーザー設定
//            ext.setCurrentUser(player.getUUID());

            // 操作2: ロード (ItemStack からNBT復元)
            // ext.loadAdditional(stack, level, pos);  // 不要: context作成時に自動実行

            // 操作3: シミュレーション
            ext.simulateVirtualTick(level, stack);

            // 操作4: 保存 (NBT → ItemStack)
            ext.saveAdditional(stack);
        }
    }

    // ===== 安全性チェックパターン =====

    /**
     * 例6: ItemStack が有効な虚想LinkedTypewriter かどうかを
     * 事前に確認してから操作。
     */
    public static void example_safeOperationWithValidation(
            Player player,
            ItemStack stack,
            Level level
    ) {
        // Step 1: ItemStack の検証
        if (!LinkedTypewriterExternalTrigger.isValidVirtualLinkedTypewriter(stack)) {
            CACL.LOGGER.debug("このアイテムは虚想LinkedTypewriter ではありません");
            return;
        }

        // Step 2: 安全に操作を実行
        boolean success = LinkedTypewriterExternalTrigger.triggerLinkedTypewriter(
                stack,
                level,
                player,
                be -> {
                    // 虚想BE上の安全な操作
                    CACL.LOGGER.debug("虚想BEは正常に動作しています");
                }
        );

        if (!success) {
            CACL.LOGGER.debug("虚想BEの操作に失敗しました");
        }
    }

    // ===== 外部イベントハンドラ統合パターン =====

    /**
     * 例7: イベントハンドラから呼び出す例。
     * (例) コマンド実行、Right-Click ハンドラ等。
     */
    public static void example_eventHandlerIntegration(
            Player player,
            ItemStack stack,
            Level level,
            String eventName
    ) {
        CACL.LOGGER.debug("イベント発生: " + eventName);

        // 有効性チェック
        if (!LinkedTypewriterExternalTrigger.isValidVirtualLinkedTypewriter(stack)) {
            return;
        }

        // 虚想BEに対して操作を委譲
        boolean result = LinkedTypewriterExternalTrigger.triggerLinkedTypewriter(
                stack,
                level,
                player,
                be -> {
                    switch (eventName) {
                        case "ON_KEY_PRESS":
                            // キー入力イベント時の処理
                            CACL.LOGGER.debug("キー入力を虚想BEで処理");
                            break;

                        case "ON_USE":
                            // アイテム使用時の処理
                            // be.checkAndStartUsing(player, stack);
                            CACL.LOGGER.debug("アイテム使用を虚想BEで処理");
                            break;

                        case "ON_TICK":
                            // Tick 時の処理
                            // be.simulateVirtualTick(level, stack);
                            CACL.LOGGER.debug("Tick処理を虚想BEで実行");
                            break;
                    }
                }
        );

        if (!result) {
            CACL.LOGGER.debug("イベント処理に失敗しました: " + eventName);
        }
    }
}

