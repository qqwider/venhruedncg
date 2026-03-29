package me.qwider.sundlc.mixin;

import me.qwider.sundlc.module.ModuleManager;
import me.qwider.sundlc.module.modules.visuals.Ambience;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientWorld.class)
public class MixinClientWorld {
    @Inject(method = "getSkyColor", at = @At("HEAD"), cancellable = true)
    private void onGetSkyColor(Vec3d cameraPos, float tickDelta, CallbackInfoReturnable<Vec3d> cir) {
        ModuleManager.getModules().stream()
                .filter(m -> m instanceof Ambience && m.isEnabled())
                .findFirst()
                .ifPresent(m -> {
                    int c = Ambience.skyColor.color;
                    double r = ((c >> 16) & 0xFF) / 255.0;
                    double g = ((c >> 8) & 0xFF) / 255.0;
                    double b = (c & 0xFF) / 255.0;
                    cir.setReturnValue(new Vec3d(r, g, b));
                });
    }
}