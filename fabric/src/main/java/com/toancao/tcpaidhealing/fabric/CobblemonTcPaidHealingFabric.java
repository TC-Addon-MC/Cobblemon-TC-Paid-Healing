package com.toancao.tcpaidhealing.fabric;

import com.toancao.tcpaidhealing.PaidHealingManager;
import net.fabricmc.api.ModInitializer;

public final class CobblemonTcPaidHealingFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        PaidHealingManager.init();
    }
}
