package com.mumu17.cacl.registry;

import com.mumu17.cacl.CACL;
import com.mumu17.cacl.item.CoordinateRecorderItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {


    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(CACL.MODID);


    public static final DeferredItem<Item> COORDINATE_RECORDER =
            ITEMS.register("coordinate_recorder",
                    () -> new CoordinateRecorderItem(
                            new Item.Properties().stacksTo(1)
                    )
            );

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}
