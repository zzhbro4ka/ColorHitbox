package com.colorhitbox.mixin;

import com.colorhitbox.client.ColorHitboxClient;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class HitboxMixin {

    @Inject(method = "renderLevel", at = @At("TAIL"))
    private void renderHitboxes(CallbackInfo ci) {
        if (!ColorHitboxClient.show) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        float[] c = ColorHitboxClient.COLORS[ColorHitboxClient.colorIndex];
        float r = c[0], g = c[1], b = c[2];

        MultiBufferSource.BufferSource buf = mc.renderBuffers().bufferSource();
        PoseStack ps = new PoseStack();
        Vec3 cam = mc.getEntityRenderDispatcher().camera.getPosition();

        for (Player player : mc.level.players()) {
            if (player == mc.player) continue;
            AABB box = player.getBoundingBox();
            ps.pushPose();
            ps.translate(box.minX - cam.x, box.minY - cam.y, box.minZ - cam.z);
            float dx = (float)(box.maxX - box.minX);
            float dy = (float)(box.maxY - box.minY);
            float dz = (float)(box.maxZ - box.minZ);
            ShapeRenderer.renderBox(ps, buf.getBuffer(ShapeRenderer.BOX),
                0, 0, 0, dx, dy, dz, r, g, b, 1f);
            ps.popPose();
        }
        buf.endBatch();
    }
}
