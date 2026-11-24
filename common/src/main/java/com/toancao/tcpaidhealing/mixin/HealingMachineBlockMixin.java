package com.toancao.tcpaidhealing.mixin;

import com.cobblemon.mod.common.block.HealingMachineBlock;
import com.cobblemon.mod.common.block.entity.HealingMachineBlockEntity;
import com.toancao.tcpaidhealing.PaidHealingManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HealingMachineBlock.class)
public abstract class HealingMachineBlockMixin {
    @Inject(method = "useWithoutItem", at = @At("HEAD"), cancellable = true)
    private void cobblemonTcPaidHealing$preCheck(BlockState state, Level level, BlockPos pos, Player player,
                                                  BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir) {
        if (level.isClientSide || !(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        if (!(level.getBlockEntity(pos) instanceof HealingMachineBlockEntity machine)) {
            return;
        }
        if (PaidHealingManager.blockIfUnaffordable(serverPlayer, machine)) {
            cir.setReturnValue(InteractionResult.SUCCESS);
        }
    }
}
