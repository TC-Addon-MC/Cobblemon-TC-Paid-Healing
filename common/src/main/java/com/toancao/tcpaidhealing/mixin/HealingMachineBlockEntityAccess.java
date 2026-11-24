package com.toancao.tcpaidhealing.mixin;

import com.cobblemon.mod.common.block.entity.HealingMachineBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = HealingMachineBlockEntity.class, remap = false)
public interface HealingMachineBlockEntityAccess {
    @Invoker("clearData")
    void invokeClearData();
}
