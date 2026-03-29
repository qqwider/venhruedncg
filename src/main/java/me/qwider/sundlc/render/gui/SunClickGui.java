package me.qwider.sundlc.render.gui;

import me.qwider.sundlc.render.Animation;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;

public class SunClickGui extends Screen {
    private static final List<Panel> panels = new ArrayList<>();
    private final Animation openAnim = new Animation(8.0);
    private boolean closing = false; // Флаг начала закрытия
    static {
        float startX = 30;
        for (me.qwider.sundlc.module.Category c : me.qwider.sundlc.module.Category.values()) {
            panels.add(new Panel(c, startX, 30));
            startX += 105;
        }
    }

    public SunClickGui() {
        super(Text.of("ClickGUI"));
    }
    @Override
    public void removed() {
        for (Panel p : panels) {
            // Устанавливаем activeModule в null, чтобы закрыть боковую панель
            try {
                java.lang.reflect.Field field = p.getClass().getDeclaredField("activeModule");
                field.setAccessible(true);
                field.set(p, null);
            } catch (Exception ignored) {}
        }
        me.qwider.sundlc.config.ConfigManager.save();
        super.removed();
    }

    // Сделай список панелей доступным для конфига:
    public static List<Panel> getPanels() { return panels; }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        openAnim.update(!closing);
        float anim = (float) openAnim.getValue();

        if (closing && anim <= 0.001f) {
            super.close();
            return;
        }

        // 1. Улучшенный фон: Радиальный градиент (имитация через fillGradient)
        int alpha = (int) (180 * anim);
        // Затемняем края сильнее, чем центр
        context.fillGradient(0, 0, width, height, (alpha << 24) | 0x050505, (alpha << 24) | 0x101010);

        if (anim < 0.01) return;

        // 2. Отрисовка панелей с небольшим эффектом появления
        context.getMatrices().push();

        // Панели не просто масштабируются, а немного "подлетают"
        float scale = 0.9f + (0.1f * anim);
        context.getMatrices().translate(width / 2f, height / 2f, 0);
        context.getMatrices().scale(scale, scale, 1);
        context.getMatrices().translate(-width / 2f, -height / 2f, 0);

        var matrix = context.getMatrices().peek().getPositionMatrix();
        for (Panel p : panels) {
            p.render(context, matrix, mouseX, mouseY, anim);
        }

        context.getMatrices().pop();
    }

    @Override
    public void close() {
        // Вместо мгновенного закрытия запускаем анимацию назад
        this.closing = true;
    }

    @Override
    public boolean shouldPause() { return false; }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (closing) return false; // Блокируем клики при закрытии
        for (Panel p : panels) p.mouseClicked(mouseX, mouseY, button);
        return super.mouseClicked(mouseX, mouseY, button);
    }
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for (Panel p : panels) p.mouseReleased(mouseX, mouseY, button);
        return super.mouseReleased(mouseX, mouseY, button);
    }


}