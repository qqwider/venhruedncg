package me.qwider.sundlc.render;

import me.qwider.sundlc.mixin.BossBarHudAccessor;
import me.qwider.sundlc.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import org.joml.Matrix4f;

public class DynamicIsland {
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    private static final Animation morphAnim = new Animation(7.0);
    private static final Animation yOffsetAnim = new Animation(6.0);
    private static final Animation tabAlphaAnim = new Animation(8.0);

    private static String notifyMod = "";
    private static String notifyState = "";
    private static long startTime = 0;
    private static boolean isNotify = false;

    public static void show(Module m, boolean state) {
        notifyMod = m.getName();
        notifyState = state ? "Enabled" : "Disabled";
        startTime = System.currentTimeMillis();
        isNotify = true;
    }

    public static void render(DrawContext context) {
        if (mc.player == null) return;

        // 1. Таймер уведомления
        if (isNotify && System.currentTimeMillis() - startTime > 2500) isNotify = false;
        morphAnim.update(isNotify);

        // 2. Логика BossBar (Смещение вниз)
        var bossBars = ((BossBarHudAccessor) mc.inGameHud.getBossBarHud()).getBossBars();
        yOffsetAnim.update(!bossBars.isEmpty());

        float bossBarShift = bossBars.size() * 22f;
        float y = 5 + (float) (yOffsetAnim.getValue() * bossBarShift);

        // 3. Логика Tab (Прозрачность и Яркость)
        // ИСПОЛЬЗУЕМ СОСТОЯНИЕ ВИДИМОСТИ HUD ТАБА
        boolean isTabVisible = ((me.qwider.sundlc.mixin.PlayerListHudAccessor) mc.inGameHud.getPlayerListHud()).isVisible();
        tabAlphaAnim.update(isTabVisible);

        float tabVal = (float) tabAlphaAnim.getValue();
        float globalAlpha = 1.0f - (tabVal * 0.85f);     // Прозрачность (до 15%)
        float textBrightness = 1.0f - (tabVal * 0.65f);  // Яркость текста (до 35%)

        // Подготовка цветов
        int alphaInt = (int)(255 * globalAlpha);
        int colorBase = (alphaInt << 24) | 0x1D1B1F;
        int colorInner = (alphaInt << 24) | 0x222126;

        int white = getAdjustedColor(0xFFFFFF, globalAlpha, textBrightness);
        int accent = getAdjustedColor(0x8877FF, globalAlpha, textBrightness);
        int mutedWhite = getAdjustedColor(0xBBBBBB, globalAlpha, textBrightness);
        int separatorColor = getAdjustedColor(0x444444, globalAlpha, textBrightness);

        // 4. Геометрия
        float anim = (float) morphAnim.getValue();
        float screenW = mc.getWindow().getScaledWidth();

        int fps = mc.getCurrentFps();
        int ping = 0;
        if (mc.getNetworkHandler() != null) {
            PlayerListEntry entry = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
            if (entry != null) ping = entry.getLatency();
        }

        float h = 18;
        float leftWidth = 42;
        float rightBaseW = 80;
        float rightExpandW = 110;
        float currentRightW = rightBaseW + (rightExpandW - rightBaseW) * anim;
        float totalW = leftWidth + currentRightW + 9;
        float x = (screenW - totalW) / 2f;

        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();

        // --- РЕНДЕР ---

        // 1. Основной корпус
        RenderUtils.drawRoundedRect(context, x, y, totalW, h, 6, colorBase);

        // 2. Левая подложка (Logo)
        RenderUtils.drawRoundedRect(context, x + 3, y + 3, leftWidth, h - 6, 4, colorInner);

        float sunW = MSDFRenderer.getStringWidth("Sun", 8);
        float dlcW = MSDFRenderer.getStringWidth("DLC", 8);
        float logoX = x + 3 + (leftWidth - (sunW + dlcW)) / 2f;

        MSDFRenderer.drawString(matrix, "Sun", logoX, y + 11.5f, 8, accent);
        MSDFRenderer.drawString(matrix, "DLC", logoX + sunW, y + 11.5f, 8, white);

        // 3. Правая подложка
        float rightX = x + leftWidth + 6;
        RenderUtils.drawRoundedRect(context, rightX, y + 3, currentRightW, h - 6, 4, colorInner);

        if (anim < 0.5f) {
            // Режим статистики (FPS / Ping)
            float statusAlpha = (1.0f - (anim * 2)) * globalAlpha;
            int sAlphaInt = (int)(255 * statusAlpha);

            if (sAlphaInt > 5) {
                float centerX = rightX + rightBaseW / 2f;
                float sepW = MSDFRenderer.getStringWidth("/", 7);

                MSDFRenderer.drawString(matrix, "/", centerX - sepW / 2f, y + 11f, 7, separatorColor);

                String fStr = fps + "fps";
                float fW = MSDFRenderer.getStringWidth(fStr, 7);
                MSDFRenderer.drawString(matrix, fStr, centerX - (sepW / 2f) - fW - 2, y + 11f, 7, mutedWhite);

                String pStr = ping + "ms";
                MSDFRenderer.drawString(matrix, pStr, centerX + (sepW / 2f) + 2, y + 11f, 7, mutedWhite);
            }
        } else {
            // Режим уведомления
            float notifyAlpha = (anim - 0.5f) * 2 * globalAlpha;
            int nAlphaInt = (int)(255 * notifyAlpha);

            if (nAlphaInt > 5) {
                int baseStateHex = notifyState.equals("Enabled") ? 0x88FF88 : 0xFF8888;
                int stateColor = getAdjustedColor(baseStateHex, globalAlpha * ((anim - 0.5f) * 2), textBrightness);

                MSDFRenderer.drawString(matrix, notifyMod, rightX + 5, y + 11f, 7.5f, white);

                float stateW = MSDFRenderer.getStringWidth(notifyState, 6.5f);
                MSDFRenderer.drawString(matrix, notifyState, rightX + currentRightW - stateW - 5, y + 10.5f, 6.5f, stateColor);
            }
        }
    }

    private static int getAdjustedColor(int hex, float alpha, float brightness) {
        int r = (int) (((hex >> 16) & 0xFF) * brightness);
        int g = (int) (((hex >> 8) & 0xFF) * brightness);
        int b = (int) ((hex & 0xFF) * brightness);
        int a = (int) (255 * alpha);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}