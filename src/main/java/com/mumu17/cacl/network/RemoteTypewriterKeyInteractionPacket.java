package com.mumu17.cacl.network;

import com.mumu17.cacl.CACL;
import com.mumu17.cacl.mixin_interface.ILinkedTypewriterBlockEntityExtension;
import com.mumu17.cacl.util.CuriosUtils;
import com.mumu17.cacl.util.DummyLevel;
import com.mumu17.cacl.util.LinkedTypewriterBlockEntityUtils;
import dev.simulated_team.simulated.Simulated;
import dev.simulated_team.simulated.content.blocks.redstone.linked_typewriter.LinkedTypewriterBlockEntity;
import dev.simulated_team.simulated.content.blocks.redstone.linked_typewriter.LinkedTypewriterEntries;
import dev.simulated_team.simulated.content.blocks.redstone.linked_typewriter.LinkedTypewriterItem;
import dev.simulated_team.simulated.index.SimRegistries;
import foundry.veil.api.network.handler.ServerPacketContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.TickTask;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

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

//        LinkedTypewriterBlockEntityUtils.VirtualLinkedTypewriterContext context =
//                LinkedTypewriterBlockEntityUtils.createVirtualLinkedTypewriterContext(linkedTypewriterStack, player.level(), player);
//        if (context == null) return;
//        LinkedTypewriterBlockEntity lbe = context.blockEntity();

        LinkedTypewriterBlockEntity lbe = LinkedTypewriterBlockEntityUtils.getLinkedTypewriterBlockEntityAt(player.blockPosition(), player);

        CustomData blockEntityData = linkedTypewriterStack.get(DataComponents.BLOCK_ENTITY_DATA);
        if (lbe != null && blockEntityData != null) {
            CACL.LOGGER.debug("1. Key Interaction: isCurrentPlayer={}, Key={}, Action={}, Entries={}", lbe.checkUser(player.getUUID()), this.key, this.action, Arrays.toString(lbe.getTypewriterEntries().getKeyMap().keySet().toArray()));
            lbe.onKeyInteraction(player.getUUID(), null, this.key, this.action == 1);
            CACL.LOGGER.debug("2. Key Interaction: isCurrentPlayer={}, Key={}, Action={}, EntryActive={}", lbe.checkUser(player.getUUID()), this.key, this.action, lbe.getTypewriterEntries().getEntry(this.key).isAlive());

            ctx.server().execute(() -> {
                DummyLevel dummyLevel = DummyLevel.getDummyLevelFor(player);
                if (dummyLevel != null) {
                    dummyLevel.setBlockEntity(player.blockPosition(), lbe, player);
                }
                CACL.LOGGER.debug("Player Level isClientSide: {}", player.level().isClientSide);
                CACL.LOGGER.debug("3. Key Interaction: isCurrentPlayer={}, Key={}, Action={}, EntryActive={}", lbe.checkUser(player.getUUID()), this.key, this.action, lbe.getTypewriterEntries().getEntry(this.key).isAlive());
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
