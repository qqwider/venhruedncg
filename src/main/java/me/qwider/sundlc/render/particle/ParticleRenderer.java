package me.qwider.sundlc.render.particle;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

public class ParticleRenderer {
    public static final List<CustomParticle> particles = new CopyOnWriteArrayList<>();
    private static final Random random = new Random();

    public static void add(CustomParticle p) {
        particles.add(p);
    }

    public static void render(MatrixStack matrices, VertexConsumerProvider consumers) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;

        Vec3d lookVec = mc.player.getRotationVec(1.0f);
        Vec3d cameraPos = mc.getEntityRenderDispatcher().camera.getPos();

        // 1. ОБНОВЛЕНИЕ С УЧЕТОМ ВИДИМОСТИ
        particles.removeIf(p -> {
            Vec3d toParticle = p.pos.subtract(cameraPos).normalize();
            // Частица в зоне видимости, если угол обзора ~120 градусов
            boolean isInSight = lookVec.dotProduct(toParticle) > 0.2;
            
            p.update(isInSight);
            
            double distSq = p.pos.squaredDistanceTo(mc.player.getPos());
            return p.isDead() || distSq > 3600; // 60 блоков макс
        });

        // 2. УМНЫЙ СПАВН (только если модуль Particles включен)
        var particlesModule = me.qwider.sundlc.module.ModuleManager.getModule("Particles");
        if (particlesModule != null && particlesModule.isEnabled()) {
            spawnBackgroundParticles(mc);
        }

        if (particles.isEmpty()) return;

        // 3. РЕНДЕР ПО ТИПАМ
        Quaternionf cameraRotation = mc.getEntityRenderDispatcher().camera.getRotation();

        for (ParticleType type : ParticleType.values()) {
            VertexConsumer buffer = consumers.getBuffer(type.renderLayer);

            for (CustomParticle p : particles) {
                if (p.type != type) continue;

                matrices.push();
                matrices.translate(p.pos.x - cameraPos.x, p.pos.y - cameraPos.y, p.pos.z - cameraPos.z);
                matrices.multiply(cameraRotation);

                float s = p.currentSize;
                if (s <= 0) { 
                    matrices.pop(); 
                    continue; 
                }

                Matrix4f matrix = matrices.peek().getPositionMatrix();
                
                int c = p.color;
                float r = (c == -1) ? 1f : ((c >> 16) & 0xFF) / 255f;
                float g = (c == -1) ? 1f : ((c >> 8) & 0xFF) / 255f;
                float b = (c == -1) ? 1f : (c & 0xFF) / 255f;

                drawVertex(buffer, matrix, -s, -s, 0, 1, r, g, b, p.alpha);
                drawVertex(buffer, matrix, -s, s, 0, 0, r, g, b, p.alpha);
                drawVertex(buffer, matrix, s, s, 1, 0, r, g, b, p.alpha);
                drawVertex(buffer, matrix, s, -s, 1, 1, r, g, b, p.alpha);

                matrices.pop();
            }
        }
    }

    private static void spawnBackgroundParticles(MinecraftClient mc) {
        int maxParticles = (int) me.qwider.sundlc.module.modules.visuals.Particles.amount.value;
        
        if (particles.size() < maxParticles) {
            // Спавним по 3 штуки за раз для плотности
            for (int i = 0; i < 3; i++) {
                Vec3d spawnPos;
                
                // 85% спавним широким фронтом перед игроком, 15% сзади/сбоку
                if (random.nextDouble() < 0.85) {
                    spawnPos = findSpawnPosWideFront(mc);
                } else {
                    spawnPos = findSpawnPosEverywhere(mc);
                }
                
                if (spawnPos == null) continue;

                String modeStr = me.qwider.sundlc.module.modules.visuals.Particles.mode.getMode();
                ParticleType type = ParticleType.valueOf(modeStr);
                int color = me.qwider.sundlc.module.modules.visuals.Particles.color.color;

                particles.add(new CustomParticle(
                    spawnPos,
                    new Vec3d(
                        (random.nextDouble() - 0.5) * 0.008, // Медленное движение во все стороны
                        (random.nextDouble() - 0.5) * 0.008,
                        (random.nextDouble() - 0.5) * 0.008
                    ),
                    0.15f + random.nextFloat() * 0.12f,
                    color,
                    18000 + random.nextInt(7000), // 18-25 секунд
                    type
                ));
            }
        }
    }

    // Спавн ШИРОКИМ фронтом перед игроком (180 градусов по горизонтали)
    private static Vec3d findSpawnPosWideFront(MinecraftClient mc) {
        // Берем Yaw и отклоняем на ±90 градусов
        float yaw = mc.player.getYaw() + (random.nextFloat() * 180 - 90);
        double radYaw = Math.toRadians(-yaw);

        // Дистанция от 10 до 30 блоков
        double dist = 10 + random.nextDouble() * 20;

        // Распределяем по высоте независимо от взгляда (от -10 до +15 блоков)
        double yOffset = random.nextDouble() * 25 - 10;

        double x = Math.sin(radYaw) * dist;
        double z = Math.cos(radYaw) * dist;

        Vec3d target = mc.player.getPos().add(x, yOffset, z);
        if (!mc.world.getBlockState(BlockPos.ofFloored(target)).isAir()) return null;
        return target;
    }

    // Спавн ГДЕ УГОДНО вокруг (для атмосферы)
    private static Vec3d findSpawnPosEverywhere(MinecraftClient mc) {
        double dist = 5 + random.nextDouble() * 25;
        double theta = random.nextDouble() * 2.0 * Math.PI;
        double phi = Math.acos(2.0 * random.nextDouble() - 1.0);
        
        Vec3d target = mc.player.getPos().add(
            dist * Math.sin(phi) * Math.cos(theta),
            dist * Math.sin(phi) * Math.sin(theta),
            dist * Math.cos(phi)
        );
        if (!mc.world.getBlockState(BlockPos.ofFloored(target)).isAir()) return null;
        return target;
    }

    private static void drawVertex(VertexConsumer buffer, Matrix4f matrix, float x, float y, float u, float v, float r, float g, float b, float a) {
        buffer.vertex(matrix, x, y, 0).color(r, g, b, a).texture(u, v)
                .overlay(OverlayTexture.DEFAULT_UV).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).normal(0, 1, 0);
    }
}