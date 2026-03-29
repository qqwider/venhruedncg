package me.qwider.sundlc.module.settings;

import java.awt.Color;

public class ColorSetting extends Setting {
    public int color;
    public float hue, saturation, brightness, alpha = 1.0f; // Всегда 1.0
    public boolean opened;
    public boolean draggingHSB, draggingHue; // Убрали draggingAlpha

    public ColorSetting(String name, Color defaultColor) {
        super(name);
        this.color = defaultColor.getRGB();
        updateHSB();
    }

    public void updateHSB() {
        float[] hsb = Color.RGBtoHSB((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF, null);
        this.hue = hsb[0];
        this.saturation = hsb[1];
        this.brightness = hsb[2];
    }

    public void updateColor() {
        int rgb = Color.HSBtoRGB(hue, saturation, brightness);
        // Альфа всегда 255 (0xFF)
        this.color = (rgb & 0x00FFFFFF) | (0xFF000000);
    }
}