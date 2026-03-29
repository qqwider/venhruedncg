package me.qwider.sundlc.render;

import com.mojang.blaze3d.systems.RenderSystem;
import me.qwider.sundlc.module.ModuleManager;
import me.qwider.sundlc.module.modules.visuals.TargetESP;
import me.qwider.sundlc.module.modules.visuals.TargetHUD;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public class TargetESPRenderer {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private static final Identifier MARKER_TEX = Identifier.of("sundlc", "textures/esp/marker.png");
    private static final Identifier BLOOM_TEX = Identifier.of("sundlc", "textures/esp/glow.png");

    public static void render(MatrixStack matrices) {
        TargetESP esp = (TargetESP) ModuleManager.getModules().stream().filter(m -> m instanceof TargetESP).findFirst().orElse(null);
        if (esp == null || !esp.isEnabled()) return;

        // Проверка цели
        boolean hasTarget = TargetHUD.target != null && TargetHUD.target.isAlive() && TargetHUD.lingerTicks > 0;
        esp.animation.update(hasTarget);
        float anim = (float) esp.animation.getValue();
        if (anim <= 0.001f) return;

        LivingEntity target = TargetHUD.target;
        if (target == null) return;

        float delta = mc.getRenderTickCounter().getTickDelta(false);
        Vec3d camPos = mc.getEntityRenderDispatcher().camera.getPos();
        Vec3d pos = new Vec3d(
                MathHelper.lerp(delta, target.prevX, target.getX()),
                MathHelper.lerp(delta, target.prevY, target.getY()),
                MathHelper.lerp(delta, target.prevZ, target.getZ())
        ).subtract(camPos);

        // Общее время для анимаций
        float time = (mc.player.age + delta);
        String mode = TargetESP.mode.getMode();

        // ГЛАВНЫЙ ЦИКЛ ВЫБОРА РЕЖИМА
        switch (mode) {
            case "Marker" -> renderMarker(matrices, target, pos, anim, time);
            case "Ghosts" -> renderDoubleHelixGhosts(matrices, target, pos, anim, time);
            case "Cubes" -> renderRichCubes(matrices, target, pos, anim, time);
        }
    }


    // --- 2. GHOSTS (Double Helix) ---
    private static void renderDoubleHelixGhosts(MatrixStack matrices, LivingEntity target, Vec3d pos, float anim, float time) {
        setupRender(BLOOM_TEX);
        RenderSystem.blendFunc(770, 1);

        float radius = Math.max(0.25f, target.getWidth() * (float)TargetESP.ghostsRadius.value);
        float height = target.getHeight();
        int segments = (int)TargetESP.ghostsSegments.value;
        int c = getTargetColor(target);
        float yaw = mc.getEntityRenderDispatcher().camera.getYaw();
        float pitch = mc.getEntityRenderDispatcher().camera.getPitch();
        float speed = (float)TargetESP.ghostsSpeed.value;

        for (int i = 0; i < segments; i++) {
            float fade = 1.0f - (float) i / segments;
            double t = (time * speed) - (i * 0.04);

            // Задержка появления для каждой партиклы (волновой эффект)
            float delay = i / (float) segments;
            float particleAnim = MathHelper.clamp((anim - delay * 0.3f) / (1.0f - delay * 0.3f), 0, 1);

            // Плавная интерполяция (ease out)
            particleAnim = (float) (1 - Math.pow(1 - particleAnim, 3));

            float alpha = particleAnim * fade * 0.6f;
            float particleSize = (0.18f * fade + 0.05f) * particleAnim;

            float sn = (float) Math.sin(t);
            float cs = (float) Math.cos(t);
            float y = (height * 0.5f) + (float) Math.sin(t * 0.7) * (height * 0.45f);

            drawBillboardAt(matrices, pos.x + cs * radius, pos.y + y, pos.z + sn * radius, particleSize, yaw, pitch, c, alpha);
            drawBillboardAt(matrices, pos.x - cs * radius, pos.y + y, pos.z - sn * radius, particleSize, yaw, pitch, c, alpha);
        }
        RenderSystem.defaultBlendFunc();
        endRender();
    }

    // --- 3. CUBES (Rich Style) ---
    private static void renderRichCubes(MatrixStack matrices, LivingEntity target, Vec3d pos, float anim, float time) {
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.disableCull();
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder b = tess.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);

        int count = (int)TargetESP.cubesCount.value;
        float size = (float)TargetESP.cubesSize.value;
        float speed = (float)TargetESP.cubesSpeed.value;
        int color = getTargetColor(target);
        int r = (color >> 16) & 0xFF, g = (color >> 8) & 0xFF, bl = color & 0xFF;

        for (int i = 0; i < count; i++) {
            float angle = (time * speed) + (i * (360f / count));
            float radius = target.getWidth() + 0.4f;
            float ySeed = (float) Math.sin(time * 0.05f + i * 1.5f) * 0.5f + 0.5f;
            
            // Задержка появления для каждого куба
            float delay = i / (float) count;
            float cubeAnim = MathHelper.clamp((anim - delay * 0.4f) / (1.0f - delay * 0.4f), 0, 1);
            
            // Плавная интерполяция (ease out cubic)
            cubeAnim = (float) (1 - Math.pow(1 - cubeAnim, 3));
            
            int cubeAlpha = (int)(120 * cubeAnim);
            float cubeSize = size * cubeAnim;
            
            float x = (float) (pos.x + radius * Math.cos(Math.toRadians(angle)));
            float z = (float) (pos.z + radius * Math.sin(Math.toRadians(angle)));
            float y = (float) (pos.y + (target.getHeight() * 0.1f) + (target.getHeight() * 0.8f * ySeed));
            
            addCubeToBuffer(b, matrices, x, y, z, cubeSize, r, g, bl, cubeAlpha, time + i);
        }
        BufferRenderer.drawWithGlobalProgram(b.end());
        RenderSystem.enableCull();
    }

    // --- 4. MARKER ---
    private static void renderMarker(MatrixStack matrices, LivingEntity target, Vec3d pos, float anim, float time) {
        setupRender(MARKER_TEX);
        RenderSystem.disableDepthTest();
        matrices.push();
        matrices.translate(pos.x, pos.y + target.getHeight() * 0.55, pos.z);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-mc.getEntityRenderDispatcher().camera.getYaw()));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(mc.getEntityRenderDispatcher().camera.getPitch()));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(time * (float)TargetESP.markerSpeed.value));
        float size = (target.getWidth() * (float)TargetESP.markerSize.value) * anim;
        drawTexturedQuad(matrices, size, 0.9f * anim, getTargetColor(target));
        matrices.pop();
        RenderSystem.enableDepthTest();
        endRender();
    }

    // --- СЛУЖЕБНЫЕ МЕТОДЫ ---

    private static int getTargetColor(LivingEntity target) {
        int base = TargetESP.color.color;
        if (target.hurtTime > 0) {
            float hitFactor = target.hurtTime / 10f;
            int r = (int) MathHelper.lerp(hitFactor, (base >> 16) & 0xFF, 255);
            int g = (int) MathHelper.lerp(hitFactor, (base >> 8) & 0xFF, 50);
            int b = (int) MathHelper.lerp(hitFactor, base & 0xFF, 50);
            return (0xFF << 24) | (r << 16) | (g << 8) | b;
        }
        return base;
    }

    private static void drawBillboardAt(MatrixStack ms, double x, double y, double z, float s, float yaw, float pitch, int c, float a) {
        ms.push();
        ms.translate(x, y, z);
        ms.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-yaw));
        ms.multiply(RotationAxis.POSITIVE_X.rotationDegrees(pitch));
        drawTexturedQuad(ms, s, a, c);
        ms.pop();
    }

    private static void setupRender(Identifier tex) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
        RenderSystem.setShaderTexture(0, tex);
    }

    private static void endRender() {
        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
    }

    private static void drawTexturedQuad(MatrixStack matrices, float size, float alpha, int color) {
        Matrix4f m = matrices.peek().getPositionMatrix();
        BufferBuilder b = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        float r = (color >> 16 & 0xFF)/255f, g = (color >> 8 & 0xFF)/255f, bl = (color & 0xFF)/255f;
        float a = ((color >> 24 & 0xFF)/255f) * alpha;
        b.vertex(m, -size, -size, 0).texture(0, 0).color(r, g, bl, a);
        b.vertex(m, -size, size, 0).texture(0, 1).color(r, g, bl, a);
        b.vertex(m, size, size, 0).texture(1, 1).color(r, g, bl, a);
        b.vertex(m, size, -size, 0).texture(1, 0).color(r, g, bl, a);
        BufferRenderer.drawWithGlobalProgram(b.end());
    }

    private static void addCubeToBuffer(BufferBuilder b, MatrixStack ms, float x, float y, float z, float s, int r, int g, int bl, int a, float rot) {
        ms.push();
        ms.translate(x, y, z);
        ms.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rot * 20f));
        ms.multiply(RotationAxis.POSITIVE_X.rotationDegrees(rot * 10f));
        Matrix4f m = ms.peek().getPositionMatrix();
        float h = s / 2f;
        drawFace(b, m, -h, -h, h,  h, -h, h,  h, h, h, -h, h, h, r, g, bl, a);
        drawFace(b, m, -h, -h, -h, -h, h, -h,  h, h, -h, h, -h, -h, (int)(r*0.8), (int)(g*0.8), (int)(bl*0.8), a);
        drawFace(b, m, -h, h, -h, -h, h, h,  h, h, h, h, h, -h, (int)Math.min(255, r*1.1), (int)Math.min(255, g*1.1), (int)Math.min(255, bl*1.1), a);
        drawFace(b, m, -h, -h, -h, h, -h, -h,  h, -h, h, -h, -h, h, (int)(r*0.7), (int)(g*0.7), (int)(bl*0.7), a);
        drawFace(b, m, -h, -h, -h, -h, -h, h, -h, h, h, -h, h, -h, (int)(r*0.9), (int)(g*0.9), (int)(bl*0.9), a);
        drawFace(b, m, h, -h, -h, h, h, -h,  h, h, h, h, -h, h, (int)(r*0.9), (int)(g*0.9), (int)(bl*0.9), a);
        ms.pop();
    }

    private static void drawFace(BufferBuilder b, Matrix4f m, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, int r, int g, int bl, int a) {
        b.vertex(m, x1, y1, z1).color(r, g, bl, a);
        b.vertex(m, x2, y2, z2).color(r, g, bl, a);
        b.vertex(m, x3, y3, z3).color(r, g, bl, a);
    }

    // Перегрузка для совместимости
    private static void drawFace(BufferBuilder b, Matrix4f m, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float x4, float y4, float z4, int r, int g, int bl, int a) {
        drawFace(b, m, x1, y1, z1, x2, y2, z2, x3, y3, z3, r, g, bl, a);
        drawFace(b, m, x1, y1, z1, x3, y3, z3, x4, y4, z4, r, g, bl, a);
    }
}