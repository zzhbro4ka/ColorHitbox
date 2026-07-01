package com.colorhitbox.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.MultiBufferSource;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import com.mojang.blaze3d.platform.InputConstants;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

public class ColorHitboxClient implements ClientModInitializer {

    static boolean show = false;
    static int colorIndex = 0;
    static final int[][] COLORS = {
        {255, 0, 0},
        {0, 255, 0},
        {0, 0, 255},
        {255, 255, 0},
        {255, 0, 255},
    };

    @Override
    public void onInitializeClient() {
        KeyMapping.Category category = KeyMapping.Category.register(
            "key.category.colorhitbox.main", 1000);

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

        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            if (!show) return;
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null) return;

            int[] c = COLORS[colorIndex];
            float r = c[0] / 255f, g = c[1] / 255f, b = c[2] / 255f;

            PoseStack matrices = context.matrixStack();
            MultiBufferSource consumers = context.consumers();
            if (consumers == null) return;

            var cam = mc.gameRenderer.getMainCamera().getPosition();

            for (Player player : mc.level.players()) {
                if (player == mc.player) continue;
                AABB box = player.getBoundingBox();
                matrices.pushPose();
                matrices.translate(
                    box.minX - cam.x, box.minY - cam.y, box.minZ - cam.z);

                float dx = (float)(box.maxX - box.minX);
                float dy = (float)(box.maxY - box.minY);
                float dz = (float)(box.maxZ - box.minZ);

                VertexConsumer vc = consumers.getBuffer(RenderType.lines());
                drawBox(matrices, vc, dx, dy, dz, r, g, b);
                matrices.popPose();
            }
        });
    }

    static void drawBox(PoseStack m, VertexConsumer vc,
                        float dx, float dy, float dz,
                        float r, float g, float b) {
        float[][] verts = {
            {0,0,0},{dx,0,0},{dx,0,0},{dx,0,dz},{dx,0,dz},{0,0,dz},{0,0,dz},{0,0,0},
            {0,dy,0},{dx,dy,0},{dx,dy,0},{dx,dy,dz},{dx,dy,dz},{0,dy,dz},{0,dy,dz},{0,dy,0},
            {0,0,0},{0,dy,0},{dx,0,0},{dx,dy,0},{dx,0,dz},{dx,dy,dz},{0,0,dz},{0,dy,dz}
        };
        Matrix4f mat = m.last().pose();
        for (int i = 0; i < verts.length; i += 2) {
            float[] a = verts[i], b2 = verts[i+1];
            float nx = b2[0]-a[0], ny = b2[1]-a[1], nz = b2[2]-a[2];
            float len = (float)Math.sqrt(nx*nx+ny*ny+nz*nz);
            if (len > 0) { nx/=len; ny/=len; nz/=len; }
            vc.addVertex(mat, a[0], a[1], a[2]).setColor(r,g,b,1f).setNormal(nx,ny,nz);
            vc.addVertex(mat, b2[0], b2[1], b2[2]).setColor(r,g,b,1f).setNormal(nx,ny,nz);
        }
    }
}
