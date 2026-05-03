package com.mumu17.cacl.network;


import com.mumu17.cacl.CACL;
import com.mumu17.cacl.mixin_interface.ILinkedTypewriterBlockEntityExtension;
import com.mumu17.cacl.util.CuriosUtils;
import com.mumu17.cacl.util.DummyLevel;
import com.mumu17.cacl.util.LinkedTypewriterBlockEntityUtils;
import dev.simulated_team.simulated.content.blocks.redstone.linked_typewriter.LinkedTypewriterBlockEntity;
import dev.simulated_team.simulated.content.blocks.redstone.linked_typewriter.LinkedTypewriterItem;
import dev.simulated_team.simulated.mixin_interface.PlayerTypewriterExtension;
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

            CACL.LOGGER.debug("LinkedTypewriterBlockEntity の取得結果: {}", lbe);

            if (lbe == null) {
                LinkedTypewriterBlockEntityUtils.VirtualLinkedTypewriterContext context =
                        LinkedTypewriterBlockEntityUtils.createVirtualLinkedTypewriterContext(linkedTypewriterStack, player.level(), player);

                if (context == null) return;

                lbe = context.blockEntity();
            }


            // CACL.LOGGER.debug("{}", linkedTypewriterBlockEntity.getTypewriterEntries());
            //CACL.LOGGER.debug("{}", ((PlayerTypewriterExtension)linkedTypewriterBlockEntity.getLevel().getPlayerByUUID(player.getUUID())).simulated$getCurrentTypewriter());
            // linkedTypewriterBlockEntity.getBlockState().getBlock().useItemOn(ItemStack.EMPTY, context.blockEntity().getLevel(), player, InteractionHand.MAIN_HAND, new BlockHitResult(player.blockPosition().getCenter(), Direction.DOWN, context.position(), false));
            // linkedTypewriterBlockEntity.checkAndStartUsing(player.getUUID());
            // ((ILinkedTypewriterBlockEntityExtension) linkedTypewriterBlockEntity).saveAdditional(linkedTypewriterStack);

            DummyLevel dummyLevel = DummyLevel.getDummyLevelFor(player);
            CACL.LOGGER.debug("虚想LinkedTypewriter を使用中: {}, {}", player.getName().getString(), dummyLevel != null && dummyLevel.isClientSide());
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
            CACL.LOGGER.debug("simulated$getCurrentTypewriter: {}", ((PlayerTypewriterExtension)player).simulated$getCurrentTypewriter());
        });
    }

}

