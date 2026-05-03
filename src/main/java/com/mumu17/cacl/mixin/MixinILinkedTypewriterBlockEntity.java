package com.mumu17.cacl.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import com.mumu17.cacl.mixin_interface.ILinkedTypewriterBlockEntityExtension;
import com.mumu17.cacl.mixin_interface.ILinkedTypewriterEntriesExtension;
import com.mumu17.cacl.util.DummyLevel;
import com.mumu17.cacl.util.LinkedTypewriterBlockEntityUtils;
import dev.simulated_team.simulated.content.blocks.redstone.linked_typewriter.LinkedTypewriterBlockEntity;
import dev.simulated_team.simulated.content.blocks.redstone.linked_typewriter.LinkedTypewriterEntries;
import dev.simulated_team.simulated.mixin_interface.PlayerTypewriterExtension;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Mixin(LinkedTypewriterBlockEntity.class)
@Debug(export = true)
public abstract class MixinILinkedTypewriterBlockEntity implements ILinkedTypewriterBlockEntityExtension {

    @Shadow
    private UUID currentUser;

    @Shadow
    private LinkedTypewriterEntries entryMap;

    @Shadow
    private String typedEntry;

    @Mutable
    @Shadow
    @Final
    private List<Integer> pressedKeys;

    @Unique
    public boolean runOnServer = false;

    @Unique
    public boolean clientCheck = false;

    public MixinILinkedTypewriterBlockEntity() {
        super();
    }

    @Override
    public void saveAdditional(ItemStack stack) {
        LinkedTypewriterBlockEntity self = (LinkedTypewriterBlockEntity) (Object) this;
        CustomData customData = stack.get(DataComponents.BLOCK_ENTITY_DATA);
        CompoundTag compoundTag = customData != null ? customData.copyTag() : new CompoundTag();
        if (this.currentUser != null) {
            compoundTag.putUUID("currentUser", this.currentUser);
        } else {
            compoundTag.remove("currentUser");
        }
        compoundTag.putString("typedEntry", this.typedEntry);
        if (self.getLevel() != null) {
            compoundTag.put("Keys", this.entryMap.saveKeys(self.getLevel().registryAccess()));
            compoundTag.putIntArray("PressedKeys", this.pressedKeys);
        }
        stack.set(DataComponents.BLOCK_ENTITY_DATA, CustomData.of(compoundTag));
    }

    @Override
    public void loadAdditional(ItemStack stack, Level level, BlockPos pos) {
        CustomData customData = stack.get(DataComponents.BLOCK_ENTITY_DATA);
        if (customData != null) {
            CompoundTag tag = customData.copyTag();
            if (tag.contains("currentUser")) {
                this.currentUser = tag.getUUID("currentUser");
            } else {
                this.currentUser = null;
            }
            this.typedEntry = tag.getString("typedEntry");
            this.entryMap = LinkedTypewriterEntries.readKeys(level.registryAccess(), tag.getList("Keys", 10), pos);
            List<Integer>  t_pressedKeys = Arrays.stream(tag.getIntArray("PressedKeys")).boxed().toList();
            this.pressedKeys = !t_pressedKeys.isEmpty() ? new ArrayList<>(t_pressedKeys) : new ArrayList<>();
        }
    }

    @Override
    public boolean isRunOnServer(DummyLevel dummyLevel) {
        boolean lastRunOnServer = this.runOnServer;
        setRunOn(dummyLevel);
        return !lastRunOnServer && this.runOnServer;
    }

    @Unique
    private void setRunOn(DummyLevel dummyLevel) {
        this.runOnServer = !dummyLevel.isClientSide();
    }

    public void setClientCheck(boolean b) {
        this.clientCheck = b;
    }

    @ModifyArg(method = "checkAndStartUsing", at = @At(value = "INVOKE", target = "Ldev/simulated_team/simulated/mixin_interface/PlayerTypewriterExtension;simulated$setCurrentTypewriter(Lnet/minecraft/core/BlockPos;)V"))
    public BlockPos cacl$setCurrentTypewriter(BlockPos pos, @Local(name = "player") PlayerTypewriterExtension player) {
        return pos != null ? pos : (player != null ? ((Player) player).blockPosition() : null);
    }

    @ModifyExpressionValue(
            method = {"tick", "checkAndStartUsing", "disconnectUser"},
            at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/Level;isClientSide:Z", opcode = Opcodes.GETFIELD)
    )
    public boolean cacl$isClientSide(boolean original) {
        BlockEntity self = (BlockEntity) (Object) this;
        DummyLevel dummyLevel = DummyLevel.getDummyLevelFor(self);
        return dummyLevel != null ? dummyLevel.isClientSide() : original;
    }

    @WrapWithCondition(method = "tick", at = @At(value = "INVOKE", target = "Ldev/simulated_team/simulated/content/blocks/redstone/linked_typewriter/LinkedTypewriterEntries;updateNetworks(Lnet/minecraft/world/level/Level;)V"))
    public boolean cacl$updateNetworks(LinkedTypewriterEntries instance, Level level) {
        BlockEntity self = (BlockEntity) (Object) this;
        DummyLevel dummyLevel = DummyLevel.getDummyLevelFor(self);
        if (dummyLevel != null) {
            (((ILinkedTypewriterEntriesExtension)entryMap).cacl$setIsClientSide(false))
                    .updateNetworks(level);
        }
        return dummyLevel == null;
    }

    @Definition(id = "currentUser", field = "Ldev/simulated_team/simulated/content/blocks/redstone/linked_typewriter/LinkedTypewriterBlockEntity;currentUser:Ljava/util/UUID;")
    @Expression("this.currentUser == null")
    @ModifyExpressionValue(method = "checkAndStartUsing", at = @At("MIXINEXTRAS:EXPRESSION"))
    public boolean cacl$isCurrentUserNull(boolean original, @Local(argsOnly = true) UUID uuid) {
        LinkedTypewriterBlockEntity lbe = ((LinkedTypewriterBlockEntity)(Object)this);
        Level level = lbe.getLevel();
        if (level != null) {
            Player player = level.getPlayerByUUID(uuid);
            if (player != null) {
                DummyLevel  dummyLevel = DummyLevel.getDummyLevelFor(player, false);
                if (dummyLevel != null) {
                    boolean b = isRunOnServer(dummyLevel);
                    return b ? this.clientCheck : original;
                }
            }
        }
        return original;
    }

    @ModifyExpressionValue(method = "checkAndStartUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getBlockEntity(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/entity/BlockEntity;"))
    public BlockEntity cacl$getBlockEntity(BlockEntity original, @Local(name = "player") PlayerTypewriterExtension player) {
        if (original != null) return original;
        if (player != null) {
            Player p = (Player) player;
            return LinkedTypewriterBlockEntityUtils.getLinkedTypewriterBlockEntityAt(p.blockPosition(), p);
        }
        return null;
    }

    @Inject(method = "disconnectUser", at = @At("HEAD"))
    public void cacl$disconnectUser(CallbackInfo ci) {
        LinkedTypewriterBlockEntity lbe = ((LinkedTypewriterBlockEntity)(Object)this);
        if (lbe != null) {
            DummyLevel dummyLevel = DummyLevel.getDummyLevelFor(lbe);
            if (dummyLevel != null) {
                Level level = dummyLevel.getLevel();
                if (level != null) {
                    Player player = level.getPlayerByUUID(this.currentUser);
                    if (player != null) {
                        dummyLevel.removeBlockEntity(player.blockPosition(), player);
                    }
                }
            }
        }
    }
}
