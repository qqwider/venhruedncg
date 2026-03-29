package me.qwider.sundlc.render.gui;

import me.qwider.sundlc.render.MSDFRenderer;
import me.qwider.sundlc.render.RenderUtils;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;

public class CustomTextField {
    private String text = "";
    private final int maxLength;
    private boolean focused = false;
    private long lastCursorBlink = System.currentTimeMillis();
    private boolean cursorVisible = true;
    public float x, y, width, height;
    private String placeholder = "";

    public CustomTextField(float x, float y, float width, float height, int maxLength) {
        this.x = x; this.y = y; this.width = width; this.height = height;
        this.maxLength = maxLength;
    }

    public void render(DrawContext context, int mouseX, int mouseY) {
        // 1. ШЕЙДЕРНАЯ ОБВОДКА (Рисуем прямоугольник на 1px больше)
        // Если в фокусе - фиолетовый, если нет - очень тусклый белый
        int borderColor = focused ? 0xFF8877FF : 0x30FFFFFF;
        RenderUtils.drawRoundedRect(context, x - 1, y - 1, width + 2, height + 2, 5.5f, borderColor);

        // 2. ОСНОВНОЙ ФОН ПОЛЯ (Через шейдер)
        // Делаем его очень темным, чтобы текст читался
        RenderUtils.drawRoundedRect(context, x, y, width, height, 5, 0xFF0F0F11);

        // Логика курсора
        if (focused && System.currentTimeMillis() - lastCursorBlink > 500) {
            cursorVisible = !cursorVisible;
            lastCursorBlink = System.currentTimeMillis();
        }

        // 3. ТЕКСТ (MSDF)
        String displayText = text.isEmpty() && !focused ? placeholder : text;
        int textColor = text.isEmpty() && !focused ? 0x60FFFFFF : 0xFFFFFFFF;

        float textX = x + 8;
        float textY = y + height / 2f + 3.5f;

        if (!displayText.isEmpty()) {
            MSDFRenderer.drawString(context.getMatrices().peek().getPositionMatrix(), displayText, textX, textY, 8, textColor);
        }

        // 4. КУРСОР
        if (focused && cursorVisible) {
            float textW = text.isEmpty() ? 0 : MSDFRenderer.getStringWidth(text, 8);
            // Курсор тоже шейдером для мягкости
            RenderUtils.drawRoundedRect(context, textX + textW + 1, y + 5, 1, height - 10, 0.5f, 0xFF8877FF);
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        focused = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
        if (focused) lastCursorBlink = System.currentTimeMillis();
        return focused;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!focused) return false;
        if (keyCode == GLFW.GLFW_KEY_BACKSPACE && !text.isEmpty()) {
            text = text.substring(0, text.length() - 1);
            return true;
        }
        return false;
    }

    public boolean charTyped(char chr, int modifiers) {
        if (!focused || text.length() >= maxLength) return false;
        if (chr >= 32 && chr != 127) {
            text += chr;
            return true;
        }
        return false;
    }
    public boolean isFocused() {
        return focused;
    }

    public String getText() { return text.trim(); }
    public void setText(String t) { this.text = t; }
    public void setPlaceholder(String p) { this.placeholder = p; }
}