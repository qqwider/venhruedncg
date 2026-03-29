package me.qwider.sundlc.mixin;

import me.qwider.sundlc.module.ModuleManager;
import me.qwider.sundlc.module.modules.visuals.NoRender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameOverlayRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameOverlayRenderer.class)
public class MixinInGameOverlayRenderer {

    // Оставляем только очистку экрана от огня
    @Inject(method = "renderFireOverlay", at = @At("HEAD"), cancellable = true)
    private static void onRenderFire(MinecraftClient client, MatrixStack matrices, CallbackInfo ci) {
        if (ModuleManager.getModules().stream().anyMatch(m -> m instanceof NoRender && m.isEnabled()) && NoRender.fire.enabled) {
            ci.cancel();
        }
    }

    // МЕТОД onRenderWater ПОЛНОСТЬЮ УДАЛЕН
}