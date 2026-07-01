package com.colorhitbox.mixin;

import com.colorhitbox.client.ColorHitboxClient;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
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

import java.util.List;

@Mixin(LevelRenderer.class)
public class HitboxMixin {

    @Inject(method = "renderLevel", at = @At("TAIL"))
    private void onRenderLevel(CallbackInfo ci) {
        if (!ColorHitboxClient.show) return;
        // рендеринг через mixin пока заглушка
    }
}
