package me.qwider.sundlc.mixin;

import me.qwider.sundlc.module.ModuleManager;
import me.qwider.sundlc.module.modules.visuals.NameTags;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin {
    @Inject(method = "hasLabel", at = @At("HEAD"), cancellable = true)
    private void onHasLabel(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        // Если это игрок и включен модуль NameTags
        if (entity instanceof PlayerEntity && ModuleManager.getModules().stream().anyMatch(m -> m instanceof NameTags && m.isEnabled())) {
            // Если это МЫ и мы в F5 — всё равно возвращаем false (скрываем ванильный ник)
            // Чтобы работал наш кастомный рендер
            cir.setReturnValue(false);
        }
    }
}