package me.qwider.sundlc.render;

import com.mojang.blaze3d.systems.RenderSystem;
import me.qwider.sundlc.module.Module;
import me.qwider.sundlc.module.ModuleManager;
import me.qwider.sundlc.module.modules.visuals.ArrayListModule;
import me.qwider.sundlc.module.modules.visuals.TargetHUD;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.EntityHitResult;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SunHUD {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private static LivingEntity lastTarget = null;
    private static float smoothHealth = 0;

    public static void render(DrawContext context) {
        if (mc.player == null) return;

        renderArrayList(context);
        renderTargetHUD(context);
    }

    private static void renderArrayList(DrawContext context) {
        Module arrayModule = ModuleManager.getModules().stream().filter(m -> m instanceof ArrayListModule).findFirst().orElse(null);
        if (arrayModule == null || !arrayModule.isEnabled()) return;

        List<Module> active = ModuleManager.getModules().stream()
                .filter(m -> !(m instanceof ArrayListModule))
                .sorted(Comparator.comparingDouble(m -> -MSDFRenderer.getStringWidth(m.getName(), 9f)))
                .toList();

        float x = 5, y = 5;
        for (Module m : active) {
            m.animation.update(m.isEnabled());
            float anim = (float) m.animation.getValue();
            if (anim < 0.005f) continue;

            float tw = MSDFRenderer.getStringWidth(m.getName(), 9f);
            float w = tw + 14;

            context.getMatrices().push();
            context.getMatrices().translate(x - (w * (1-anim)), y, 0);
            context.getMatrices().scale(anim, anim, 1);

            drawStyledRect(context, 0, 0, w, 14, anim);
            MSDFRenderer.drawString(context.getMatrices().peek().getPositionMatrix(), m.getName(), 6, 10.5f, 9, getAlphaColor(0xFFFFFF, anim));

            context.getMatrices().pop();
            y += 16 * anim;
        }
    }

    public static void renderTargetHUD(DrawContext context) {
        Module m = ModuleManager.getModules().stream().filter(mod -> mod instanceof TargetHUD).findFirst().orElse(null);
        if (m == null || !m.isEnabled()) return;

        // 1. ЗАХВАТ ЦЕЛИ ПРИ НАВОДКЕ
        if (mc.crosshairTarget instanceof net.minecraft.util.hit.EntityHitResult ehr) {
            if (ehr.getEntity() instanceof LivingEntity le && le.isAlive() && le != mc.player) {
                TargetHUD.target = le; // Запоминаем в модуле
            }
        }

        // 2. ПРОВЕРКА ВАЛИДНОСТИ ТЕКУЩЕЙ ЦЕЛИ
        if (TargetHUD.target != null) {
            // Сбрасываем цель если моб мертв или слишком далеко (более 20 блоков)
            if (!TargetHUD.target.isAlive() || 
                mc.player.squaredDistanceTo(TargetHUD.target) > 400) {
                TargetHUD.target = null;
                TargetHUD.smoothHealth = 0;
            }
        }

        // 3. УСЛОВИЕ ОТОБРАЖЕНИЯ
        boolean isChat = mc.currentScreen instanceof ChatScreen;
        boolean hasValidTarget = TargetHUD.target != null;
        boolean shouldShow = hasValidTarget || isChat;

        // 4. АНИМАЦИЯ (FPS-BASED)
        m.animation.update(shouldShow);
        float anim = (float) m.animation.getValue();

        // 5. КОНЕЦ АНИМАЦИИ: Если худ закрылся - забываем цель окончательно
        if (anim <= 0.005f) {
            return;
        }

        // 6. КТО ОТОБРАЖАЕТСЯ
        // Рисуем либо цель из модуля, либо игрока (если в чате и нет цели)
        LivingEntity entity = (TargetHUD.target != null) ? TargetHUD.target : (isChat ? mc.player : null);
        if (entity == null) return;

        // 7. ПАРАМЕТРЫ И ПЛАВНОЕ ХП
        float w = 132, h = 42, x = TargetHUD.pos.x, y = TargetHUD.pos.y;
        TargetHUD.pos.width = w; TargetHUD.pos.height = h;

        float realHp = entity.getHealth();
        if (TargetHUD.smoothHealth <= 0.1f) TargetHUD.smoothHealth = realHp;
        TargetHUD.smoothHealth += (realHp - TargetHUD.smoothHealth) * 0.15f;

        // --- РЕНДЕР ---
        context.getMatrices().push();
        context.getMatrices().translate(x + w / 2f, y + h / 2f, 0);
        context.getMatrices().scale(anim, anim, 1);
        context.getMatrices().translate(-(x + w / 2f), -(y + h / 2f), 0);

        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();
        int alpha = (int)(255 * anim);
        int colorBase = (alpha << 24) | 0x1D1B1F;
        int colorInner = (alpha << 24) | 0x222126;
        int textColor = (alpha << 24) | 0xFFFFFF;

        // Рисуем корпус
        RenderUtils.drawRoundedRect(context, x, y, w, h, 6, colorBase);

        // Рисуем Голову (Используя наш исправленный drawRoundedSkin)
        RenderUtils.drawRoundedRect(context, x + 4, y + 4, 34, 34, 5, colorInner);
        if (entity instanceof net.minecraft.client.network.AbstractClientPlayerEntity player) {
            // Эффект покраснения головы при уроне
            if (entity.equals(TargetHUD.target) && TargetHUD.damageAnim > 0.01f) {
                RenderSystem.setShaderColor(1.0f, 1.0f - TargetHUD.damageAnim, 1.0f - TargetHUD.damageAnim, anim);
            } else {
                RenderSystem.setShaderColor(1, 1, 1, anim);
            }
            RenderUtils.drawRoundedSkin(context, player.getSkinTextures().texture(), x + 7, y + 7, 28, 28, 4, anim);
            RenderSystem.setShaderColor(1, 1, 1, 1);
        } else {
            // Буква моба центрированная
            String initial = entity.getName().getString().substring(0, 1).toUpperCase();
            float iw = MSDFRenderer.getStringWidth(initial, 15);
            MSDFRenderer.drawString(matrix, initial, x + 4 + (34 - iw)/2f, y + 26.5f, 15, textColor);
        }

        // Правая инфо-зона (Подложка)
        float infoX = x + 42, infoW = w - 46;
        RenderUtils.drawRoundedRect(context, infoX, y + 4, infoW, h - 8, 4, colorInner);

        // Текст Имени
        String name = entity.getName().getString();
        MSDFRenderer.drawString(matrix, name.length() > 14 ? name.substring(0, 12) + ".." : name, infoX + 5, y + 12.5f, 8, textColor);

        // Броня
        java.util.List<net.minecraft.item.ItemStack> armor = new java.util.ArrayList<>();
        entity.getArmorItems().forEach(armor::add);
        java.util.Collections.reverse(armor);
        for (int i = 0; i < 4; i++) {
            net.minecraft.item.ItemStack stack = (i < armor.size()) ? armor.get(i) : net.minecraft.item.ItemStack.EMPTY;
            float slotX = infoX + 4 + (i * 13);
            RenderUtils.drawRoundedRect(context, slotX, y + 19, 11, 11, 3, colorBase);
            if (!stack.isEmpty()) {
                context.getMatrices().push();
                float s = 0.6f;
                context.getMatrices().translate(slotX + 0.5f, y + 19.5f, 150);
                context.getMatrices().scale(s, s, 1f);
                context.drawItem(stack, 0, 0);
                context.getMatrices().pop();
            }
        }

        // Текст HP
        String hpStr = String.format("%.1f", entity.getHealth());
        float hpW = MSDFRenderer.getStringWidth(hpStr, 8);
        MSDFRenderer.drawString(matrix, hpStr, infoX + infoW - hpW - 12, y + 27, 8, textColor);
        MSDFRenderer.drawString(matrix, "hp", infoX + infoW - 11, y + 27, 6.5f, (alpha << 24) | 0x888888);

        // Полоска HP
        float barW = infoW - 8;
        float hpP = Math.max(0.01f, Math.min(1f, TargetHUD.smoothHealth / entity.getMaxHealth()));
        RenderUtils.drawRoundedRect(context, infoX + 4, y + h - 8.5f, barW, 2.5f, 1f, (alpha/4 << 24) | 0x000000);
        RenderUtils.drawRoundedRect(context, infoX + 4, y + h - 8.5f, barW * hpP, 2.5f, 1f, (alpha << 24) | 0x8877FF);

        context.getMatrices().pop();
    }

    // Хелпер для двухслойных прямоугольников SunDLC
    private static void drawStyledRect(DrawContext context, float x, float y, float w, float h, float anim) {
        RenderUtils.drawRoundedRect(context, x, y, w, h, 4, getAlphaColor(0x1D1B1F, anim));
        RenderUtils.drawRoundedRect(context, x + 1.5f, y + 1.5f, w - 3f, h - 3f, 3, getAlphaColor(0x222126, anim));
    }

    // Хелпер для альфа-канала
    private static int getAlphaColor(int rgb, float anim) {
        int alpha = (int)(255 * anim);
        return (alpha << 24) | (rgb & 0x00FFFFFF);
    }
}