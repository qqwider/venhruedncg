package me.qwider.sundlc.module.modules.visuals;

import me.qwider.sundlc.module.Category;
import me.qwider.sundlc.module.Module;
import me.qwider.sundlc.module.settings.ColorSetting;
import me.qwider.sundlc.module.settings.ModeSetting;
import me.qwider.sundlc.module.settings.NumberSetting;
import me.qwider.sundlc.render.particle.CustomParticle;
import me.qwider.sundlc.render.particle.ParticleRenderer;
import me.qwider.sundlc.render.particle.ParticleType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import java.util.Random;

public class HitParticles extends Module {
    public static ModeSetting mode = new ModeSetting("Type", "STAR", "STAR", "HEART", "SNOW");
    public static NumberSetting count = new NumberSetting("Count", 1, 20, 10);
    public static ColorSetting color = new ColorSetting("Color", new java.awt.Color(255, 255, 255));

    private int lastHurtTicks = 0;

    public HitParticles() {
        super("HitParticles", Category.VISUALS);
        addSettings(mode, count, color);
    }

    @Override
    public void onTick() {
        if (!this.isEnabled()) return;
        
        LivingEntity target = TargetHUD.target;

        if (target != null && target.isAlive()) {
            // Ловим момент, когда hurtTime УВЕЛИЧИЛСЯ (значит был удар)
            if (target.hurtTime > lastHurtTicks && target.hurtTime > 0) {
                spawnHitParticles(target);
            }
            lastHurtTicks = target.hurtTime;
        } else {
            lastHurtTicks = 0;
        }
    }

    private void spawnHitParticles(LivingEntity entity) {
        Random r = new Random();
        ParticleType type = ParticleType.valueOf(mode.getMode());

        for (int i = 0; i < (int)count.value; i++) {
            // Спавним партиклы снизу сущности
            Vec3d spawnPos = entity.getPos().add(
                (r.nextDouble() - 0.5) * entity.getWidth(),
                0.1, // Чуть выше земли
                (r.nextDouble() - 0.5) * entity.getWidth()
            );
            
            // Вылетают вверх с небольшим разбросом
            Vec3d velocity = new Vec3d(
                (r.nextDouble() - 0.5) * 0.05, // Небольшой разброс по X
                0.08 + r.nextDouble() * 0.04,  // Вверх (0.08-0.12)
                (r.nextDouble() - 0.5) * 0.05  // Небольшой разброс по Z
            );

            ParticleRenderer.add(new CustomParticle(
                spawnPos,
                velocity,
                0.2f + r.nextFloat() * 0.1f,
                color.color,
                3000f + r.nextInt(1000), // Живут 3-4 секунды
                type,
                true // Это партикла от удара
            ));
        }
    }
}