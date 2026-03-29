package me.qwider.sundlc.mixin;

import me.qwider.sundlc.module.ModuleManager;
import me.qwider.sundlc.module.modules.visuals.NoRender;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class MixinWorldRenderer {

    // В 1.21.1 параметры: MatrixStack, Matrix4f, RenderTickCounter, double, double, double
    @Inject(method = "renderClouds", at = @At("HEAD"), cancellable = true)
    private void onRenderClouds(MatrixStack matrices, Matrix4f matrix4f, Matrix4f matrix4f2, float tickDelta, double cameraX, double cameraY, double cameraZ, CallbackInfo ci) {
        if (ModuleManager.getModules().stream().anyMatch(m -> m instanceof NoRender && m.isEnabled()) && NoRender.clouds.enabled) {
            ci.cancel();
        }
    }

    // В 1.21.1 параметры: LightmapTextureManager, float, double, double, double
    @Inject(method = "renderWeather", at = @At("HEAD"), cancellable = true)
    private void onRenderWeather(LightmapTextureManager manager, float tickDelta, double cameraX, double cameraY, double cameraZ, CallbackInfo ci) {
        if (ModuleManager.getModules().stream().anyMatch(m -> m instanceof NoRender && m.isEnabled()) && NoRender.weather.enabled) {
            ci.cancel();
        }
    }
}