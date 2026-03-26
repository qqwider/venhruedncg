package me.qwider.sundlc.render;

import net.fabricmc.fabric.api.client.rendering.v1.CoreShaderRegistrationCallback;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;

public class ShaderManager {
    private static ShaderProgram roundedShader;
    private static ShaderProgram msdfShader;

    public static void init() {
        CoreShaderRegistrationCallback.EVENT.register(context -> {
            // Оба теперь используют POSITION_TEXTURE_COLOR для передачи UV
            context.register(Identifier.of("sundlc", "rounded"), VertexFormats.POSITION_TEXTURE_COLOR, program -> roundedShader = program);
            context.register(Identifier.of("sundlc", "msdf"), VertexFormats.POSITION_TEXTURE_COLOR, program -> msdfShader = program);
        });
    }

    public static ShaderProgram getRoundedShader() { return roundedShader; }
    public static ShaderProgram getMsdfShader() { return msdfShader; }
}