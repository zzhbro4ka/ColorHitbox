package com.colorhitbox.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.ResourceLocation;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;

public class ColorHitboxClient implements ClientModInitializer {

    public static boolean show = false;
    public static int colorIndex = 0;
    public static final float[][] COLORS = {
        {1f, 0f, 0f},
        {0f, 1f, 0f},
        {0f, 0f, 1f},
        {1f, 1f, 0f},
        {1f, 0f, 1f},
    };

    @Override
    public void onInitializeClient() {
        KeyMapping.Category category = KeyMapping.Category.register(
            ResourceLocation.fromNamespaceAndPath("colorhitbox", "main"));

        KeyMapping keyShow = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.colorhitbox.toggle", InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_H, category));

        KeyMapping keyColor = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.colorhitbox.color", InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_J, category));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (keyShow.consumeClick()) show = !show;
            if (keyColor.consumeClick()) colorIndex = (colorIndex + 1) % COLORS.length;
        });
    }
}
