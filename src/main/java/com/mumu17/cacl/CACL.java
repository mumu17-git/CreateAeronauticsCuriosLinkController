package com.mumu17.cacl;

import com.mojang.logging.LogUtils;
import com.mumu17.cacl.registry.ModItems;
import com.mumu17.cacl.registry.ModNetworks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import org.slf4j.Logger;

@Mod(CACL.MODID)
public class CACL {
    public static final String MODID = "cacl";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CACL(IEventBus modBus) {
        modBus.addListener(this::registerPayloads);
        ModItems.register(modBus);
        ModNetworks.init();
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        ModNetworks.register(event.registrar(MODID));
    }

}
