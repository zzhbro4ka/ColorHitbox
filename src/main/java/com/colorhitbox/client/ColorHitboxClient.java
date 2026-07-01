package com.colorhitbox.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import org.lwjgl.glfw.GLFW;
import org.joml.Matrix4f;

public class ColorHitboxClient implements ClientModInitializer {

    static boolean show = false;
    static int colorIndex = 0;
    static final int[][] COLORS = {
        {255, 0, 0},    // красный
        {0, 255, 0},    // зелёный
        {0, 0, 255},    // синий
        {255, 255, 0},  // жёлтый
        {255, 0, 255},  // фиолетовый
    };

    @Override
    public void onInitializeClient() {
        KeyMapping keyShow = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.colorhitbox.toggle", InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_H, "key.category.colorhitbox.main"));

        KeyMapping keyColor = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.colorhitbox.color", InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_J, "key.category.colorhitbox.main"));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (keyShow.consumeClick()) show = !show;
            if (keyColor.consumeClick()) colorIndex = (colorIndex + 1) % COLORS.length;
        });

        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            if (!show) return;
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.world == null) return;

            int[] c = COLORS[colorIndex];
            float r = c[0] / 255f, g = c[1] / 255f, b = c[2] / 255f;

            var matrices = context.matrixStack();
            var consumers = context.consumers();
            if (consumers == null) return;

            var cam = mc.gameRenderer.getCamera().getPos();

            for (PlayerEntity player : mc.world.getPlayers()) {
                if (player == mc.player) continue;
                Box box = player.getBoundingBox();
                matrices.push();
                matrices.translate(
                    box.minX - cam.x, box.minY - cam.y, box.minZ - cam.z);

                double dx = box.maxX - box.minX;
                double dy = box.maxY - box.minY;
                double dz = box.maxZ - box.minZ;

                VertexConsumer vc = consumers.getBuffer(RenderLayer.LINES);
                drawBox(matrices, vc, (float)dx, (float)dy, (float)dz, r, g, b);
                matrices.pop();
            }
        });
    }

    static void drawBox(MatrixStack m, VertexConsumer vc,
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
