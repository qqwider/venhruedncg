package me.qwider.sundlc.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;

public class RenderUtils {
    public static void drawRoundedRect(DrawContext context, float x, float y, float w, float h, float r, int color) {
        drawRoundedRectCustom(context, x, y, w, h, r, r, r, r, color);
    }

    public static void drawRoundedRectCustom(DrawContext context, float x, float y, float w, float h, float tl, float tr, float br, float bl, int color) {
        ShaderProgram shader = ShaderManager.getRoundedShader();
        if (shader == null) return;

        float a = ((color >> 24) & 0xFF) / 255f;
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;

        shader.getUniform("Size").set(w, h);
        shader.getUniform("Radius").set(tl, tr, br, bl);

        RenderSystem.setShader(() -> shader);
        RenderSystem.enableBlend();

        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();
        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);

        // Передаем UV координаты (0,0), (0,1), (1,1), (1,0) для шейдера
        buffer.vertex(matrix, x, y, 0).texture(0, 0).color(r, g, b, a);
        buffer.vertex(matrix, x, y + h, 0).texture(0, 1).color(r, g, b, a);
        buffer.vertex(matrix, x + w, y + h, 0).texture(1, 1).color(r, g, b, a);
        buffer.vertex(matrix, x + w, y, 0).texture(1, 0).color(r, g, b, a);

        BufferRenderer.drawWithGlobalProgram(buffer.end());
    }
    public static void drawRoundedSkin(DrawContext context, Identifier tex, float x, float y, float w, float h, float r, float alpha) {
        ShaderProgram shader = ShaderManager.getRoundedShader();
        if (shader == null) return;

        shader.getUniform("Size").set(w, h);
        shader.getUniform("Radius").set(r, r, r, r);
        shader.getUniform("UseTexture").set(1);

        // Передаем координаты лица (8,8 -> 16,16) в атласе 64x64
        shader.getUniform("AtlasRegion").set(8f/64f, 8f/64f, 16f/64f, 16f/64f);

        RenderSystem.setShader(() -> shader);
        RenderSystem.setShaderTexture(0, tex);
        RenderSystem.enableBlend();

        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();
        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);

        // ВАЖНО: передаем 0.0-1.0. Это нужно шейдеру для правильного скругления!
        buffer.vertex(matrix, x, y, 0).texture(0, 0).color(1, 1, 1, alpha);
        buffer.vertex(matrix, x, y + h, 0).texture(0, 1).color(1, 1, 1, alpha);
        buffer.vertex(matrix, x + w, y + h, 0).texture(1, 1).color(1, 1, 1, alpha);
        buffer.vertex(matrix, x + w, y, 0).texture(1, 0).color(1, 1, 1, alpha);

        BufferRenderer.drawWithGlobalProgram(buffer.end());
        shader.getUniform("UseTexture").set(0);
    }

    public static void drawRoundedRectOutline(DrawContext context, float x, float y, float w, float h, float r, float thickness, int color) {
        // Рисуем 4 линии для обводки
        float a = ((color >> 24) & 0xFF) / 255f;
        float red = ((color >> 16) & 0xFF) / 255f;
        float green = ((color >> 8) & 0xFF) / 255f;
        float blue = (color & 0xFF) / 255f;

        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();
        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        // Верхняя линия
        buffer.vertex(matrix, x + r, y, 0).color(red, green, blue, a);
        buffer.vertex(matrix, x + r, y + thickness, 0).color(red, green, blue, a);
        buffer.vertex(matrix, x + w - r, y + thickness, 0).color(red, green, blue, a);
        buffer.vertex(matrix, x + w - r, y, 0).color(red, green, blue, a);

        // Нижняя линия
        buffer.vertex(matrix, x + r, y + h - thickness, 0).color(red, green, blue, a);
        buffer.vertex(matrix, x + r, y + h, 0).color(red, green, blue, a);
        buffer.vertex(matrix, x + w - r, y + h, 0).color(red, green, blue, a);
        buffer.vertex(matrix, x + w - r, y + h - thickness, 0).color(red, green, blue, a);

        // Левая линия
        buffer.vertex(matrix, x, y + r, 0).color(red, green, blue, a);
        buffer.vertex(matrix, x, y + h - r, 0).color(red, green, blue, a);
        buffer.vertex(matrix, x + thickness, y + h - r, 0).color(red, green, blue, a);
        buffer.vertex(matrix, x + thickness, y + r, 0).color(red, green, blue, a);

        // Правая линия
        buffer.vertex(matrix, x + w - thickness, y + r, 0).color(red, green, blue, a);
        buffer.vertex(matrix, x + w - thickness, y + h - r, 0).color(red, green, blue, a);
        buffer.vertex(matrix, x + w, y + h - r, 0).color(red, green, blue, a);
        buffer.vertex(matrix, x + w, y + r, 0).color(red, green, blue, a);

        BufferRenderer.drawWithGlobalProgram(buffer.end());
    }
}