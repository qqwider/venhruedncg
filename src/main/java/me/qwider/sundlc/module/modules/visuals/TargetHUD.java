package me.qwider.sundlc.module.modules.visuals;

import me.qwider.sundlc.module.Category;
import me.qwider.sundlc.module.Module;
import me.qwider.sundlc.module.settings.BooleanSetting;
import me.qwider.sundlc.module.settings.NumberSetting;
import me.qwider.sundlc.render.gui.Draggable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.EntityHitResult;

public class TargetHUD extends Module {
    public static LivingEntity target = null;
    public static float damageAnim = 0;
    public static float lastTargetHealth = 0;
    public static float lastPlayerHealth = 0;
    public static int lingerTicks = 0;

    // Настройки (должны быть созданы в конструкторе и добавлены в addSettings)
    public final BooleanSetting players = new BooleanSetting("Players", true);
    public final BooleanSetting mobs = new BooleanSetting("Mobs", true);
    public final NumberSetting lingerDelay = new NumberSetting("Linger Delay", 0.1, 5.0, 1.0);

    // Позиция
    public static final Draggable pos = new Draggable("TargetHUD", 100, 100, 145, 42);

    public TargetHUD() {
        super("TargetHUD", Category.OVERLAY);
        // ВАЖНО: Добавляем настройки в список модуля, чтобы ConfigManager их увидел
        addSettings(players, mobs, lingerDelay);
    }

    @Override
    public void onTick() {
        var mc = net.minecraft.client.MinecraftClient.getInstance();
        if (mc.player == null) return;

        // 1. Поиск цели
        if (mc.crosshairTarget instanceof EntityHitResult ehr && ehr.getEntity() instanceof LivingEntity le) {
            if (le != mc.player && le.isAlive()) {

                boolean isPlayer = le instanceof PlayerEntity;
                boolean isMob = !isPlayer;

                // Проверка фильтров из настроек
                if ((isPlayer && players.enabled) || (isMob && mobs.enabled)) {
                    target = le;
                    // Обновляем время задержки из настройки
                    lingerTicks = (int) (lingerDelay.value * 20);
                }
            }
        }

        // 2. Обработка таймера
        if (lingerTicks > 0) lingerTicks--;

        // 3. Логика анимации урона
        if (target != null && target.isAlive()) {
            float currentHealth = target.getHealth();
            if (lastTargetHealth > 0 && currentHealth < lastTargetHealth - 0.1f) damageAnim = 1.0f;
            lastTargetHealth = currentHealth;
        } else {
            lastTargetHealth = 0;
        }

        if (damageAnim > 0) damageAnim = Math.max(0, damageAnim - 0.08f);

        if (!isEnabled()) {
            target = null;
            lingerTicks = 0;
        }
    }
}