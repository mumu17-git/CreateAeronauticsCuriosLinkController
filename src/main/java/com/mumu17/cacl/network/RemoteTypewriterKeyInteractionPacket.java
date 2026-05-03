package com.mumu17.cacl.network;

import com.mumu17.cacl.CACL;
import com.mumu17.cacl.mixin_interface.ILinkedTypewriterBlockEntityExtension;
import com.mumu17.cacl.util.CuriosUtils;
import com.mumu17.cacl.util.DummyLevel;
import com.mumu17.cacl.util.LinkedTypewriterBlockEntityUtils;
import dev.simulated_team.simulated.content.blocks.redstone.linked_typewriter.LinkedTypewriterBlockEntity;
import dev.simulated_team.simulated.content.blocks.redstone.linked_typewriter.LinkedTypewriterItem;
import foundry.veil.api.network.handler.ServerPacketContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.jetbrains.annotations.NotNull;

public record RemoteTypewriterKeyInteractionPacket(BlockPos interactionPos, int key, int scanCode, int action /*If it's being pressed etc*/) implements CustomPacketPayload {
    public static final Type<RemoteTypewriterKeyInteractionPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(CACL.MODID, "key_interaction"));

    public static final StreamCodec<ByteBuf, RemoteTypewriterKeyInteractionPacket> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, RemoteTypewriterKeyInteractionPacket::interactionPos,
            ByteBufCodecs.INT, RemoteTypewriterKeyInteractionPacket::key,
            ByteBufCodecs.INT, RemoteTypewriterKeyInteractionPacket::scanCode,
            ByteBufCodecs.INT, RemoteTypewriterKeyInteractionPacket::action,
            RemoteTypewriterKeyInteractionPacket::new);

    public void handle(final ServerPacketContext ctx) {
        Player player = (Player) ctx.player();

        ItemStack linkedTypewriterStack = CuriosUtils.getEquippedLinkedTypewriter(player);

        if (linkedTypewriterStack == null || !(linkedTypewriterStack.getItem() instanceof LinkedTypewriterItem))
            return;

        LinkedTypewriterBlockEntity lbe = LinkedTypewriterBlockEntityUtils.getLinkedTypewriterBlockEntityAt(player.blockPosition(), player);

        CustomData blockEntityData = linkedTypewriterStack.get(DataComponents.BLOCK_ENTITY_DATA);
        if (lbe != null && blockEntityData != null) {
            lbe.onKeyInteraction(player.getUUID(), null, this.key, this.action == 1);

            ctx.server().execute(() -> {
                DummyLevel dummyLevel = DummyLevel.getDummyLevelFor(player);
                if (dummyLevel != null) {
                    dummyLevel.setBlockEntity(player.blockPosition(), lbe, player);
                }
                lbe.getTypewriterEntries().updateNetworks(player.level());
                ((ILinkedTypewriterBlockEntityExtension) lbe).saveAdditional(linkedTypewriterStack);
            });
        }

    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
