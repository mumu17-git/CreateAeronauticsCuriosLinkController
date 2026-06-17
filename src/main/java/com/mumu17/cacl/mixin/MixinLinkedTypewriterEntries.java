package com.mumu17.cacl.mixin;

import com.mumu17.cacl.mixin_interface.ILinkedTypewriterEntriesExtension;
import dev.simulated_team.simulated.content.blocks.redstone.linked_typewriter.LinkedTypewriterEntries;
import org.spongepowered.asm.mixin.*;

@Mixin(LinkedTypewriterEntries.class)
public class MixinLinkedTypewriterEntries implements ILinkedTypewriterEntriesExtension {

    @Unique
    boolean cacl$isClientSide = true;

    @Override
    public LinkedTypewriterEntries cacl$setIsClientSide(boolean b) {
        cacl$isClientSide = b;
        return (LinkedTypewriterEntries)(Object) this;
    }
}
