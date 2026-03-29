package me.qwider.sundlc.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import me.qwider.sundlc.module.ModuleManager;
import me.qwider.sundlc.module.modules.visuals.Ambience;
import net.minecraft.client.render.BackgroundRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BackgroundRenderer.class)
public class MixinBackgroundRenderer {

    /**
     * Используем этот метод, так как он виден в твоих подсказках.
     * Он отвечает за передачу цвета тумана в видеокарту.
     */
    @Inject(method = "applyFogColor", at = @At("HEAD"), cancellable = true)
    private static void onApplyFogColor(CallbackInfo ci) {
        ModuleManager.getModules().stream()
                .filter(m -> m instanceof Ambience && m.isEnabled())
                .findFirst()
                .ifPresent(m -> {
                    int c = Ambience.skyColor.color;
                    float r = ((c >> 16) & 0xFF) / 255f;
                    float g = ((c >> 8) & 0xFF) / 255f;
                    float b = (c & 0xFF) / 255f;

                    // Устанавливаем цвет очистки экрана и тумана напрямую
                    RenderSystem.clearColor(r, g, b, 1.0f);

                    // Отменяем ванильный расчет, чтобы он не перекрасил всё обратно в синий/серый
                    ci.cancel();
                });
    }
}