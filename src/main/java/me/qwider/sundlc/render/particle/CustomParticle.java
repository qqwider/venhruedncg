package me.qwider.sundlc.render.particle;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import java.util.Random;

public class CustomParticle {
    public Vec3d pos;
    public Vec3d velocity;
    public float alpha, baseSize, currentSize, maxLife;
    public long startTime;
    public ParticleType type;
    public int color;
    private final double swaySeed;
    private static final Random random = new Random();
    private float customAge = 0;
    public boolean isHitParticle = false; // Флаг для партиклов от удара
    private boolean onGround = false;

    public CustomParticle(Vec3d pos, Vec3d velocity, float size, int color, float lifeMs, ParticleType type) {
        this(pos, velocity, size, color, lifeMs, type, false);
    }

    public CustomParticle(Vec3d pos, Vec3d velocity, float size, int color, float lifeMs, ParticleType type, boolean isHitParticle) {
        this.pos = pos;
        this.velocity = velocity;
        this.baseSize = size;
        this.currentSize = size;
        this.color = color;
        this.maxLife = lifeMs;
        this.startTime = System.currentTimeMillis();
        this.type = type;
        this.swaySeed = random.nextDouble() * 1000;
        this.isHitParticle = isHitParticle;
    }

    public void update(boolean isInSight) {
        double time = (System.currentTimeMillis() + swaySeed) / 2000.0;
        
        if (isHitParticle) {
            // Логика для партиклов от удара с плавной физикой
            if (!onGround) {
                // Применяем движение
                this.pos = this.pos.add(velocity);
                
                // Мягкая гравитация
                velocity = velocity.add(0, -0.008, 0);
                
                // Проверяем столкновение с землёй
                MinecraftClient mc = MinecraftClient.getInstance();
                if (mc.world != null) {
                    BlockPos below = BlockPos.ofFloored(pos.x, pos.y - 0.05, pos.z);
                    if (!mc.world.getBlockState(below).isAir() && velocity.y < 0) {
                        // Мягкий отскок с большой потерей энергии
                        velocity = new Vec3d(velocity.x * 0.7, -velocity.y * 0.3, velocity.z * 0.7);
                        
                        // Если скорость слишком мала, останавливаемся
                        if (Math.abs(velocity.y) < 0.015) {
                            onGround = true;
                            velocity = Vec3d.ZERO;
                            // Выравниваем по поверхности блока
                            this.pos = new Vec3d(pos.x, Math.floor(pos.y) + 1.01, pos.z);
                        }
                    }
                }
                
                // Сильное трение воздуха для плавности
                velocity = velocity.multiply(0.96, 1.0, 0.96);
            } else {
                // Лежим на земле, слегка покачиваемся
                double swayX = Math.sin(time * 0.3) * 0.0005;
                double swayZ = Math.cos(time * 0.3) * 0.0005;
                this.pos = this.pos.add(swayX, 0, swayZ);
            }
        } else {
            // Логика для обычных фоновых партиклов
            double swayX = Math.sin(time * 0.5) * 0.002;
            double swayY = Math.cos(time * 0.3) * 0.001;
            double swayZ = Math.sin(time * 0.4) * 0.002;
            
            this.pos = this.pos.add(
                velocity.x + swayX,
                velocity.y + swayY,
                velocity.z + swayZ
            );

            velocity = velocity.add(
                (random.nextDouble() - 0.5) * 0.0001,
                (random.nextDouble() - 0.5) * 0.0001,
                (random.nextDouble() - 0.5) * 0.0001
            );
            
            double speed = velocity.length();
            if (speed > 0.02) {
                velocity = velocity.normalize().multiply(0.02);
            }
        }

        // МЯГКОЕ СТАРЕНИЕ
        float delta = isInSight ? 1.0f : 2.5f;
        customAge += delta;

        // Пульсация размера
        float pulse = (float) Math.sin(time * 2.0) * 0.02f;
        this.currentSize = baseSize + pulse;

        float progress = (customAge * 50f) / maxLife;

        // Плавное появление и исчезновение
        if (progress < 0.1f) {
            alpha = progress / 0.1f;
        } else {
            alpha = Math.max(0, 1.0f - ((progress - 0.1f) / 0.9f));
        }
    }

    public boolean isDead() {
        return (customAge * 50f > maxLife) || alpha <= 0;
    }
}