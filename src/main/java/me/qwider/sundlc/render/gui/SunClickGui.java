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

    public SunClickGui() {
        super(Text.of("ClickGUI"));
        if (panels.isEmpty()) {
            float startX = 30;
            for (me.qwider.sundlc.module.Category c : me.qwider.sundlc.module.Category.values()) {
                panels.add(new Panel(c, startX, 30));
                startX += 105;
            }
        }
    }
    @Override
    public void removed() {
        me.qwider.sundlc.config.ConfigManager.save(); // Сохраняем при закрытии
        super.removed();
    }

    // Сделай список панелей доступным для конфига:
    public static List<Panel> getPanels() { return panels; }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Обновляем анимацию
        openAnim.update(!closing);
        float anim = (float) openAnim.getValue();

        // Если закрываемся и анимация на нуле - выходим
        if (closing && anim <= 0.001f) {
            super.close();
            return;
        }

        // --- РЕНДЕР ФОНА (ЗАТЕМНЕНИЕ) ---
        // Вычисляем альфу от 0 до 150 (примерно 60% прозрачности)
        int alpha = (int) (150 * anim);
        // Создаем ARGB цвет: альфа смещается на 24 бита влево
        int backgroundColor = (alpha << 24) | 0x000000; // Чисто черный с прозрачностью

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        // Рисуем на весь экран
        context.fill(0, 0, width, height, backgroundColor);
        // --------------------------------

        if (anim < 0.01) return;

        // Отрисовка панелей с анимацией
        context.getMatrices().push();

        context.getMatrices().translate(width / 2f, height / 2f, 0);
        context.getMatrices().scale(anim, anim, 1);
        context.getMatrices().translate(-width / 2f, -height / 2f, 0);

        RenderSystem.setShaderColor(1, 1, 1, 1);
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

}