package com.mumu17.cacl.event;

import com.mumu17.cacl.CACL;
import com.mumu17.cacl.util.LinkedTypewriterBlockEntityUtils;
import dev.simulated_team.simulated.content.blocks.redstone.linked_typewriter.LinkedTypewriterBlockEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(
        modid = CACL.MODID,
        bus = EventBusSubscriber.Bus.GAME
)
public class ServerEvents {
    @SubscribeEvent
    public static void tick(ServerTickEvent.Post event) {
        tickRemoteLinkedTypewriter(event.getServer());
    }

    private static void tickRemoteLinkedTypewriter(MinecraftServer server) {
        for (Player player : server.getPlayerList().getPlayers()) {

            if (player.level().isClientSide) continue;

            LinkedTypewriterBlockEntity lbe = LinkedTypewriterBlockEntityUtils.getLinkedTypewriterBlockEntityAt(player.blockPosition(), player);
            if (lbe == null) continue;
            lbe.getTypewriterEntries().updateNetworks(player.level());

        }
    }


}
