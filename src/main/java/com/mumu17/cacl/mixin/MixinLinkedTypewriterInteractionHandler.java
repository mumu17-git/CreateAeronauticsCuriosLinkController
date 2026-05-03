package com.mumu17.cacl.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import com.mumu17.cacl.network.RemoteTypewriterKeyInteractionPacket;
import com.mumu17.cacl.util.DummyLevel;
import dev.simulated_team.simulated.content.blocks.redstone.linked_typewriter.LinkedTypewriterBlockEntity;
import dev.simulated_team.simulated.content.blocks.redstone.linked_typewriter.LinkedTypewriterInteractionHandler;
import dev.simulated_team.simulated.network.packets.linked_typewriter.TypewriterKeyInteractionPacket;
import foundry.veil.api.network.VeilPacketManager;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(LinkedTypewriterInteractionHandler.class)
public class MixinLinkedTypewriterInteractionHandler {
    @ModifyArg(method = "onKeyPress", at = @At(value = "INVOKE", target = "Lfoundry/veil/api/network/VeilPacketManager$PacketSink;sendPacket([Lnet/minecraft/network/protocol/common/custom/CustomPacketPayload;)V", ordinal = 1), index = 0)
    private static CustomPacketPayload[] checkSendPacket(CustomPacketPayload[] payloads, @Local(name = "be") LinkedTypewriterBlockEntity be, @Local(name = "minecraft") Minecraft minecraft, @Local(argsOnly = true, index = 0) int key, @Local(argsOnly = true, index = 1) int scanCode, @Local(argsOnly = true, index = 2) int action) {
        if (minecraft.player != null && DummyLevel.getDummyLevelFor(be) != null) {
            return new CustomPacketPayload[]{new RemoteTypewriterKeyInteractionPacket(minecraft.player.blockPosition(), key, scanCode, action)};
        }
        return payloads;
    }
}
