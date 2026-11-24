package com.toancao.tcpaidhealing.neoforge;

import com.toancao.tcpaidhealing.CobblemonTcPaidHealing;
import com.toancao.tcpaidhealing.PaidHealingManager;
import net.neoforged.fml.common.Mod;

@Mod(CobblemonTcPaidHealing.MODID)
public final class CobblemonTcPaidHealingNeoForge {
    public CobblemonTcPaidHealingNeoForge() {
        PaidHealingManager.init();
    }
}
