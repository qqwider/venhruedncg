package me.qwider.sundlc.render;

import com.mojang.blaze3d.systems.RenderSystem;
import me.qwider.sundlc.module.ModuleManager;
import me.qwider.sundlc.module.modules.visuals.NameTags;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;

public class NameTagsRenderer {
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public static void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers) {
        boolean enabled = ModuleManager.getModules().stream()
                .anyMatch(m -> m instanceof NameTags && m.isEnabled());

        if (!enabled || mc.world == null || mc.player == null) return;

        // Очищаем цвет перед рендером, чтобы не было наложения оттенков от мира
        RenderSystem.setShaderColor(1, 1, 1, 1);

        for (PlayerEntity player : mc.world.getPlayers()) {
            if ((player == mc.player && mc.options.getPerspective().isFirstPerson())
                    || !player.isAlive() || player.isInvisible()) continue;

            double tickDelta = mc.getRenderTickCounter().getTickDelta(false);

            // Расчет позиции относительно камеры
            double x = player.prevX + (player.getX() - player.prevX) * tickDelta - mc.getEntityRenderDispatcher().camera.getPos().x;
            double y = player.prevY + (player.getY() - player.prevY) * tickDelta - mc.getEntityRenderDispatcher().camera.getPos().y;
            double z = player.prevZ + (player.getZ() - player.prevZ) * tickDelta - mc.getEntityRenderDispatcher().camera.getPos().z;

            renderTag(player, x, y + player.getHeight() + 0.50, z, matrices);
        }
    }

    private static void renderTag(PlayerEntity entity, double x, double y, double z, MatrixStack matrices) {
        // Используем DrawContext с Immediate потребителем
        DrawContext context = new DrawContext(mc, mc.getBufferBuilders().getEntityVertexConsumers());
        MatrixStack stack = context.getMatrices();

        stack.push();
        // В WorldRenderEvents.LAST матрицы уже могут быть смещены, поэтому используем переданные координаты x,y,z
        stack.translate(x, y, z);

        stack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-mc.getEntityRenderDispatcher().camera.getYaw()));
        stack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(mc.getEntityRenderDispatcher().camera.getPitch()));

        float dist = (float) mc.getEntityRenderDispatcher().camera.getPos().distanceTo(entity.getPos());
        float scale = 0.012f + (dist * 0.0012f);
        scale = Math.min(scale, 0.04f);
        stack.scale(-scale, -scale, scale);

        // --- УЛЬТИМАТИВНЫЙ ФИКС БАГОВ РЕНДЕРА ---
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest(); // Тэг поверх всего
        RenderSystem.depthMask(false);    // Запрещаем писать в буфер глубины (убирает баги с водой)

        String name = entity.getName().getString();
        int ping = 0;
        PlayerListEntry entry = mc.getNetworkHandler().getPlayerListEntry(entity.getUuid());
        if (entry != null) ping = entry.getLatency();
        String pingStr = ping + "ms";

        float nameW = MSDFRenderer.getStringWidth(name, 10);
        float pingW = MSDFRenderer.getStringWidth(pingStr, 8);
        float sepW = MSDFRenderer.getStringWidth(" / ", 8);

        float innerW = nameW + sepW + pingW + 8;
        float totalW = innerW + 4;
        float h = 16;
        float rectX = -totalW / 2f;

        // 1. Корпус
        RenderUtils.drawRoundedRect(context, rectX, 0, totalW, h, 5, 0xCC1D1B1F);

        // 2. Подложка (с небольшим Z-сдвигом)
        stack.push();
        stack.translate(0, 0, -0.1f);
        float innerX = rectX + 2;
        RenderUtils.drawRoundedRect(context, innerX, 2, innerW, h - 4, 4, 0xDD222126);

        // 3. Текст
        stack.translate(0, 0, -0.1f);
        Matrix4f matrix = stack.peek().getPositionMatrix();
        float textStartX = innerX + (innerW - (nameW + sepW + pingW)) / 2f;

        MSDFRenderer.drawString(matrix, name, textStartX, 11.2f, 10, 0xFFFFFFFF);
        MSDFRenderer.drawString(matrix, " / ", textStartX + nameW, 11.2f, 8, 0xFF444444);
        MSDFRenderer.drawString(matrix, pingStr, textStartX + nameW + sepW, 11.2f, 8, 0xFF8877FF);

        stack.pop();

        // Возвращаем состояния
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        stack.pop();

        // Сбрасываем цвет после себя
        RenderSystem.setShaderColor(1, 1, 1, 1);
    }
}