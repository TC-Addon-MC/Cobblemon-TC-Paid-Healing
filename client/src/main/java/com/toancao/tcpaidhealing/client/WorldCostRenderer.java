package com.toancao.tcpaidhealing.client;

import com.cobblemon.mod.common.client.CobblemonClient;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.toancao.tcpaidhealing.PaidHealingManager;
import com.toancao.tcpaidhealing.config.PaidHealingConfig;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class WorldCostRenderer {
    private static final int ICON_SIZE = 16;
    private static final int ICON_GAP = 2;
    private static final String DOLLARS_SYMBOL = "$";
    private static final int MAX_DISTANCE_SQR = 25;
    private static final float LABEL_HEIGHT_OFFSET = 1.175F;
    private static final boolean DEBUG = Boolean.getBoolean("tcpaidhealing.debug");
    private static final Logger LOGGER = LoggerFactory.getLogger("cobblemon_tc_paid_healing");
    private static final Set<String> DEBUG_KEYS = new HashSet<>();

    private static ItemStack currencyStack;
    private static String resolvedItemId;
    private static boolean pending;
    private static boolean dollarsMode;
    private static String label = "";
    private static int lastCost = -1;
    private static boolean lastDollars;
    private static int screenX;
    private static int screenY;

    private WorldCostRenderer() {
    }

    // Ghi log chẩn đoán một lần cho mỗi khoá, chỉ bật khi chạy game với tham số hệ thống tcpaidhealing.debug.
    private static void debug(String key, String message) {
        if (DEBUG_KEYS.add(key)) {
            LOGGER.info("[TCPH] {}", message);
        }
    }

    // Chiếu tâm khối hồi máu ra toạ độ màn hình để lần vẽ HUD kế tiếp đặt nhãn giá đúng vị trí.
    public static void capture(Camera camera, Matrix4f viewMatrix, Matrix4f projectionMatrix) {
        pending = false;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null || minecraft.screen != null) {
            return;
        }
        HitResult hit = minecraft.hitResult;
        if (hit == null || hit.getType() != HitResult.Type.BLOCK) {
            return;
        }
        BlockPos pos = ((BlockHitResult) hit).getBlockPos();
        if (pos.distSqr(minecraft.player.blockPosition()) > MAX_DISTANCE_SQR) {
            return;
        }
        BlockState state = minecraft.level.getBlockState(pos);
        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        if (!blockId.getNamespace().equals("cobblemon") || !blockId.getPath().equals("healing_machine")) {
            return;
        }
        int charge = 0;
        for (Map.Entry<Property<?>, Comparable<?>> entry : state.getValues().entrySet()) {
            if (entry.getKey().getName().equals("charge") && entry.getValue() instanceof Integer value) {
                charge = value;
                break;
            }
        }
        if (charge <= 0 || charge >= 6) {
            return;
        }
        int count = 0;
        for (Pokemon pokemon : CobblemonClient.INSTANCE.getStorage().getParty()) {
            if (pokemon != null && pokemon.canBeHealed()) {
                count++;
            }
        }
        if (count <= 0) {
            return;
        }
        PaidHealingConfig config = PaidHealingManager.config();
        if (!config.enabled) {
            return;
        }
        int cost = PaidHealingManager.costFor(count);
        boolean dollars = PaidHealingManager.isCobbleDollarsActive();
        if (!dollars && resolveCurrency(config.currencyItem) == null) {
            return;
        }
        if (DEBUG) {
            debug("aim", "block=" + blockId + " pos=" + pos + " charge=" + charge + " healable=" + count
                    + " cost=" + cost + " dollars=" + dollars);
        }
        Vec3 cameraPos = camera.getPosition();
        Vector4f clip = new Vector4f((float) (pos.getX() + 0.5D - cameraPos.x),
                (float) (pos.getY() + LABEL_HEIGHT_OFFSET - cameraPos.y),
                (float) (pos.getZ() + 0.5D - cameraPos.z), 1.0F);
        clip.mul(viewMatrix).mul(projectionMatrix);
        if (clip.w <= 0.05F) {
            return;
        }
        float ndcX = clip.x / clip.w;
        float ndcY = clip.y / clip.w;
        if (Math.abs(ndcX) > 1.0F || Math.abs(ndcY) > 1.0F) {
            return;
        }
        int width = minecraft.getWindow().getGuiScaledWidth();
        int height = minecraft.getWindow().getGuiScaledHeight();
        screenX = Math.round((ndcX * 0.5F + 0.5F) * width);
        screenY = Math.round((1.0F - (ndcY * 0.5F + 0.5F)) * height);
        if (cost != lastCost || dollars != lastDollars) {
            lastCost = cost;
            lastDollars = dollars;
            label = dollars ? DOLLARS_SYMBOL + cost : Integer.toString(cost);
        }
        dollarsMode = dollars;
        pending = true;
    }

    // Vẽ nhãn giá cỡ cố định cạnh icon tiền trên HUD, canh giữa theo toạ độ màn hình đã chiếu ở bước capture.
    public static void renderHud(GuiGraphics graphics) {
        if (!pending) {
            return;
        }
        pending = false;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen != null || minecraft.options.hideGui) {
            return;
        }
        Font font = minecraft.font;
        int textWidth = font.width(label);
        boolean hasIcon = !dollarsMode && currencyStack != null;
        int gap = hasIcon ? ICON_GAP : 0;
        int iconWidth = hasIcon ? ICON_SIZE : 0;
        int totalWidth = iconWidth + gap + textWidth;
        int left = screenX - totalWidth / 2;
        int textY = screenY - font.lineHeight / 2;
        if (hasIcon) {
            graphics.renderItem(currencyStack, left, textY - 4);
        }
        graphics.drawString(font, label, left + iconWidth + gap, textY,
                dollarsMode ? 0xFF55FF55 : 0xFFFFFFFF, true);
    }

    // Phân giải item tiền một lần cho mỗi ID, trả null nếu ID không tồn tại để bỏ qua việc vẽ nhãn.
    private static ItemStack resolveCurrency(String itemId) {
        if (itemId.equals(resolvedItemId)) {
            return currencyStack;
        }
        resolvedItemId = itemId;
        currencyStack = null;
        ResourceLocation id = ResourceLocation.tryParse(itemId);
        if (id != null && BuiltInRegistries.ITEM.containsKey(id)) {
            Item item = BuiltInRegistries.ITEM.get(id);
            currencyStack = new ItemStack(item);
        }
        return currencyStack;
    }
}
