package com.toancao.tcpaidhealing.mixin;

import com.cobblemon.mod.common.block.entity.HealingMachineBlockEntity;
import com.toancao.tcpaidhealing.PaidHealingManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = HealingMachineBlockEntity.class, remap = false)
public abstract class HealingMachineBlockEntityMixin {
    @Inject(method = "completeHealing", at = @At("HEAD"), cancellable = true, require = 1)
    private void cobblemonTcPaidHealing$charge(CallbackInfo ci) {
        if (PaidHealingManager.tryCharge((HealingMachineBlockEntity) (Object) this)) {
            return;
        }
        ((HealingMachineBlockEntityAccess) this).invokeClearData();
        ci.cancel();
    }
}
