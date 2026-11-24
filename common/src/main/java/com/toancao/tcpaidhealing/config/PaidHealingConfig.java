package com.toancao.tcpaidhealing.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

public final class PaidHealingConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = Paths.get("config", "cobblemon_tc_paid_healing.json");
    private static final String DEFAULT_ITEM = "minecraft:emerald";

    public boolean enabled = true;
    public int costPerPokemon = 10;
    public String currencyMode = "AUTO";
    public String currencyItem = DEFAULT_ITEM;
    public boolean chargePcPokemon = true;
    public boolean freeForCreative = true;

    // Đọc file config, chuẩn hoá các giá trị sai và chỉ ghi lại khi nội dung thực sự thay đổi.
    public static PaidHealingConfig load() {
        boolean changed = !Files.exists(FILE);
        PaidHealingConfig config;
        try {
            String json = Files.readString(FILE, StandardCharsets.UTF_8);
            config = GSON.fromJson(json, PaidHealingConfig.class);
            if (config == null) {
                config = new PaidHealingConfig();
                changed = true;
            }
        } catch (IOException | RuntimeException e) {
            config = new PaidHealingConfig();
            changed = true;
        }
        changed |= config.normalize();
        if (changed) {
            config.save();
        }
        return config;
    }

    // Ép giá về số không âm, chế độ tiền và item tiền về giá trị hợp lệ, trả true nếu có sửa.
    private boolean normalize() {
        boolean changed = false;
        if (costPerPokemon < 0) {
            costPerPokemon = 0;
            changed = true;
        }
        String mode = currencyMode == null ? "" : currencyMode.trim().toUpperCase(Locale.ROOT);
        if (!mode.equals("AUTO") && !mode.equals("COBBLEDOLLARS") && !mode.equals("ITEM")) {
            mode = "AUTO";
            changed = true;
        }
        if (!mode.equals(currencyMode)) {
            currencyMode = mode;
            changed = true;
        }
        if (currencyItem == null || currencyItem.isBlank() || ResourceLocation.tryParse(currencyItem.trim()) == null) {
            currencyItem = DEFAULT_ITEM;
            changed = true;
        }
        return changed;
    }

    // Ghi config xuống đĩa dạng JSON UTF-8, bỏ qua lỗi IO để không làm hỏng quá trình khởi động game.
    public void save() {
        try {
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, GSON.toJson(this), StandardCharsets.UTF_8);
        } catch (IOException ignored) {
        }
    }
}
