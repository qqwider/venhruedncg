package me.qwider.sundlc.render.gui;

import me.qwider.sundlc.render.Animation;
import me.qwider.sundlc.render.MSDFRenderer;
import me.qwider.sundlc.render.RenderUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.joml.Matrix4f;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

public class SunTextField {
    private float x, y, w, h;
    private String text = "";
    private String placeholder;
    private boolean focused = false;
    private int maxLength = 32;
    private long lastBlinkTime = 0;
    private boolean cursorVisible = true;
    private Animation focusAnim = new Animation(15.0);
    private Animation hoverAnim = new Animation(12.0);
    private Runnable onChange;
    private TextRenderer textRenderer;
    
    public SunTextField(float x, float y, float w, float h, String placeholder, TextRenderer textRenderer) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.placeholder = placeholder;
        this.textRenderer = textRenderer;
    }
    
    public void render(DrawContext context, int mouseX, int mouseY, float anim) {
        boolean isHovered = isHovered(mouseX, mouseY);
        hoverAnim.update(isHovered);
        focusAnim.update(focused);
        
        float hoverVal = (float) hoverAnim.getValue();
        float focusVal = (float) focusAnim.getValue();
        
        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();
        
        // Фон поля ввода с анимацией
        int bgAlpha = (int)(255 * anim);
        int bgColor = focused ? 0x2A2A2E : (isHovered ? 0x262629 : 0x222226);
        RenderUtils.drawRoundedRect(context, x, y, w, h, 6, (bgAlpha << 24) | bgColor);
        
        // Обводка при фокусе/наведении
        if (focusVal > 0.01f || hoverVal > 0.01f) {
            float outlineAlpha = Math.max(focusVal, hoverVal * 0.5f);
            RenderUtils.drawRoundedRect(context, x - 1, y - 1, w + 2, h + 2, 7, 
                (int)(60 * outlineAlpha * anim) << 24 | 0x8877FF);
        }
        
        // Текст или плейсхолдер
        String displayText = text.isEmpty() ? placeholder : text;
        int textColor = text.isEmpty() ? 0x666666 : 0xFFFFFF;
        float textAlpha = text.isEmpty() ? 0.7f : 1.0f;
        
        float textX = x + 8;
        float textY = y + h / 2f + 4f;
        
        // Обрезаем текст если он слишком длинный
        float maxTextWidth = w - 16;
        String clippedText = getClippedText(displayText, maxTextWidth, 8f);
        
        MSDFRenderer.drawString(matrix, clippedText, textX, textY, 8f, 
            (int)(255 * anim * textAlpha) << 24 | textColor);
        
        // Курсор
        if (focused) {
            updateCursorBlink();
            if (cursorVisible) {
                float cursorX = textX + MSDFRenderer.getStringWidth(clippedText, 8f);
                RenderUtils.drawRoundedRect(context, cursorX, y + 6, 1, h - 12, 0.5f, 
                    (int)(200 * anim) << 24 | 0x8877FF);
            }
        }
    }
    
    private String getClippedText(String text, float maxWidth, float fontSize) {
        if (MSDFRenderer.getStringWidth(text, fontSize) <= maxWidth) {
            return text;
        }
        
        String clipped = text;
        while (MSDFRenderer.getStringWidth(clipped + "...", fontSize) > maxWidth && clipped.length() > 1) {
            clipped = clipped.substring(0, clipped.length() - 1);
        }
        return clipped + "...";
    }
    
    private void updateCursorBlink() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastBlinkTime > 500) { // Моргание каждые 500ms
            cursorVisible = !cursorVisible;
            lastBlinkTime = currentTime;
        }
    }
    
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovered(mouseX, mouseY)) {
            setFocused(true);
            return true;
        }
        setFocused(false);
        return false;
    }
    
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!focused) return false;
        
        if (Screen.isPaste(keyCode)) {
            paste();
            return true;
        } else if (Screen.isCopy(keyCode)) {
            copy();
            return true;
        } else if (Screen.isCut(keyCode)) {
            cut();
            return true;
        } else if (Screen.isSelectAll(keyCode)) {
            // Можно добавить выделение текста позже
            return true;
        }
        
        return false;
    }
    
    public boolean charTyped(char chr, int modifiers) {
        if (!focused) return false;
        
        if (text.length() < maxLength && chr >= 32 && chr != 127) {
            text += chr;
            if (onChange != null) onChange.run();
            return true;
        }
        return false;
    }
    
    private void paste() {
        try {
            Transferable transferable = java.awt.Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
            if (transferable != null && transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                String pasted = (String) transferable.getTransferData(DataFlavor.stringFlavor);
                if (text.length() + pasted.length() <= maxLength) {
                    text += pasted;
                    if (onChange != null) onChange.run();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void copy() {
        if (!text.isEmpty()) {
            java.awt.Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
                new java.awt.datatransfer.StringSelection(text), null
            );
        }
    }
    
    private void cut() {
        copy();
        text = "";
        if (onChange != null) onChange.run();
    }
    
    public boolean isHovered(double mx, double my) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }
    
    public void setFocused(boolean focused) {
        this.focused = focused;
        if (!focused) {
            cursorVisible = false;
        }
    }
    
    public String getText() { return text; }
    public void setText(String text) { 
        this.text = text; 
        if (onChange != null) onChange.run();
    }
    public void setOnChange(Runnable onChange) { this.onChange = onChange; }
    public void setMaxLength(int maxLength) { this.maxLength = maxLength; }
    public boolean isFocused() { return focused; }
}