package me.qwider.sundlc.render;

import com.mojang.blaze3d.systems.RenderSystem;
import me.qwider.sundlc.module.Module;
import me.qwider.sundlc.module.ModuleManager;
import me.qwider.sundlc.module.modules.visuals.TargetHUD;
import me.qwider.sundlc.module.modules.visuals.Potions;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SunHUD {
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    // Для плавного ХП
    private static float targetHudHealth = 0;
    public static void render(DrawContext context) {
        if (mc.player == null) return;

        renderTargetHUD(context);
        DynamicIsland.render(context);
        renderPotions(context);
        renderCoordinates(context);
    }

    // Система анимаций зелий
    private static final java.util.Map<net.minecraft.entity.effect.StatusEffect, Animation> potionAnims = new java.util.HashMap<>();

    private static void renderPotions(DrawContext context) {
        Module m = ModuleManager.getModules().stream().filter(mod -> mod.getName().equals("Potions")).findFirst().orElse(null);
        if (m == null || !m.isEnabled() || mc.player == null) return;

        // 1. ПОЛУЧАЕМ СПИСОК ЭФФЕКТОВ
        // Берем коллекцию активных эффектов
        var activeInstances = mc.player.getStatusEffects();

        // Добавляем эффекты от маяков
        var beaconEffects = getBeaconEffects();
        activeInstances = new java.util.ArrayList<>(activeInstances);
        activeInstances.addAll(beaconEffects);

        // Обновляем анимации - все активные эффекты всегда видны
        for (var instance : activeInstances) {
            var effect = instance.getEffectType().value();
            potionAnims.computeIfAbsent(effect, k -> new Animation(1.0)).update(true);
        }
        
        // Удаляем эффекты которых больше нет
        java.util.Collection<StatusEffectInstance> finalActiveInstances = activeInstances;
        potionAnims.keySet().removeIf(effect ->
            finalActiveInstances.stream().noneMatch(i -> i.getEffectType().value() == effect)
        );
        
        if (potionAnims.isEmpty()) return;

        // 2. ГЕОМЕТРИЯ
        float w = 100;
        float headerH = 14;
        float rowH = 15;

        float totalRowsHeight = rowH * potionAnims.size();
        float totalH = headerH + totalRowsHeight + 4;
        
        // Обновляем размер для Draggable
        Potions.pos.width = w;
        Potions.pos.height = totalH;
        
        float x = Potions.pos.x;
        float y = Potions.pos.y;

        // 3. КОРПУС
        RenderUtils.drawRoundedRect(context, x, y, w, totalH, 6, 0xFF1D1B1F);

        String title = "Potions";
        float titleW = MSDFRenderer.getStringWidth(title, 8f);
        MSDFRenderer.drawString(context.getMatrices().peek().getPositionMatrix(), title, x + (w - titleW) / 2f, y + 10f, 8f, 0xFFFFFFFF);

        // 4. РЕНДЕР СТРОК
        float currentY = y + headerH + 2;

        // Сортируем список по названию (для стабильности)
        List<net.minecraft.entity.effect.StatusEffect> sortedList = new ArrayList<>(potionAnims.keySet());
        sortedList.sort(Comparator.comparing(e -> net.minecraft.client.resource.language.I18n.translate(e.getTranslationKey())));

        for (var effect : sortedList) {
            float a = 1.0f;

            // Находим ДАННЫЕ эффекта у игрока (уровень, время)
            // В 1.21.1 поиск идет через RegistryEntry, поэтому стримим список
            var instance = mc.player.getStatusEffects().stream()
                    .filter(i -> i.getEffectType().value() == effect)
                    .findFirst().orElse(null);

            int alpha = 255;
            RenderUtils.drawRoundedRect(context, x + 3, currentY, w - 6, rowH - 2, 4, (alpha << 24) | 0x222126);

            // --- СЛОТ-СИСТЕМА ---
            float iconSize = 9;
            float timerBlockW = 26;
            float leftPadding = 16;

            if (instance != null) {
                // ИКОНКА (Берем RegistryEntry прямо из инстанса — это фиксит розовые квадраты)
                var sprite = mc.getStatusEffectSpriteManager().getSprite(instance.getEffectType());
                RenderSystem.setShaderColor(1, 1, 1, a);
                context.drawSprite((int)x + 5, (int)currentY + 2, 0, (int)iconSize, (int)iconSize, sprite);
                RenderSystem.setShaderColor(1, 1, 1, 1);

                // ТЕКСТ (Название + Уровень)
                String name = net.minecraft.client.resource.language.I18n.translate(effect.getTranslationKey());
                int level = instance.getAmplifier() + 1;
                String displayName = (level > 1) ? name + " " + level : name;

                // ТАЙМЕР
                int ticks = instance.getDuration();
                String timeStr = (ticks > 32767 || ticks == -1) ? "inf" : String.format("%02d:%02d", (ticks/1200), (ticks/20)%60);

                // 1. Рисуем Таймер (Справа)
                float timeW = MSDFRenderer.getStringWidth(timeStr, 6.5f);
                float timeX = x + w - timerBlockW + (timerBlockW - timeW) / 2f - 2;
                MSDFRenderer.drawString(context.getMatrices().peek().getPositionMatrix(), timeStr, timeX, currentY + 9f, 6.5f, (alpha << 24) | 0x888888);

                // 2. Рисуем Название (Бегущая строка)
                float textX = x + leftPadding;
                float maxTextW = w - leftPadding - timerBlockW - 2;
                float fullTextW = MSDFRenderer.getStringWidth(displayName, 7f);

                // Включаем Scissor (обрезку)
                context.enableScissor((int)textX, (int)currentY, (int)(textX + maxTextW), (int)(currentY + rowH));

                float offsetX = 0;
                if (fullTextW > maxTextW) {
                    float diff = fullTextW - maxTextW;
                    double time = System.currentTimeMillis() / 2000.0;
                    double cycle = time % 4.0;
                    if (cycle < 2.0) {
                        offsetX = -diff * (float) (cycle / 2.0);
                    } else {
                        offsetX = -diff * (float) ((4.0 - cycle) / 2.0);
                    }
                }

                MSDFRenderer.drawString(context.getMatrices().peek().getPositionMatrix(), displayName, textX + offsetX, currentY + 9f, 7f, (alpha << 24) | 0xFFFFFFFF);

                context.disableScissor();
            }

            currentY += rowH;
        }
    }

    public static void renderTargetHUD(DrawContext context) {
        Module m = ModuleManager.getModules().stream().filter(mod -> mod instanceof TargetHUD).findFirst().orElse(null);
        if (m == null || !m.isEnabled()) return;

        // 1. ЗАХВАТ ЦЕЛИ ТЕПЕРЬ ПОЛНОСТЬЮ В TargetHUD.onTick()
        // Это избавляет от дублирования кода и багов с фильтрами

        // 2. ПРОВЕРКА ВАЛИДНОСТИ
        boolean isChat = mc.currentScreen instanceof ChatScreen;

        // Худ виден если есть цель и таймер не кончился, либо если открыт чат (тест режима)
        boolean shouldShow = (TargetHUD.target != null && TargetHUD.target.isAlive() && TargetHUD.lingerTicks > 0) || isChat;

        m.animation.update(shouldShow);
        float anim = (float) m.animation.getValue();

        if (anim <= 0.005f) {
            if (!shouldShow) TargetHUD.target = null;
            return;
        }

        // Выбираем кого рисовать
        LivingEntity entity = (TargetHUD.target != null) ? TargetHUD.target : (isChat ? mc.player : null);
        if (entity == null) return;

        float w = 125, h = 42, x = TargetHUD.pos.x, y = TargetHUD.pos.y;
        TargetHUD.pos.width = w; TargetHUD.pos.height = h;

        // Плавное ХП
        float realHp = entity.getHealth();
        if (targetHudHealth <= 0.1f || Math.abs(targetHudHealth - realHp) > 10) targetHudHealth = realHp;
        targetHudHealth += (realHp - targetHudHealth) * 0.12f;

        context.getMatrices().push();
        context.getMatrices().translate(x + w / 2f, y + h / 2f, 0);
        context.getMatrices().scale(anim, anim, 1);
        context.getMatrices().translate(-(x + w / 2f), -(y + h / 2f), 0);

        int alpha = (int)(255 * anim);
        int colorBase = (alpha << 24) | 0x1D1B1F;
        int colorInner = (alpha << 24) | 0x222126;
        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();

        // Корпус
        RenderUtils.drawRoundedRect(context, x, y, w, h, 6, colorBase);

        // Голова
        RenderUtils.drawRoundedRect(context, x + 4, y + 4, 34, 34, 5, colorInner);
        if (entity instanceof AbstractClientPlayerEntity player) {
            float damageRed = TargetHUD.damageAnim;
            RenderSystem.setShaderColor(1, 1.0f - damageRed, 1.0f - damageRed, anim);
            RenderUtils.drawRoundedSkin(context, player.getSkinTextures().texture(), x + 7, y + 7, 28, 28, 4, anim);
            RenderSystem.setShaderColor(1, 1, 1, 1);
        } else {
            String initial = entity.getName().getString().substring(0, 1).toUpperCase();
            MSDFRenderer.drawString(matrix, initial, x + 16, y + 26.5f, 15, (alpha << 24) | 0xFFFFFF);
        }

        // Инфо-зона
        float infoX = x + 42, infoW = w - 46;
        RenderUtils.drawRoundedRect(context, infoX, y + 4, infoW, h - 8, 4, colorInner);

        // Имя
        String name = entity.getName().getString();
        MSDFRenderer.drawString(matrix, name.length() > 14 ? name.substring(0, 12) + ".." : name, infoX + 5, y + 12.5f, 8, (alpha << 24) | 0xFFFFFF);

        // Броня
        List<ItemStack> armor = new ArrayList<>();
        entity.getArmorItems().forEach(armor::add);
        java.util.Collections.reverse(armor);
        for (int i = 0; i < 4; i++) {
            ItemStack stack = (i < armor.size()) ? armor.get(i) : ItemStack.EMPTY;
            float slotX = infoX + 4 + (i * 13);
            RenderUtils.drawRoundedRect(context, slotX, y + 19, 11, 11, 3, colorBase);
            if (!stack.isEmpty()) {
                context.getMatrices().push();
                context.getMatrices().translate(slotX + 0.5f, y + 19.5f, 150);
                context.getMatrices().scale(0.6f, 0.6f, 1f);
                context.drawItem(stack, 0, 0);
                context.getMatrices().pop();
            }
        }

        // HP Текст и бар
        String hpStr = String.format("%.1f", entity.getHealth());
        float hpW = MSDFRenderer.getStringWidth(hpStr, 8);
        // Отступ от правого края инфо-зоны всего 5 пикселей
        MSDFRenderer.drawString(matrix, hpStr, infoX + infoW - hpW - 5, y + 27, 8, (alpha << 24) | 0xFFFFFF);

        float hpP = Math.max(0.01f, Math.min(1f, targetHudHealth / entity.getMaxHealth()));
        RenderUtils.drawRoundedRect(context, infoX + 4, y + h - 8.5f, barW(infoW), 2.5f, 1f, (alpha/4 << 24) | 0x000000);
        RenderUtils.drawRoundedRect(context, infoX + 4, y + h - 8.5f, barW(infoW) * hpP, 2.5f, 1f, (alpha << 24) | 0x8877FF);

        context.getMatrices().pop();
    }

    private static float barW(float infoW) { return infoW - 10; }

    private static java.util.List<StatusEffectInstance> getBeaconEffects() {
        var effects = new java.util.ArrayList<StatusEffectInstance>();
        if (mc.player == null) return effects;
        
        // Простая реализация для маяков - возвращаем пустой список
        // В будущем можно добавить детекцию маяков через рефлексию или другие методы
        return effects;
    }
    private static void renderCoordinates(DrawContext context) {
        Module m = ModuleManager.getModules().stream()
                .filter(mod -> mod instanceof me.qwider.sundlc.module.modules.visuals.Coordinates)
                .findFirst().orElse(null);

        if (m == null || !m.isEnabled() || mc.player == null) return;

        // 1. ПОЛУЧАЕМ ДАННЫЕ
        String x = String.format("%.1f", mc.player.getX());
        String y = String.format("%.1f", mc.player.getY());
        String z = String.format("%.1f", mc.player.getZ());

        // 2. ГЕОМЕТРИЯ (Расчет размеров слотов)
        float textSize = 7.5f;
        float slotPadding = 4;
        float slotH = 12;
        float spaceBetween = 3;

        // Считаем ширину каждого слота индивидуально под текст
        float wX = MSDFRenderer.getStringWidth("X: " + x, textSize) + (slotPadding * 2);
        float wY = MSDFRenderer.getStringWidth("Y: " + y, textSize) + (slotPadding * 2);
        float wZ = MSDFRenderer.getStringWidth("Z: " + z, textSize) + (slotPadding * 2);

        // Общая ширина и высота внешней подложки
        float totalW = wX + wY + wZ + (spaceBetween * 2) + 6;
        float totalH = 18;

        // Обновляем Draggable для взаимодействия в чате
        me.qwider.sundlc.module.modules.visuals.Coordinates.pos.width = totalW;
        me.qwider.sundlc.module.modules.visuals.Coordinates.pos.height = totalH;

        float posX = me.qwider.sundlc.module.modules.visuals.Coordinates.pos.x;
        float posY = me.qwider.sundlc.module.modules.visuals.Coordinates.pos.y;

        // 3. ЦВЕТА (Твоя палитра)
        int colorBase = 0xFF1D1B1F;  // Темный корпус
        int colorInner = 0xFF222126; // Внутренний слот (подложка)
        int accent = 0xFF8877FF;     // Твой фиолетовый

        // 4. РЕНДЕР КОРПУСА
        RenderUtils.drawRoundedRect(context, posX, posY, totalW, totalH, 6, colorBase);

        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();
        float currentX = posX + 3; // Начальный отступ слева
        float slotY = posY + (totalH - slotH) / 2f; // Центровка слотов по вертикали

        // ОТРИСОВКА СЛОТОВ (X, Y, Z)

        // Слот X
        RenderUtils.drawRoundedRect(context, currentX, slotY, wX, slotH, 4, colorInner);
        drawCoordText(matrix, "X", x, currentX + slotPadding, slotY + 8.5f, textSize, accent);
        currentX += wX + spaceBetween;

        // Слот Y
        RenderUtils.drawRoundedRect(context, currentX, slotY, wY, slotH, 4, colorInner);
        drawCoordText(matrix, "Y", y, currentX + slotPadding, slotY + 8.5f, textSize, accent);
        currentX += wY + spaceBetween;

        // Слот Z
        RenderUtils.drawRoundedRect(context, currentX, slotY, wZ, slotH, 4, colorInner);
        drawCoordText(matrix, "Z", z, currentX + slotPadding, slotY + 8.5f, textSize, accent);
    }

    // Вспомогательный метод для красивого разделения цветов (Префикс: Значение)
    private static void drawCoordText(Matrix4f matrix, String label, String value, float x, float y, float size, int accent) {
        MSDFRenderer.drawString(matrix, label + ":", x, y, size, accent);
        float offset = MSDFRenderer.getStringWidth(label + ": ", size);
        MSDFRenderer.drawString(matrix, value, x + offset, y, size, 0xFFFFFFFF);
    }
}