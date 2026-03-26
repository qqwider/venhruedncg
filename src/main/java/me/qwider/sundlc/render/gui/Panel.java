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

        int colorPanelBg = (alpha << 24) | 0x1C1C1D;
        int colorTitleBg = (alpha << 24) | 0x1D1B1F;
        int colorModule = (alpha << 24) | 0x222126;
        int colorModuleEnabled = (alpha << 24) | 0x35333A;
        int textColor = (alpha << 24) | 0xFFFFFF;

        float totalHeight = h + 4;
        if (open && !modules.isEmpty()) {
            totalHeight += (modules.size() * 16) + 2;
        }

        RenderUtils.drawRoundedRect(context, x - 2, y - 2, w + 4, totalHeight, 7, colorPanelBg);
        RenderUtils.drawRoundedRect(context, x, y, w, h, 5, colorTitleBg);

        float tw = MSDFRenderer.getStringWidth(category.name, 11);
        MSDFRenderer.drawString(matrix, category.name, x + (w - tw)/2f, y + 11.5f, 11, textColor);

        if (open) {
            float currentY = y + h + 3;
            for (Module m : modules) {
                int modColor = m.isEnabled() ? colorModuleEnabled : colorModule;
                RenderUtils.drawRoundedRect(context, x, currentY, w, 14, 3, modColor);
                MSDFRenderer.drawString(matrix, m.getName(), x + 6, currentY + 10.2f, 9, textColor);
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
                if (isHovered(mouseX, mouseY, x, currentY, w, 14)) m.toggle();
                currentY += 16;
            }
        }
    }

    private boolean isHovered(double mx, double my, float x, float y, float w, float h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }
}