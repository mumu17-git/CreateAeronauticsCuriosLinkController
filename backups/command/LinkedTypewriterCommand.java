package com.mumu17.cacl.command;

import com.mumu17.cacl.CACL;
import com.mumu17.cacl.util.LinkedTypewriterExternalTrigger;
import com.mumu17.cacl.mixin_interface.ILinkedTypewriterBlockEntityExtension;
import dev.simulated_team.simulated.content.blocks.redstone.linked_typewriter.LinkedTypewriterItem;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.UUID;

/**
 * コマンドから虚想LinkedTypewriter を操作する実装例。
 *
 * 使用例:
 * /linkedtypewriter use @s
 * /linkedtypewriter tick @s
 * /linkedtypewriter setuser @s <player_name>
 */
@EventBusSubscriber(modid = "cacl", bus = EventBusSubscriber.Bus.MOD)
public class LinkedTypewriterCommand {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        var dispatcher = event.getDispatcher();

        dispatcher.register(
                Commands.literal("linkedtypewriter")
                        .then(Commands.literal("use")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(ctx -> executeUse(
                                                ctx.getSource(),
                                                EntityArgument.getPlayer(ctx, "player")
                                        ))
                                )
                        )
                        .then(Commands.literal("tick")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(ctx -> executeTick(
                                                ctx.getSource(),
                                                EntityArgument.getPlayer(ctx, "player")
                                        ))
                                )
                        )
                        .then(Commands.literal("status")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(ctx -> executeStatus(
                                                ctx.getSource(),
                                                EntityArgument.getPlayer(ctx, "player")
                                        ))
                                )
                        )
        );
    }

    /**
     * /linkedtypewriter use @s
     * 指定プレイヤーのLinkedTypewriter アイテムの虚想BEを操作開始。
     */
    private static int executeUse(CommandSourceStack source, Player targetPlayer) {
        ItemStack stack = targetPlayer.getItemInHand(InteractionHand.MAIN_HAND);

        if (!(stack.getItem() instanceof LinkedTypewriterItem)) {
            source.sendFailure(Component.literal("プレイヤーが LinkedTypewriter を持っていません"));
            return 0;
        }

        boolean success = LinkedTypewriterExternalTrigger.triggerLinkedTypewriter(
                stack,
                targetPlayer.level(),
                targetPlayer,
                be -> {
                    CACL.LOGGER.debug("コマンドから虚想BEを操作: " + targetPlayer.getName().getString());
                    // be.checkAndStartUsing(targetPlayer, stack);
                }
        );

        if (success) {
            source.sendSuccess(
                    () -> Component.literal("✓ " + targetPlayer.getName().getString() +
                            " の虚想LinkedTypewriter を操作しました"),
                    true
            );
            return 1;
        } else {
            source.sendFailure(Component.literal("✗ 虚想LinkedTypewriter の操作に失敗しました"));
            return 0;
        }
    }

    /**
     * /linkedtypewriter tick @s
     * 指定プレイヤーのLinkedTypewriter 虚想BEのTickシミュレーションを実行。
     */
    private static int executeTick(CommandSourceStack source, Player targetPlayer) {
        ItemStack stack = targetPlayer.getItemInHand(InteractionHand.MAIN_HAND);

        if (!(stack.getItem() instanceof LinkedTypewriterItem)) {
            source.sendFailure(Component.literal("プレイヤーが LinkedTypewriter を持っていません"));
            return 0;
        }

        boolean success = LinkedTypewriterExternalTrigger.simulateVirtualTick(
                stack,
                targetPlayer.level(),
                targetPlayer
        );

        if (success) {
            source.sendSuccess(
                    () -> Component.literal("✓ " + targetPlayer.getName().getString() +
                            " の虚想LinkedTypewriter Tick を実行しました"),
                    true
            );
            return 1;
        } else {
            source.sendFailure(Component.literal("✗ Tick シミュレーションの実行に失敗しました"));
            return 0;
        }
    }

    /**
     * /linkedtypewriter status @s
     * 指定プレイヤーのLinkedTypewriter 虚想BEの状態を表示。
     */
    private static int executeStatus(CommandSourceStack source, Player targetPlayer) {
        ItemStack stack = targetPlayer.getItemInHand(InteractionHand.MAIN_HAND);

        if (!(stack.getItem() instanceof LinkedTypewriterItem)) {
            source.sendFailure(Component.literal("プレイヤーが LinkedTypewriter を持っていません"));
            return 0;
        }

        if (!LinkedTypewriterExternalTrigger.isValidVirtualLinkedTypewriter(stack)) {
            source.sendFailure(Component.literal("このアイテムは有効な虚想LinkedTypewriter ではありません"));
            return 0;
        }

        var context = LinkedTypewriterExternalTrigger.getVirtualContext(
                stack,
                targetPlayer.level(),
                targetPlayer,
                targetPlayer.blockPosition()
        );

        if (context != null) {
            var be = context.blockEntity();
            var ext = (ILinkedTypewriterBlockEntityExtension) be;
            var currentUser = (UUID)null;// ext.getCurrentUser();

            source.sendSuccess(
                    () -> Component.literal("LinkedTypewriter 状態 [" + targetPlayer.getName().getString() + "]:\n" +
                            "  位置: " + be.getBlockPos() + "\n" +
                            "  現在のユーザー: " + (currentUser != null ? currentUser : "なし")),
                    false
            );
            return 1;
        } else {
            source.sendFailure(Component.literal("虚想BEのコンテキスト取得に失敗しました"));
            return 0;
        }
    }
}

