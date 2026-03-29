package me.qwider.sundlc.render.gui;

import me.qwider.sundlc.render.Animation;
import me.qwider.sundlc.render.MSDFRenderer;
import me.qwider.sundlc.render.RenderUtils;
import me.qwider.sundlc.util.AltAccount;
import me.qwider.sundlc.util.AltManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

public class AltManageScreen extends Screen {
    private final Screen parent;
    private final Animation fadeIn = new Animation(10.0);
    private CustomTextField nameField;
    private long startTime;

    // Параметры окна
    private final float W = 200, H = 290;

    // Скроллинг
    private float scrollY = 0;
    private float targetScrollY = 0;

    public AltManageScreen(Screen parent) {
        super(Text.of("SunDLC Alt Manager"));
        this.parent = parent;
        this.startTime = System.currentTimeMillis();
    }

    @Override
    protected void init() {
        nameField = new CustomTextField(width / 2f - 85, height / 2f + 75, 170, 22, 16);
        nameField.setPlaceholder("Никнейм игрока...");
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        fadeIn.update(true);
        float anim = (float) fadeIn.getValue();
        float time = (System.currentTimeMillis() - startTime) / 1000f;

        // Плавный скролл
        scrollY = MathHelper.lerp(delta * 0.2f, scrollY, targetScrollY);

        renderBackground(context, anim, time);

        float x = (width - W) / 2f;
        float y = (height - H) / 2f;

        context.getMatrices().push();
        float scale = 0.96f + (anim * 0.04f);
        context.getMatrices().translate(width / 2f, height / 2f, 0);
        context.getMatrices().scale(scale, scale, 1);
        context.getMatrices().translate(-width / 2f, -height / 2f, 0);

        drawShadow(context, x, y, W, H, anim);
        RenderUtils.drawRoundedRect(context, x, y, W, H, 10, (int)(245 * anim) << 24 | 0x111113);

        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();
        drawHeader(context, matrix, x, y, anim);

        // --- СПИСОК С ПРОКРУТКОЙ ---
        float listY = y + 60;
        float listH = 135; // Высота области списка

        // Включаем Scissor (обрезку), чтобы аккаунты не вылезали за пределы области
        context.enableScissor((int)x, (int)listY, (int)(x + W), (int)(listY + listH));

        context.getMatrices().push();
        context.getMatrices().translate(0, scrollY, 0);

        float itemY = listY + 5;
        for (int i = 0; i < AltManager.accounts.size(); i++) {
            renderAccountItem(context, AltManager.accounts.get(i), x + 15, itemY, W - 30, mouseX, (int)(mouseY - scrollY), anim);
            itemY += 28;
        }

        // Расчет максимального скролла
        float totalHeight = AltManager.accounts.size() * 28;
        float maxScroll = Math.max(0, totalHeight - listH + 10);
        targetScrollY = MathHelper.clamp(targetScrollY, -maxScroll, 0);

        context.getMatrices().pop();
        context.disableScissor();
        // --- КОНЕЦ СПИСКА ---

        // Секция ввода
        float inputZoneY = y + 210;
        MSDFRenderer.drawString(matrix, "+", x + 18, inputZoneY - 4, 9, (int)(255 * anim) << 24 | 0x8877FF);
        MSDFRenderer.drawString(matrix, "Создать новый профиль", x + 30, inputZoneY - 5, 6.5f, (int)(160 * anim) << 24 | 0xBBBBBB);

        nameField.render(context, mouseX, mouseY);
        drawLoginButton(context, x + 15, y + H - 35, W - 30, 22, mouseX, mouseY, anim);

        boolean closeHov = isHovered(mouseX, mouseY, x + W - 22, y + 8, 14, 14);
        MSDFRenderer.drawString(matrix, "X", x + W - 18, y + 18, closeHov ? 9 : 8, closeHov ? 0xFFFF4444 : 0x50FFFFFF);

        context.getMatrices().pop();
    }

