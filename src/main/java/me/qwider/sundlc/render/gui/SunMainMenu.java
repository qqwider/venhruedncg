package me.qwider.sundlc.render.gui;

import me.qwider.sundlc.render.Animation;
import me.qwider.sundlc.render.MSDFRenderer;
import me.qwider.sundlc.render.RenderUtils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public class SunMainMenu extends Screen {
    private final Animation fadeIn = new Animation(8.0);
    private final List<MenuButton> buttons = new ArrayList<>();
    private long startTime;
    private boolean soundPlayed = false;
    private float soundTriggerTime = 0.8f; // Звук проиграется через 800ms после открытия

    public SunMainMenu() {
        super(Text.of("SunDLC"));
        this.startTime = System.currentTimeMillis();
    }

    @Override
    protected void init() {
        buttons.clear();
        int btnW = 140;
        int btnH = 22;
        int centerX = width / 2 - btnW / 2;

        int startY = height / 2 - 30;

        // Добавляем кнопки с индексом для анимации
        buttons.add(new MenuButton(centerX, startY, btnW, btnH, "Одиночная игра", () -> client.setScreen(new SelectWorldScreen(this)), 0));
        buttons.add(new MenuButton(centerX, startY + 26, btnW, btnH, "Сетевая игра", () -> client.setScreen(new MultiplayerScreen(this)), 1));
        buttons.add(new MenuButton(centerX, startY + 52, btnW, btnH, "Alt Accounts", () -> client.setScreen(new AltManageScreen(this)), 2));
        buttons.add(new MenuButton(centerX, startY + 78, btnW, btnH, "Настройки", () -> client.setScreen(new OptionsScreen(this, client.options)), 3));
        buttons.add(new MenuButton(centerX, startY + 104, btnW, btnH, "Выйти из игры", () -> client.scheduleStop(), 4));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        fadeIn.update(true);
        float anim = (float) fadeIn.getValue();
        float time = (System.currentTimeMillis() - startTime) / 1000f;

        // Проигрываем звук через 800ms после открытия меню
        if (!soundPlayed && time >= soundTriggerTime && client != null && client.getSoundManager() != null) {
            try {
                SoundEvent menuSound = SoundEvent.of(Identifier.of("sundlc", "mainmenu"));
                client.getSoundManager().play(PositionedSoundInstance.master(menuSound, 1.0f));
                soundPlayed = true;
            } catch (Exception e) {
                System.err.println("[SunMainMenu] Не удалось воспроизвести звук: " + e.getMessage());
            }
        }

        // 1. КРУТОЙ ТЕХНО-ФОН (Анимированная сетка) - вместо стандартного фона
        renderCoolBackground(context, anim, time);

        // 2. ЦЕНТРАЛЬНАЯ ПАНЕЛЬ (Увеличена высота для новой кнопки)
        float pW = 170, pH = 226;
        float pX = (width - pW) / 2f, pY = (height - pH) / 2f;

        // Анимация появления панели: масштаб + fade
        float panelAlpha = Math.min(1.0f, time / 1.5f); // Появляется за 1500ms
        panelAlpha = (float) (1 - Math.pow(1 - panelAlpha, 3)); // Ease out cubic
        
        context.getMatrices().push();
        float panelScale = 0.80f + (panelAlpha * 0.20f); // От 0.80 до 1.0
        context.getMatrices().translate(pX + pW / 2f, pY + pH / 2f, 0);
        context.getMatrices().scale(panelScale, panelScale, 1);
        context.getMatrices().translate(-(pX + pW / 2f), -(pY + pH / 2f), 0);

        drawSoftShadow(context, pX, pY, pW, pH, 15, anim * panelAlpha);
        RenderUtils.drawRoundedRect(context, pX, pY, pW, pH, 10, (int)(240 * anim * panelAlpha) << 24 | 0x121214);

        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();

        // 3. ПРИВЕТСТВИЕ с анимацией появления
        float welcomeAlpha = Math.min(1.0f, (time - 0.5f) / 1.0f); // Задержка 500ms
        welcomeAlpha = Math.max(0, welcomeAlpha);
        welcomeAlpha = (float) (1 - Math.pow(1 - welcomeAlpha, 3));
        
        String welcomeText = "Привет, " + client.getSession().getUsername();
        float welcomeW = MSDFRenderer.getStringWidth(welcomeText, 6f);
        
        context.getMatrices().push();
        float welcomeSlide = (1 - welcomeAlpha) * 20; // Сдвиг сверху
        context.getMatrices().translate(0, -welcomeSlide, 0);
        MSDFRenderer.drawString(matrix, welcomeText, pX + (pW - welcomeW) / 2f, pY + 18, 6f, (int)(120 * anim * panelAlpha * welcomeAlpha) << 24 | 0xAAAAAA);
        context.getMatrices().pop();

        // 4. ЛОГОТИП (SUN DLC) с анимацией
        float logoAlpha = Math.min(1.0f, (time - 0.8f) / 1.2f); // Задержка 800ms
        logoAlpha = Math.max(0, logoAlpha);
        logoAlpha = (float) (1 - Math.pow(1 - logoAlpha, 3));
        
        float logoY = pY + 38;
        String part1 = "SUN", part2 = "DLC";
        float fullW = MSDFRenderer.getStringWidth(part1, 22) + MSDFRenderer.getStringWidth(part2, 22) + 2;
        float logoX = pX + (pW - fullW) / 2f;

        context.getMatrices().push();
        float logoScale = 0.75f + (logoAlpha * 0.25f); // От 0.75 до 1.0
        context.getMatrices().translate(pX + pW / 2f, logoY + 11, 0); // +11 это примерно центр текста
        context.getMatrices().scale(logoScale, logoScale, 1);
        context.getMatrices().translate(-(pX + pW / 2f), -(logoY + 11), 0);
        
        MSDFRenderer.drawString(matrix, part1, logoX, logoY, 22, (int)(255 * anim * panelAlpha * logoAlpha) << 24 | 0x8877FF);
        MSDFRenderer.drawString(matrix, part2, logoX + MSDFRenderer.getStringWidth(part1, 22) + 2, logoY, 22, (int)(255 * anim * panelAlpha * logoAlpha) << 24 | 0xFFFFFFFF);
        context.getMatrices().pop();

        // Тонкий разделитель с анимацией
        float dividerAlpha = Math.min(1.0f, (time - 1.2f) / 0.8f);
        dividerAlpha = Math.max(0, dividerAlpha);
        RenderUtils.drawRoundedRect(context, pX + 40, pY + 48, pW - 80, 1, 0.5f, (int)(40 * anim * panelAlpha * dividerAlpha) << 24 | 0xFFFFFF);

        // 5. КНОПКИ С АНИМАЦИЕЙ ПОЯВЛЕНИЯ
        for (MenuButton btn : buttons) {
            btn.render(context, matrix, mouseX, mouseY, anim * panelAlpha, time);
        }

        context.getMatrices().pop();

        // 6. ВЕРСИЯ (ВНИЗУ СЛЕВА) с анимацией
        float versionAlpha = Math.min(1.0f, (time - 2.2f) / 0.8f);
        versionAlpha = Math.max(0, versionAlpha);
        String version = "v1.21.1 | SunDLC Client";
        float verAlpha = 100 * anim * versionAlpha;
        MSDFRenderer.drawString(matrix, version, 10, height - 15, 7, (int)verAlpha << 24 | 0x777777);
    }

    private void renderCoolBackground(DrawContext context, float anim, float time) {
        // Темный фон
        context.fill(0, 0, width, height, (int)(255 * anim) << 24 | 0x080809);

        // Рисуем анимированную сетку (Grid)
        int gridSize = 30;
        int color = (int)(15 * anim) << 24 | 0x8877FF;

        float offsetX = (time * 10) % gridSize;
        float offsetY = (time * 10) % gridSize;

        for (float x = -gridSize; x < width + gridSize; x += gridSize) {
            RenderUtils.drawRoundedRect(context, x + offsetX, 0, 0.5f, height, 0, color);
        }
        for (float y = -gridSize; y < height + gridSize; y += gridSize) {
            RenderUtils.drawRoundedRect(context, 0, y + offsetY, width, 0.5f, 0, color);
        }

        // Виньетка (затемнение по краям)
        context.fillGradient(0, 0, width, height / 3, (int)(150 * anim) << 24 | 0x000000, 0);
        context.fillGradient(0, height - height / 3, width, height, 0, (int)(150 * anim) << 24 | 0x000000);
    }

    private void drawSoftShadow(DrawContext context, float x, float y, float w, float h, int range, float anim) {
        for (int i = 0; i < range; i++) {
            int alpha = (int) ((range - i) * 1.2 * anim);
            RenderUtils.drawRoundedRect(context, x - i, y - i, w + i * 2, h + i * 2, 8 + i, (alpha << 24) | 0x000000);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (MenuButton btn : buttons) {
            if (btn.isHovered(mouseX, mouseY)) {
                // ПРОИГРЫВАЕМ ЗВУК КЛИКА
                me.qwider.sundlc.util.SoundUtil.playClick();

                btn.action.run();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private class MenuButton {
        float x, y, w, h;
        String text;
        Runnable action;
        int index; // Индекс для задержки анимации
        Animation hover = new Animation(15.0);
        Animation appear = new Animation(12.0);

        public MenuButton(float x, float y, float w, float h, String text, Runnable action, int index) {
            this.x = x; this.y = y; this.w = w; this.h = h; this.text = text; this.action = action; this.index = index;
        }

        public void render(DrawContext context, Matrix4f matrix, int mx, int my, float globalAnim, float time) {
            // Анимация появления с задержкой для каждой кнопки
            float delay = 1.5f + (index * 0.15f); // Начинаем с 1500ms + 150ms задержка между кнопками
            float appearProgress = Math.max(0, Math.min(1, (time - delay) / 0.8f)); // 800ms длительность появления
            
            // Ease out cubic для плавности
            appearProgress = (float) (1 - Math.pow(1 - appearProgress, 3));
            
            boolean isH = isHovered(mx, my);
            hover.update(isH);
            float hVal = (float) hover.getValue();

            // АНИМАЦИЯ УВЕЛИЧЕНИЯ (Scale)
            float scale = 1.0f + (hVal * 0.04f);

            context.getMatrices().push();
            
            // Эффект появления: сдвиг снизу вверх + fade
            float slideOffset = (1 - appearProgress) * 20;
            context.getMatrices().translate(0, slideOffset, 0);
            
            // Центрируем трансформацию относительно кнопки
            context.getMatrices().translate(x + w / 2f, y + h / 2f, 0);
            context.getMatrices().scale(scale, scale, 1);
            context.getMatrices().translate(-(x + w / 2f), -(y + h / 2f), 0);

            int alpha = (int)(255 * globalAnim * appearProgress);
            int btnColor = isH ? 0x222126 : 0x18181A;

            // Корпус кнопки
            RenderUtils.drawRoundedRect(context, x, y, w, h, 5, (alpha << 24) | btnColor);

            // Подложка при наведении
            if (isH) {
                RenderUtils.drawRoundedRect(context, x, y, w, h, 5, (int)(30 * alpha / 255.0 * globalAnim * appearProgress) << 24 | 0x8877FF);
            }

            // Легкое фиолетовое свечение контура при наведении
            if (hVal > 0.01f) {
                RenderUtils.drawRoundedRect(context, x, y, w, h, 5, (int)(40 * hVal * globalAnim * appearProgress) << 24 | 0x8877FF);
            }

            float textW = MSDFRenderer.getStringWidth(text, 8f);
            int textCol = isH ? 0xFFFFFFFF : 0x999999;
            if (appearProgress > 0) { // Рендерим только если видимость > 0
                MSDFRenderer.drawString(context.getMatrices().peek().getPositionMatrix(), text, x + (w - textW) / 2f, y + h/2f + 3f, 8f, (alpha << 24) | textCol);
            }

            context.getMatrices().pop();
        }

        public boolean isHovered(double mx, double my) {
            return mx >= x && mx <= x + w && my >= y && my <= y + h;
        }
    }

    @Override public boolean shouldPause() { return false; }
}