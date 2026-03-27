package me.qwider.sundlc.mixin;

import me.qwider.sundlc.module.modules.visuals.Potions;
import me.qwider.sundlc.module.modules.visuals.TargetHUD;
import me.qwider.sundlc.render.SunHUD; // ИСПРАВЛЕННЫЙ ИМПОРТ
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatScreen.class)
public class MixinChat {

    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        TargetHUD.pos.update(mouseX, mouseY);
        Potions.pos.update(mouseX, mouseY);
        // Теперь вызываем метод из SunHUD
        SunHUD.renderTargetHUD(context);
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void onClick(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        TargetHUD.pos.onMouseClick((int) mouseX, (int) mouseY, button);
        Potions.pos.onMouseClick((int) mouseX, (int) mouseY, button);
    }
}