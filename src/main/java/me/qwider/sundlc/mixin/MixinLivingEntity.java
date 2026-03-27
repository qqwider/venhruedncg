package me.qwider.sundlc.mixin;

import me.qwider.sundlc.module.modules.visuals.TargetHUD;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity {
    // Этот миксин больше не нужен, так как используем отслеживание здоровья в onTick
}