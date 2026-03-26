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

    public static final Draggable pos = new Draggable("TargetHUD", 100, 100, 132, 42);

    public TargetHUD() {
        super("TargetHUD", Category.OVERLAY);
    }

    @Override
    public void onTick() {
        // Уменьшаем эффект покраснения со временем
        if (damageAnim > 0) damageAnim -= 0.05f;

        // Если модуль выключен - принудительно чистим цель
        if (!isEnabled()) target = null;
    }
}