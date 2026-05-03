package com.mumu17.cacl.registry;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public class ModKeyBindings {

    public static final String CATEGORY = "key.categories.cacl";

    public static KeyMapping REMOTE_USE;

    public static void register() {
        REMOTE_USE = new KeyMapping(
                "key.cacl.remote_use",
                KeyConflictContext.IN_GAME,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_Y,
                CATEGORY
        );
    }
}
