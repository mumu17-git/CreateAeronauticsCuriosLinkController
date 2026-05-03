package com.mumu17.cacl.registry;

import com.mumu17.cacl.CACL;
import com.mumu17.cacl.network.RemoteTypewriterKeyInteractionPacket;
import com.mumu17.cacl.network.RemoteUsePacket;
import foundry.veil.api.network.VeilPacketManager;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class ModNetworks {
    public static final VeilPacketManager INSTANCE = VeilPacketManager.create(CACL.MODID, "0.1");

    public static void init() {
        INSTANCE.registerServerbound(
                RemoteTypewriterKeyInteractionPacket.TYPE,
                RemoteTypewriterKeyInteractionPacket.CODEC,
                RemoteTypewriterKeyInteractionPacket::handle
        );
    }

    public static void register(PayloadRegistrar registrar) {
        registrar.playToServer(
                RemoteUsePacket.TYPE,
                RemoteUsePacket.STREAM_CODEC,
                RemoteUsePacket::handle
        );
    }
}
