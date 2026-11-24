package com.toancao.tcpaidhealing;

import com.cobblemon.mod.common.api.storage.party.PartyStore;
import com.cobblemon.mod.common.block.entity.HealingMachineBlockEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.util.PlayerExtensionsKt;
import com.cobblemon.mod.common.world.gamerules.CobblemonGameRules;
import com.toancao.tcpaidhealing.config.PaidHealingConfig;
import com.toancao.tcpaidhealing.currency.CobbleDollarsAdapter;
import com.toancao.tcpaidhealing.currency.CurrencyAdapter;
import com.toancao.tcpaidhealing.currency.ItemCurrencyAdapter;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public final class PaidHealingManager {
    private static final Map<LevelBlockKey, Float> CHARGE_SNAPSHOTS = new HashMap<>();

    private static PaidHealingConfig config;
    private static CurrencyAdapter adapter;

    private PaidHealingManager() {
    }

    // Nạp config và chọn bộ tiền tệ một lần khi mod khởi động, ghi cảnh báo nếu cấu hình sai hoặc thiếu mod tiền.
    public static void init() {
        config = PaidHealingConfig.load();
        String mode = config.currencyMode.trim().toUpperCase(Locale.ROOT);
        if (mode.equals("ITEM")) {
            adapter = new ItemCurrencyAdapter(config.currencyItem);
        } else {
            CobbleDollarsAdapter dollars = CobbleDollarsAdapter.create();
            if (dollars != null) {
                adapter = dollars;
                return;
            }
            if (mode.equals("COBBLEDOLLARS")) {
                CobblemonTcPaidHealing.LOGGER.warn("CobbleDollars not installed, falling back to item {}", config.currencyItem);
            }
            adapter = new ItemCurrencyAdapter(config.currencyItem);
        }
        if (adapter instanceof ItemCurrencyAdapter item && !item.isValid()) {
            CobblemonTcPaidHealing.LOGGER.warn("Invalid currency item '{}', paid healing will be skipped", config.currencyItem);
        }
    }

    // Trả về config đã nạp, tự nạp lần đầu để client và server dùng chung một nguồn cấu hình duy nhất.
    public static PaidHealingConfig config() {
        if (config == null) {
            init();
        }
        return config;
    }

    // Cho client biết đang dùng CobbleDollars hay item để vẽ đúng icon và màu cho nhãn giá.
    public static boolean isCobbleDollarsActive() {
        return adapter instanceof CobbleDollarsAdapter;
    }

    // Trả true khi người chơi được miễn phí hoặc hệ thống chưa sẵn sàng, tránh chặn hồi máu oan.
    public static boolean shouldBypass(ServerPlayer player) {
        if (config == null || adapter == null) {
            return true;
        }
        if (!config.enabled) {
            return true;
        }
        if (config.freeForCreative && player.isCreative()) {
            return true;
        }
        return adapter instanceof ItemCurrencyAdapter item && !item.isValid();
    }

    // Đếm số Pokémon cần hồi trong party và PC theo đúng điều kiện game rule healersHealPC của Cobblemon.
    public static int healableCount(ServerPlayer player) {
        if (config == null) {
            init();
        }
        int count = 0;
        PartyStore party = PlayerExtensionsKt.party(player);
        for (Pokemon pokemon : party) {
            if (pokemon != null && pokemon.canBeHealed()) {
                count++;
            }
        }
        if (config.chargePcPokemon && player.level().getGameRules().getBoolean(CobblemonGameRules.HEALERS_HEAL_PC)) {
            for (Pokemon pokemon : PlayerExtensionsKt.pc(player)) {
                if (pokemon != null && pokemon.canBeHealed()) {
                    count++;
                }
            }
        }
        return count;
    }

    // Nhân giá mỗi Pokémon với số lượng cần hồi, dùng kiểu long để chặn tràn số nguyên khi config quá lớn.
    public static int costFor(int pokemonCount) {
        long total = (long) config().costPerPokemon * Math.max(pokemonCount, 0);
        return (int) Math.min(Integer.MAX_VALUE, total);
    }

    // Kiểm tra máy thật sự sẽ hồi cho người chơi này không, tránh báo thiếu tiền khi máy bận hoặc đã hồi đầy.
    public static boolean willHeal(HealingMachineBlockEntity machine, ServerPlayer player) {
        if (machine.isInUse()) {
            return false;
        }
        if (PlayerExtensionsKt.isInBattle(player)) {
            return false;
        }
        if (healableCount(player) <= 0) {
            return false;
        }
        return machine.canHeal(PlayerExtensionsKt.party(player));
    }

    // Chặn lượt dùng máy khi thiếu tiền và ghi lại mức sạc trước khi Cobblemon kịp trừ để hoàn lại sau.
    public static boolean blockIfUnaffordable(ServerPlayer player, HealingMachineBlockEntity machine) {
        if (!willHeal(machine, player) || shouldBypass(player)) {
            return false;
        }
        int cost = costFor(healableCount(player));
        if (cost <= 0) {
            return false;
        }
        LevelBlockKey key = keyOf(machine);
        CHARGE_SNAPSHOTS.put(key, machine.getHealingCharge());
        if (adapter.canAfford(player, cost)) {
            return false;
        }
        CHARGE_SNAPSHOTS.remove(key);
        sendInsufficient(player, cost);
        return true;
    }

    // Thu tiền ngay trước khi hồi xong, hoàn sạc máy và trả false để mixin huỷ lượt hồi khi trừ tiền thất bại.
    public static boolean tryCharge(HealingMachineBlockEntity machine) {
        LevelBlockKey key = keyOf(machine);
        ServerPlayer player = resolvePlayer(machine);
        if (player == null) {
            CHARGE_SNAPSHOTS.remove(key);
            return true;
        }
        if (shouldBypass(player)) {
            CHARGE_SNAPSHOTS.remove(key);
            if (config.enabled) {
                notifyInvalidItem(player);
            }
            return true;
        }
        int count = healableCount(player);
        int cost = costFor(count);
        if (cost <= 0) {
            CHARGE_SNAPSHOTS.remove(key);
            return true;
        }
        if (!adapter.charge(player, cost)) {
            sendInsufficient(player, cost);
            refundMachineCharge(machine, key);
            CHARGE_SNAPSHOTS.remove(key);
            return false;
        }
        CHARGE_SNAPSHOTS.remove(key);
        player.sendSystemMessage(Component.translatable(
                "cobblemon_tc_paid_healing.charge.done",
                adapter.formatAmount(cost), count), false);
        return true;
    }

    // Tìm người chơi đang dùng máy theo UUID đã lưu, trả null khi máy do NPC dùng hoặc người chơi đã thoát.
    private static ServerPlayer resolvePlayer(HealingMachineBlockEntity machine) {
        UUID user = machine.getCurrentUser();
        return user == null ? null : PlayerExtensionsKt.getPlayer(user);
    }

    // Gửi thông báo thiếu tiền kèm giá cần trả và số dư hiện có theo đúng loại tiền tệ đang dùng.
    private static void sendInsufficient(ServerPlayer player, int cost) {
        player.sendSystemMessage(Component.translatable(
                "cobblemon_tc_paid_healing.error.insufficient",
                adapter.formatAmount(cost), adapter.formatBalance(player)), false);
    }

    // Báo cho người chơi biết item tiền trong config không tồn tại nên lượt hồi này được miễn phí.
    private static void notifyInvalidItem(ServerPlayer player) {
        if (adapter instanceof ItemCurrencyAdapter item && !item.isValid()) {
            player.sendSystemMessage(Component.translatable(
                    "cobblemon_tc_paid_healing.error.invalid_item", config.currencyItem), false);
        }
    }

    // Khôi phục mức sạc đã bị Cobblemon trừ lúc bắt đầu hồi, vì lượt hồi bị huỷ nên người chơi không mất sạc.
    private static void refundMachineCharge(HealingMachineBlockEntity machine, LevelBlockKey key) {
        Float before = CHARGE_SNAPSHOTS.get(key);
        if (before == null) {
            return;
        }
        machine.setHealingCharge(Math.min(machine.getMaxCharge(), before));
    }

    // Tạo khoá gồm chiều không gian và toạ độ để hai máy khác chiều không ghi đè mức sạc của nhau.
    private static LevelBlockKey keyOf(HealingMachineBlockEntity machine) {
        Level level = machine.getLevel();
        return new LevelBlockKey(level == null ? Level.OVERWORLD : level.dimension(), machine.getBlockPos());
    }

    private record LevelBlockKey(ResourceKey<Level> dimension, BlockPos pos) {
    }
}
