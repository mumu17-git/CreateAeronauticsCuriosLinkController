package com.mumu17.cacl.item;


import dev.simulated_team.simulated.index.SimBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

public class CoordinateRecorderItem extends Item {

    public CoordinateRecorderItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(
            Level level,
            Player player,
            @NotNull InteractionHand hand
    ) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide)
            return InteractionResultHolder.pass(stack);

        if (!player.isShiftKeyDown())
            return InteractionResultHolder.pass(stack);


        BlockHitResult hit = rayTrace(level, player, 20);

        if (hit.getType() == HitResult.Type.BLOCK) {
            BlockPos pos = hit.getBlockPos();

            if (level.getBlockState(pos).getBlock() != SimBlocks.LINKED_TYPEWRITER.get())
                return InteractionResultHolder.pass(stack);

            stack.update(
                    DataComponents.CUSTOM_DATA,
                    CustomData.EMPTY,
                    data -> {
                        CompoundTag tag = data.copyTag();
                        tag.putInt("X", pos.getX());
                        tag.putInt("Y", pos.getY());
                        tag.putInt("Z", pos.getZ());
                        return CustomData.of(tag);
                    }
            );

            player.sendSystemMessage(
                    Component.translatable("chat.cacl.recorded_position")
                            .append(": ").append(
                    Component.literal(pos.toShortString()))
            );
        }

        return InteractionResultHolder.success(stack);
    }

    @Override
    @ParametersAreNonnullByDefault
    public void appendHoverText(ItemStack stack, TooltipContext tooltipContext, List<Component> tooltip, TooltipFlag flag) {
        BlockPos pos = getStoredPos(stack);
        if (pos != null) {
            tooltip.add(Component.literal(
                    "Stored: " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ()
            ));
        }
    }

    private BlockHitResult rayTrace(Level level, Player player, double range) {
        Vec3 eye = player.getEyePosition(1.0F);
        Vec3 look = player.getViewVector(1.0F);
        Vec3 end = eye.add(look.scale(range));

        return level.clip(new ClipContext(
                eye,
                end,
                ClipContext.Block.OUTLINE,
                ClipContext.Fluid.NONE,
                player
        ));
    }

    public static BlockPos getStoredPos(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return null;

        CompoundTag tag = data.copyTag();
        if (!tag.contains("X")) return null;

        return new BlockPos(
                tag.getInt("X"),
                tag.getInt("Y"),
                tag.getInt("Z")
        );
    }

}

