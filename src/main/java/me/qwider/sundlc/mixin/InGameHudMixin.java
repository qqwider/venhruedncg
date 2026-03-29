package me.qwider.sundlc.mixin;

import me.qwider.sundlc.module.ModuleManager;
import me.qwider.sundlc.module.modules.visuals.NoRender;
import me.qwider.sundlc.render.SunHUD;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {
    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        SunHUD.render(context);
    }
    @Inject(method = "renderScoreboardSidebar*", at = @At("HEAD"), cancellable = true)
    private void onRenderScoreboard(DrawContext context, net.minecraft.scoreboard.ScoreboardObjective objective, CallbackInfo ci) {
        if (ModuleManager.getModules().stream().anyMatch(m -> m instanceof NoRender && m.isEnabled()) && NoRender.scoreboard.enabled) {
            ci.cancel();
        }
    }
}