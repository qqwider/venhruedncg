package me.qwider.sundlc.render;

import me.qwider.sundlc.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import org.joml.Matrix4f;

public class DynamicIsland {
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    private static final Animation morphAnim = new Animation(7.0);

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

        if (isNotify && System.currentTimeMillis() - startTime > 2500) isNotify = false;
        morphAnim.update(isNotify);

        float anim = (float) morphAnim.getValue();
        float screenW = mc.getWindow().getScaledWidth();

        int fps = mc.getCurrentFps();
        int ping = 0;
        if (mc.getNetworkHandler() != null) {
            PlayerListEntry entry = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
            if (entry != null) ping = entry.getLatency();
        }

        // --- ПАРАМЕТРЫ ГЕОМЕТРИИ ---
        float h = 18;
        float y = 5;

        float leftWidth = 42;
        float rightBaseW = 80; // Базовая ширина для статов
        float rightExpandW = 110;
        float currentRightW = rightBaseW + (rightExpandW - rightBaseW) * anim;

        float totalW = leftWidth + currentRightW + 9;
        float x = (screenW - totalW) / 2f;

        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();

        int colorBase = 0xFF1D1B1F;
        int colorInner = 0xFF222126;
        int accent = 0xFF8877FF;

        // 1. КОРПУС
        RenderUtils.drawRoundedRect(context, x, y, totalW, h, 6, colorBase);

        // 2. ЛЕВАЯ ПОДЛОЖКА (Центровка SunDLC)
        RenderUtils.drawRoundedRect(context, x + 3, y + 3, leftWidth, h - 6, 4, colorInner);

        float sunW = MSDFRenderer.getStringWidth("Sun", 8);
        float dlcW = MSDFRenderer.getStringWidth("DLC", 8);
        float totalLogoW = sunW + dlcW;
        float logoX = x + 3 + (leftWidth - totalLogoW) / 2f; // Идеальный центр подложки

        MSDFRenderer.drawString(matrix, "Sun", logoX, y + 11.5f, 8, accent);
        MSDFRenderer.drawString(matrix, "DLC", logoX + sunW, y + 11.5f, 8, 0xFFFFFFFF);

        // 3. ПРАВАЯ ПОДЛОЖКА
        float rightX = x + leftWidth + 6;
        RenderUtils.drawRoundedRect(context, rightX, y + 3, currentRightW, h - 6, 4, colorInner);

        if (anim < 0.5f) {
            // --- РЕЖИМ СТАТИСТИКИ (Слот-система) ---
            float statusAlpha = 1.0f - (anim * 2);
            int alphaInt = (int)(255 * statusAlpha);

            if (alphaInt > 5) {
                // Константа центра правой подложки (не меняется от цифр)
                float centerX = rightX + rightBaseW / 2f;

                String sep = "/";
                int sepColor = (alphaInt << 24) | 0x444444; // Темно-серый разделитель
                float sepW = MSDFRenderer.getStringWidth(sep, 7);

                // 1. Рисуем разделитель точно по центру
                MSDFRenderer.drawString(matrix, sep, centerX - sepW / 2f, y + 11f, 7, sepColor);

                // 2. FPS - прижат вправо к разделителю (левый слот)
                String fStr = fps + "fps";
                float fW = MSDFRenderer.getStringWidth(fStr, 7);
                MSDFRenderer.drawString(matrix, fStr, centerX - (sepW / 2f) - fW - 2, y + 11f, 7, (alphaInt << 24) | 0xBBFFFFFF);

                // 3. PING - прижат влево к разделителю (правый слот)
                String pStr = ping + "ms";
                MSDFRenderer.drawString(matrix, pStr, centerX + (sepW / 2f) + 2, y + 11f, 7, (alphaInt << 24) | 0xBBFFFFFF);
            }
        } else {
            // --- РЕЖИМ УВЕДОМЛЕНИЯ ---
            float notifyAlpha = (anim - 0.5f) * 2;
            int alphaInt = (int)(255 * notifyAlpha);
            if (alphaInt > 5) {
                int stateColor = notifyState.equals("Enabled") ? 0xFF88FF88 : 0xFFFF8888;

                // Название модуля
                MSDFRenderer.drawString(matrix, notifyMod, rightX + 5, y + 11f, 7.5f, (alphaInt << 24) | 0xFFFFFFFF);

                // Статус
                float stateW = MSDFRenderer.getStringWidth(notifyState, 6.5f);
                MSDFRenderer.drawString(matrix, notifyState, rightX + currentRightW - stateW - 5, y + 10.5f, 6.5f, (alphaInt << 24) | (stateColor & 0x00FFFFFF));
            }
        }
    }
}