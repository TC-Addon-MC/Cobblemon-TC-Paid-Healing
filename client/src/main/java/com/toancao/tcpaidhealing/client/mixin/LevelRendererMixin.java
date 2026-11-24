package com.toancao.tcpaidhealing.client.mixin;

import com.toancao.tcpaidhealing.client.WorldCostRenderer;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {
    @Inject(method = "renderLevel", at = @At("TAIL"))
    private void cobblemonTcPaidHealing$worldCost(DeltaTracker delta, boolean outline, Camera camera,
                                                  GameRenderer renderer, LightTexture light,
                                                  Matrix4f viewMatrix, Matrix4f projectionMatrix, CallbackInfo ci) {
        WorldCostRenderer.capture(camera, viewMatrix, projectionMatrix);
    }
}
