package me.qwider.sundlc.mixin;

import me.qwider.sundlc.module.modules.visuals.TargetHUD; // ПРОВЕРЬ ПУТЬ
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity {
    @Inject(method = "damage", at = @At("HEAD"))
    private void onDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (TargetHUD.target != null && TargetHUD.target.equals(entity)) {
            TargetHUD.damageAnim = 1.0f;
        }
    }
}