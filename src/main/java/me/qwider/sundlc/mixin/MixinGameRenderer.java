package me.qwider.sundlc.mixin;

import me.qwider.sundlc.module.ModuleManager;
import me.qwider.sundlc.module.modules.visuals.NoHurtCam;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class MixinGameRenderer {
    @Inject(method = "bobView", at = @At("HEAD"), cancellable = true)
    private void onBobViewWhenHurt(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        // Ищем модуль в списке и проверяем, включен ли он
        ModuleManager.getModules().stream()
                .filter(m -> m instanceof NoHurtCam && m.isEnabled())
                .findFirst()
                .ifPresent(m -> ci.cancel());
    }
}