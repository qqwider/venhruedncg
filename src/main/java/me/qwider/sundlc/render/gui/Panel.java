package me.qwider.sundlc.render.gui;

import me.qwider.sundlc.module.Category;
import me.qwider.sundlc.module.Module;
import me.qwider.sundlc.module.ModuleManager;
import me.qwider.sundlc.render.MSDFRenderer;
import me.qwider.sundlc.render.RenderUtils;
import net.minecraft.client.gui.DrawContext;
import org.joml.Matrix4f;
import java.util.List;

public class Panel {
    public final Category category;
    public final Draggable drag;
    private boolean open = true;

    public Panel(Category category, float x, float y) {
        this.category = category;
        // ИСПРАВЛЕНО: Добавлен ID для сохранения в конфиг
        this.drag = new Draggable("panel_" + category.name(), x, y, 95, 16);
    }

    // МЕТОДЫ ДЛЯ КОНФИГА
    public boolean isOpen() { return open; }
    public void setOpen(boolean open) { this.open = open; }

    public void render(DrawContext context, Matrix4f matrix, int mouseX, int mouseY, float anim) {
        drag.update(mouseX, mouseY);

        float x = drag.x, y = drag.y, w = drag.width, h = drag.height;
        List<Module> modules = ModuleManager.getByCategory(category);

        int alpha = (int)(255 * anim);
        if (alpha < 1) return;

        // Цветовая палитра
        int panelBg = (alpha << 24) | 0x141415;      // Глубокий черный
        int headBg = (alpha << 24) | 0x1D1B1F;       // Цвет заголовка
        int accent = (alpha << 24) | 0x8877FF;       // Основной фиолетовый
        int moduleBg = (alpha << 24) | 0x1A1A1B;     // Цвет выключенного модуля

        float totalHeight = h;
        if (open && !modules.isEmpty()) {
            totalHeight += (modules.size() * 16) + 4;
        }

        // 1. Внешняя тень и основная подложка
        RenderUtils.drawRoundedRect(context, x - 1, y - 1, w + 2, totalHeight + 2, 8, (alpha/4 << 24) | 0x000000);
        RenderUtils.drawRoundedRect(context, x, y, w, totalHeight, 7, panelBg);

        // 2. Заголовок (Header)
        RenderUtils.drawRoundedRect(context, x, y, w, h, 6, headBg);
        // Тонкая линия разделения снизу заголовка
        RenderUtils.drawRoundedRect(context, x + 4, y + h - 1.5f, w - 8, 1, 0, (alpha/3 << 24) | 0xFFFFFF);

        // Текст заголовка (делаем чуть жирнее визуально через размер)
        float tw = MSDFRenderer.getStringWidth(category.name, 10);
        MSDFRenderer.drawString(matrix, category.name, x + (w - tw)/2f, y + 11f, 10, (alpha << 24) | 0xFFFFFF);

        if (open) {
            float currentY = y + h + 3;
            for (Module m : modules) {
                boolean hovered = isHovered(mouseX, mouseY, x + 3, currentY, w - 6, 14);

                // Анимация состояния модуля
                int targetModBg = m.isEnabled() ? 0x24222A : 0x1A1A1B;
                if (hovered) targetModBg = 0x2D2B33;

                // Рисуем кнопку модуля
                RenderUtils.drawRoundedRect(context, x + 3, currentY, w - 6, 14, 4, (alpha << 24) | targetModBg);

                // Если включен - рисуем акцентную полоску слева
                if (m.isEnabled()) {
                    RenderUtils.drawRoundedRect(context, x + 4, currentY + 3, 1.5f, 8, 1, accent);

                    // Эффект "точки" справа для включенного
                    RenderUtils.drawRoundedRect(context, x + w - 10, currentY + 5.5f, 3, 3, 1.5f, accent);
                }

                // Текст модуля (сдвигается, если есть полоска)
                float textX = x + (m.isEnabled() ? 9 : 7);
                int textColor = m.isEnabled() ? 0xFFFFFF : 0xBBBBBB;
                if (hovered) textColor = 0xFFFFFF;

                MSDFRenderer.drawString(matrix, m.getName(), textX, currentY + 10f, 8, (alpha << 24) | textColor);

                currentY += 16;
            }
        }
    }

    public void mouseClicked(double mouseX, double mouseY, int button) {
        drag.onMouseClick((int)mouseX, (int)mouseY, button);
        float x = drag.x, y = drag.y, w = drag.width;

        if (button == 1 && isHovered(mouseX, mouseY, x, y, w, 16)) open = !open;

        if (open && button == 0) {
            float currentY = y + 16 + 3;
            for (Module m : ModuleManager.getByCategory(category)) {
                if (isHovered(mouseX, mouseY, x, currentY, w, 14)) {
                    m.toggle();
                    // СОХРАНЯЕМ ПОСЛЕ КЛИКА
                    me.qwider.sundlc.config.ConfigManager.save();
                }
                currentY += 16;
            }
        }
    }

    private boolean isHovered(double mx, double my, float x, float y, float w, float h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }
}