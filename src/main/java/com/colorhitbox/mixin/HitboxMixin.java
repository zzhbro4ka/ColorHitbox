package com.colorhitbox.mixin;

import com.colorhitbox.client.ColorHitboxClient;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
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

        PoseStack ps = new PoseStack();
        MultiBufferSource.BufferSource buf = mc.renderBuffers().bufferSource();
        VertexConsumer vc = buf.getBuffer(RenderType.lines());

        var cam = mc.gameRenderer.getMainCamera().getPosition();

        for (Player player : mc.level.players()) {
            if (player == mc.player) continue;
            AABB box = player.getBoundingBox();
            ps.pushPose();
            ps.translate(box.minX - cam.x, box.minY - cam.y, box.minZ - cam.z);
            float dx = (float)(box.maxX - box.minX);
            float dy = (float)(box.maxY - box.minY);
            float dz = (float)(box.maxZ - box.minZ);
            drawBox(ps, vc, dx, dy, dz, r, g, b);
            ps.popPose();
        }
        buf.endBatch(RenderType.lines());
    }

    private void drawBox(PoseStack ps, VertexConsumer vc,
                         float dx, float dy, float dz,
                         float r, float g, float b) {
        float[][] edges = {
            {0,0,0},{dx,0,0}, {dx,0,0},{dx,0,dz}, {dx,0,dz},{0,0,dz}, {0,0,dz},{0,0,0},
            {0,dy,0},{dx,dy,0}, {dx,dy,0},{dx,dy,dz}, {dx,dy,dz},{0,dy,dz}, {0,dy,dz},{0,dy,0},
            {0,0,0},{0,dy,0}, {dx,0,0},{dx,dy,0}, {dx,0,dz},{dx,dy,dz}, {0,0,dz},{0,dy,dz}
        };
        Matrix4f mat = ps.last().pose();
        for (int i = 0; i < edges.length; i += 2) {
            float[] a = edges[i], b2 = edges[i+1];
            float nx = b2[0]-a[0], ny = b2[1]-a[1], nz = b2[2]-a[2];
            float len = (float)Math.sqrt(nx*nx+ny*ny+nz*nz);
            if (len > 0) { nx/=len; ny/=len; nz/=len; }
            vc.addVertex(mat, a[0], a[1], a[2]).setColor(r,g,b,1f).setNormal(nx,ny,nz);
            vc.addVertex(mat, b2[0], b2[1], b2[2]).setColor(r,g,b,1f).setNormal(nx,ny,nz);
        }
    }
}
