package com.mumu17.cacl.mixin_interface;

import com.mumu17.cacl.util.DummyLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface ILinkedTypewriterBlockEntityExtension {
    void saveAdditional(ItemStack stack);

    void loadAdditional(ItemStack stack, Level level, BlockPos pos);

    boolean isRunOnServer(DummyLevel dummyLevel);

    void setClientCheck(boolean clientCheck);
}
