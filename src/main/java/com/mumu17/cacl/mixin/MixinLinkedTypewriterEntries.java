package com.mumu17.cacl.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mumu17.cacl.CACL;
import com.mumu17.cacl.mixin_interface.ILinkedTypewriterEntriesExtension;
import dev.simulated_team.simulated.content.blocks.redstone.linked_typewriter.LinkedTypewriterEntries;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LinkedTypewriterEntries.class)
@Debug(export = true)
public class MixinLinkedTypewriterEntries implements ILinkedTypewriterEntriesExtension {

    @Unique
    boolean cacl$isClientSide = true;

    @ModifyExpressionValue(
            method = {"updateNetworks"},
            at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/Level;isClientSide:Z", opcode = Opcodes.GETFIELD)
    )
    public boolean cacl$isClientSide(boolean original) {
        return original;
    }

    @Inject(method = "updateNetworks", at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/redstone/link/RedstoneLinkNetworkHandler;addToNetwork(Lnet/minecraft/world/level/LevelAccessor;Lcom/simibubi/create/content/redstone/link/IRedstoneLinkable;)V"))
    public void cacl$beforeAddToNetwork(CallbackInfo ci) {
        CACL.LOGGER.debug("Adding to network, setting client side to false");
    }

    @Override
    public LinkedTypewriterEntries cacl$setIsClientSide(boolean b) {
        cacl$isClientSide = b;
        return (LinkedTypewriterEntries)(Object) this;
    }
}
