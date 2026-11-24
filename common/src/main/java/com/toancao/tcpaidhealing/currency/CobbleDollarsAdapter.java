package com.toancao.tcpaidhealing.currency;

import com.toancao.tcpaidhealing.CobblemonTcPaidHealing;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.lang.reflect.Method;
import java.math.BigInteger;

public final class CobbleDollarsAdapter implements CurrencyAdapter {
    private static final String CLASS_NAME = "fr.harmex.cobbledollars.common.utils.extensions.PlayerExtensionKt";

    private final Method getBalance;
    private final Method setBalance;

    private CobbleDollarsAdapter(Method getBalance, Method setBalance) {
        this.getBalance = getBalance;
        this.setBalance = setBalance;
    }

    public static CobbleDollarsAdapter create() {
        try {
            Class<?> extension = Class.forName(CLASS_NAME);
            Method get = extension.getMethod("getCobbleDollars", net.minecraft.world.entity.player.Player.class);
            Method set = extension.getMethod("setCobbleDollars", net.minecraft.world.entity.player.Player.class, BigInteger.class);
            return new CobbleDollarsAdapter(get, set);
        } catch (ReflectiveOperationException | LinkageError e) {
            return null;
        }
    }

    private BigInteger balanceOf(ServerPlayer player) {
        try {
            Object value = getBalance.invoke(null, player);
            return value instanceof BigInteger big ? big : BigInteger.ZERO;
        } catch (ReflectiveOperationException e) {
            return BigInteger.ZERO;
        }
    }

    @Override
    public boolean canAfford(ServerPlayer player, int amount) {
        return balanceOf(player).compareTo(BigInteger.valueOf(amount)) >= 0;
    }

    @Override
    public boolean charge(ServerPlayer player, int amount) {
        BigInteger current = balanceOf(player);
        BigInteger cost = BigInteger.valueOf(amount);
        if (current.compareTo(cost) < 0) {
            return false;
        }
        try {
            setBalance.invoke(null, player, current.subtract(cost));
            return true;
        } catch (ReflectiveOperationException e) {
            CobblemonTcPaidHealing.LOGGER.warn("Could not charge CobbleDollars", e);
            return false;
        }
    }

    @Override
    public Component formatAmount(int amount) {
        return Component.literal(String.valueOf(amount)).append(" ").append(name());
    }

    @Override
    public Component formatBalance(ServerPlayer player) {
        return Component.literal(balanceOf(player).toString());
    }

    private Component name() {
        return Component.translatable("cobblemon_tc_paid_healing.currency.cobble_dollars");
    }
}
