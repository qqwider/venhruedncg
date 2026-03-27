package me.qwider.sundlc.module.modules.visuals;

import me.qwider.sundlc.module.Category;
import me.qwider.sundlc.module.Module;
import me.qwider.sundlc.render.gui.Draggable;
import net.minecraft.entity.LivingEntity;

public class TargetHUD extends Module {
    // Переменные, к которым обращаются миксины и рендерер
    public static LivingEntity target = null;
    public static float damageAnim = 0;
    public static float smoothHealth = 0;
    public static float lastTargetHealth = 0;
    public static float lastPlayerHealth = 0;

    public static final Draggable pos = new Draggable("TargetHUD", 100, 100, 132, 42);

    public TargetHUD() {
        super("TargetHUD", Category.OVERLAY);
    }

    @Override
    public void onTick() {
        var mc = net.minecraft.client.MinecraftClient.getInstance();
        
        // Инициализация начальных значений здоровья
        if (lastTargetHealth == 0 && target != null && target.isAlive()) {
            lastTargetHealth = target.getHealth();
        }
        if (lastPlayerHealth == 0 && mc.player != null) {
            lastPlayerHealth = mc.player.getHealth();
        }
        
        // Отслеживаем изменение здоровья цели
        if (target != null && target.isAlive()) {
            float currentHealth = target.getHealth();
            if (lastTargetHealth > 0 && currentHealth < lastTargetHealth - 0.1f) {
                // Здоровье уменьшилось - активируем анимацию
                damageAnim = 1.0f;
                System.out.println("Target health decreased: " + lastTargetHealth + " -> " + currentHealth);
            }
            lastTargetHealth = currentHealth;
        } else {
            lastTargetHealth = 0;
        }
        
        // Отслеживаем изменение собственного здоровья (для работы в чате)
        if (mc.player != null) {
            float currentPlayerHealth = mc.player.getHealth();
            if (lastPlayerHealth > 0 && currentPlayerHealth < lastPlayerHealth - 0.1f) {
                // Наше здоровье уменьшилось - активируем анимацию
                damageAnim = 1.0f;
                System.out.println("Player health decreased: " + lastPlayerHealth + " -> " + currentPlayerHealth);
            }
            lastPlayerHealth = currentPlayerHealth;
        } else {
            lastPlayerHealth = 0;
        }
        
        // Уменьшаем эффект покраснения со временем (более плавно)
        if (damageAnim > 0) damageAnim = Math.max(0, damageAnim - 0.08f);

        // Если модуль выключен - принудительно чистим цель
        if (!isEnabled()) target = null;
    }
}