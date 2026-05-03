package com.mumu17.cacl.network;


import com.mumu17.cacl.mixin_interface.ILinkedTypewriterBlockEntityExtension;
import com.mumu17.cacl.util.CuriosUtils;
import com.mumu17.cacl.util.DummyLevel;
import com.mumu17.cacl.util.LinkedTypewriterBlockEntityUtils;
import dev.simulated_team.simulated.content.blocks.redstone.linked_typewriter.LinkedTypewriterBlockEntity;
import dev.simulated_team.simulated.content.blocks.redstone.linked_typewriter.LinkedTypewriterItem;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;


public record RemoteUsePacket() implements CustomPacketPayload {


    public static final Type<RemoteUsePacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("cacl", "remote_use"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RemoteUsePacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> {},
            buf -> new RemoteUsePacket()
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(RemoteUsePacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;

            ItemStack linkedTypewriterStack = CuriosUtils.getEquippedLinkedTypewriter(player);

            if (linkedTypewriterStack == null || !(linkedTypewriterStack.getItem() instanceof LinkedTypewriterItem)) return;

            LinkedTypewriterBlockEntity lbe = LinkedTypewriterBlockEntityUtils.getLinkedTypewriterBlockEntityAt(player.blockPosition(), player);

            if (lbe == null) {
                LinkedTypewriterBlockEntityUtils.VirtualLinkedTypewriterContext context =
                        LinkedTypewriterBlockEntityUtils.createVirtualLinkedTypewriterContext(linkedTypewriterStack, player.level(), player);

                if (context == null) return;

                lbe = context.blockEntity();
            }


            DummyLevel dummyLevel = DummyLevel.getDummyLevelFor(player);
            if (dummyLevel != null) {
                dummyLevel.setBlockEntity(player.blockPosition(), lbe, player);
            }
            boolean check = lbe.checkAndStartUsing(player.getUUID());
            if (!check) {
                lbe.disconnectUser();
            } else {
                if (dummyLevel != null) {
                    dummyLevel.setBlockEntity(player.blockPosition(), lbe, player);
                }
            }
            ((ILinkedTypewriterBlockEntityExtension) lbe).saveAdditional(linkedTypewriterStack);
        });
    }

}