    private void renderAccountItem(DrawContext context, AltAccount acc, float x, float y, float w, int mx, int my, float anim) {
        boolean hov = isHovered(mx, my, x, y, w, 24);
        boolean active = client.getSession().getUsername().equals(acc.name());

        // Ховер на кнопку удаления (маленький крестик справа)
        boolean deleteHov = isHovered(mx, my, x + w - 20, y + 5, 15, 15);

        int bg = active ? 0x222126 : (hov ? 0x1A1A1D : 0x141416);
        RenderUtils.drawRoundedRect(context, x, y, w, 24, 6, (int)(255 * anim) << 24 | bg);

        if (active) RenderUtils.drawRoundedRect(context, x + 2, y + 6, 1.5f, 12, 0.5f, (int)(255 * anim) << 24 | 0x8877FF);

        Matrix4f m = context.getMatrices().peek().getPositionMatrix();
        MSDFRenderer.drawString(m, acc.name(), x + 10, y + 15f, 7.5f, (int)(255 * anim) << 24 | (active ? 0xFFFFFF : 0x888888));

        // Кнопка удаления (появляется при наведении на карточку)
        if (hov) {
            MSDFRenderer.drawString(m, "x", x + w - 15, y + 14, deleteHov ? 8 : 7, deleteHov ? 0xFFFF4444 : 0x80FFFFFF);
        } else if (active) {
            MSDFRenderer.drawString(m, "online", x + w - 30, y + 14.5f, 5, (int)(200 * anim) << 24 | 0x8877FF);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        targetScrollY += (float) (verticalAmount * 20); // Скорость прокрутки
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (nameField.mouseClicked(mouseX, mouseY, button)) return true;

        float x = (width - W) / 2f, y = (height - H) / 2f;

        // Клик по списку (с учетом скролла)
        float itemY = y + 65 + scrollY;
        for (int i = 0; i < AltManager.accounts.size(); i++) {
            AltAccount acc = AltManager.accounts.get(i);

            // Проверка удаления (сначала, чтобы не сработал логин)
            if (isHovered(mouseX, mouseY, x + W - 35, itemY, 20, 24)) {
                AltManager.accounts.remove(acc);
                AltManager.save();
                me.qwider.sundlc.util.SoundUtil.playClick();
                return true;
            }

            // Проверка логина
            if (isHovered(mouseX, mouseY, x + 15, itemY, W - 40, 24)) {
                AltManager.login(acc.name());
                AltManager.save();
                me.qwider.sundlc.util.SoundUtil.playClick();
                return true;
            }
            itemY += 28;
        }

        if (isHovered(mouseX, mouseY, x + 15, y + H - 35, W - 30, 22)) handleLogin();
        if (isHovered(mouseX, mouseY, x + W - 22, y + 8, 14, 14)) client.setScreen(parent);
        return super.mouseClicked(mouseX, mouseY, button);
    }

    // ... остальные методы (drawHeader, handleLogin, keyPressed, charTyped, renderBackground, drawShadow, isHovered) остаются без изменений
    private void drawHeader(DrawContext context, Matrix4f matrix, float x, float y, float anim) {
        String t1 = "SUN", t2 = "ALTS";
        float tw1 = MSDFRenderer.getStringWidth(t1, 16);
        float tw2 = MSDFRenderer.getStringWidth(t2, 16);
        float startX = x + (W - (tw1 + tw2 + 3)) / 2f;
        MSDFRenderer.drawString(matrix, t1, startX, y + 32, 16, (int)(255 * anim) << 24 | 0x8877FF);
        MSDFRenderer.drawString(matrix, t2, startX + tw1 + 3, y + 32, 16, (int)(255 * anim) << 24 | 0xFFFFFF);
        MSDFRenderer.drawString(matrix, "Менеджер сессий", x + (W - MSDFRenderer.getStringWidth("Менеджер сессий", 6))/2f, y + 43, 6, (int)(100 * anim) << 24 | 0x999999);
        RenderUtils.drawRoundedRect(context, x + 40, y + 52, W - 80, 1, 0, (int)(30 * anim) << 24 | 0xFFFFFF);
    }

    private void drawLoginButton(DrawContext context, float x, float y, float w, float h, int mx, int my, float anim) {
        boolean hov = isHovered(mx, my, x, y, w, h);
        if (hov) RenderUtils.drawRoundedRect(context, x - 1, y - 1, w + 2, h + 2, 7, (int)(40 * anim) << 24 | 0x8877FF);
        int btnColor = hov ? 0x8877FF : 0x222126;
        RenderUtils.drawRoundedRect(context, x, y, w, h, 6, (int)(255 * anim) << 24 | btnColor);
        float tw = MSDFRenderer.getStringWidth("ВОЙТИ В СЕТЬ", 7);
        MSDFRenderer.drawString(context.getMatrices().peek().getPositionMatrix(), "ВОЙТИ В СЕТЬ", x + (w - tw)/2f, y + h/2f + 3.5f, 7, 0xFFFFFFFF);
    }

    private void handleLogin() {
        String name = nameField.getText().trim();
        if (!name.isEmpty()) {
            AltManager.login(name);
            if (AltManager.accounts.stream().noneMatch(a -> a.name().equals(name))) {
                AltManager.accounts.add(new AltAccount(name, null, null, "legacy"));
            }
            AltManager.save();
            nameField.setText("");
            me.qwider.sundlc.util.SoundUtil.playClick();
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (nameField.isFocused() && (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER)) {
            handleLogin();
            return true;
        }
        return nameField.keyPressed(keyCode, scanCode, modifiers) || super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        return nameField.charTyped(chr, modifiers) || super.charTyped(chr, modifiers);
    }

    private void renderBackground(DrawContext context, float anim, float time) {
        context.fill(0, 0, width, height, (int)(255 * anim) << 24 | 0x080809);
        float grid = 30; int color = (int)(15 * anim) << 24 | 0x8877FF;
        for (float i = -grid; i < width + grid; i += grid) RenderUtils.drawRoundedRect(context, i + (time * 10) % grid, 0, 0.5f, height, 0, color);
        for (float i = -grid; i < height + grid; i += grid) RenderUtils.drawRoundedRect(context, 0, i + (time * 10) % grid, width, 0.5f, 0, color);
    }

    private void drawShadow(DrawContext context, float x, float y, float w, float h, float anim) {
        for (int i = 0; i < 10; i++) RenderUtils.drawRoundedRect(context, x - i, y - i, w + i * 2, h + i * 2, 10 + i, (int)((10 - i) * 1.5 * anim) << 24);
    }

    private boolean isHovered(double mx, double my, float x, float y, float w, float h) { return mx >= x && mx <= x + w && my >= y && my <= y + h; }
}