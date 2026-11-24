package com.toancao.tcpaidhealing.currency;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public interface CurrencyAdapter {
    boolean canAfford(ServerPlayer player, int amount);

    boolean charge(ServerPlayer player, int amount);

    Component formatAmount(int amount);

    Component formatBalance(ServerPlayer player);
}
