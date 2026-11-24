package com.toancao.tcpaidhealing.client.mixin;

import com.toancao.tcpaidhealing.client.WorldCostRenderer;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiMixin {
    @Inject(method = "render", at = @At("TAIL"))
    private void cobblemonTcPaidHealing$hudCost(GuiGraphics graphics, DeltaTracker delta, CallbackInfo ci) {
        WorldCostRenderer.renderHud(graphics);
    }
}
