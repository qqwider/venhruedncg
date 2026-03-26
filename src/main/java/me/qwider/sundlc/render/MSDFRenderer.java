package me.qwider.sundlc.render;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.*;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class MSDFRenderer {
    private static final Identifier TEXTURE = Identifier.of("sundlc", "msdf/semibold.png");
    private static final Identifier JSON_PATH = Identifier.of("sundlc", "msdf/semibold.json");
    private static final Map<Integer, Glyph> glyphs = new HashMap<>();
    private static float atlasWidth, atlasHeight;
    private static boolean loaded = false;

    private static void load() {
        if (loaded) return;
        try {
            var resourceManager = MinecraftClient.getInstance().getResourceManager();
            // Проверка на случай, если ресурсы еще не готовы
            var resourceOpt = resourceManager.getResource(JSON_PATH);
            if (resourceOpt.isEmpty()) return;

            JsonObject json = JsonParser.parseReader(new InputStreamReader(resourceOpt.get().getInputStream())).getAsJsonObject();
            atlasWidth = json.getAsJsonObject("atlas").get("width").getAsFloat();
            atlasHeight = json.getAsJsonObject("atlas").get("height").getAsFloat();

            json.getAsJsonArray("glyphs").forEach(element -> {
                JsonObject g = element.getAsJsonObject();
                int unicode = g.get("unicode").getAsInt();
                if (g.has("atlasBounds")) {
                    JsonObject ab = g.getAsJsonObject("atlasBounds");
                    JsonObject pb = g.getAsJsonObject("planeBounds");
                    glyphs.put(unicode, new Glyph(
                            ab.get("left").getAsFloat(), ab.get("bottom").getAsFloat(), ab.get("right").getAsFloat(), ab.get("top").getAsFloat(),
                            pb.get("left").getAsFloat(), pb.get("bottom").getAsFloat(), pb.get("right").getAsFloat(), pb.get("top").getAsFloat(),
                            g.get("advance").getAsFloat()
                    ));
                } else {
                    glyphs.put(unicode, new Glyph(0,0,0,0,0,0,0,0, g.get("advance").getAsFloat()));
                }
            });
            loaded = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void drawString(Matrix4f matrix, String text, float x, float y, float size, int color) {
        if (!loaded) load();
        ShaderProgram shader = ShaderManager.getMsdfShader();
        if (shader == null || !loaded) return;

        float a = (color >> 24 & 255) / 255f;
        float r = (color >> 16 & 255) / 255f;
        float g = (color >> 8 & 255) / 255f;
        float b = (color & 255) / 255f;

        RenderSystem.setShader(() -> shader);
        RenderSystem.setShaderTexture(0, TEXTURE);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        // КРИТИЧЕСКИ ВАЖНО ДЛЯ ЧЕТКОСТИ:
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

        RenderSystem.enableBlend();
        // Стандартное смешивание для прозрачности
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);

        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        float cursorX = x;
        for (char c : text.toCharArray()) {
            Glyph glyph = glyphs.get((int) c);
            if (glyph == null) continue;
            if (glyph.atlasR() > 0) {
                float x1 = cursorX + glyph.planeL() * size;
                float y1 = y - glyph.planeT() * size;
                float x2 = cursorX + glyph.planeR() * size;
                float y2 = y - glyph.planeB() * size;

                float u1 = glyph.atlasL() / atlasWidth;
                float v1 = 1.0f - glyph.atlasT() / atlasHeight;
                float u2 = glyph.atlasR() / atlasWidth;
                float v2 = 1.0f - glyph.atlasB() / atlasHeight;

                buffer.vertex(matrix, x1, y1, 0).texture(u1, v1).color(r, g, b, a);
                buffer.vertex(matrix, x1, y2, 0).texture(u1, v2).color(r, g, b, a);
                buffer.vertex(matrix, x2, y2, 0).texture(u2, v2).color(r, g, b, a);
                buffer.vertex(matrix, x2, y1, 0).texture(u2, v1).color(r, g, b, a);
            }
            cursorX += glyph.advance() * size;
        }
        BufferRenderer.drawWithGlobalProgram(buffer.end());
    }
    public static float getStringWidth(String text, float size) {
        if (!loaded) load();
        float width = 0;
        for (char c : text.toCharArray()) {
            Glyph glyph = glyphs.get((int) c);
            if (glyph != null) {
                width += glyph.advance() * size;
            }
        }
        return width;
    }

    public record Glyph(float atlasL, float atlasB, float atlasR, float atlasT, float planeL, float planeB, float planeR, float planeT, float advance) {}
}