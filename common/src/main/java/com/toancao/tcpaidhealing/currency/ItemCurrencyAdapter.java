package com.toancao.tcpaidhealing.currency;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class ItemCurrencyAdapter implements CurrencyAdapter {
    private final Item item;
    private final String itemId;

    public ItemCurrencyAdapter(String itemId) {
        this.itemId = itemId;
        Item resolved = null;
        ResourceLocation id = ResourceLocation.tryParse(itemId);
        if (id != null && BuiltInRegistries.ITEM.containsKey(id)) {
            resolved = BuiltInRegistries.ITEM.get(id);
        }
        this.item = resolved;
    }

    public boolean isValid() {
        return item != null;
    }

    @Override
    public boolean canAfford(ServerPlayer player, int amount) {
        return isValid() && player.getInventory().countItem(item) >= amount;
    }

    @Override
    public boolean charge(ServerPlayer player, int amount) {
        if (!isValid() || amount <= 0) {
            return isValid();
        }
        if (player.getInventory().countItem(item) < amount) {
            return false;
        }
        int remaining = amount;
        for (int slot = 0; slot < player.getInventory().getContainerSize() && remaining > 0; slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.isEmpty() || !stack.is(item)) {
                continue;
            }
            int taken = Math.min(stack.getCount(), remaining);
            stack.shrink(taken);
            remaining -= taken;
        }
        player.getInventory().setChanged();
        return remaining <= 0;
    }

    @Override
    public Component formatAmount(int amount) {
        if (!isValid()) {
            return Component.literal(amount + " " + itemId);
        }
        return Component.literal(String.valueOf(amount)).append(" ").append(new ItemStack(item).getHoverName());
    }

    @Override
    public Component formatBalance(ServerPlayer player) {
        if (!isValid()) {
            return Component.literal("0");
        }
        return Component.literal(String.valueOf(player.getInventory().countItem(item)));
    }
}
