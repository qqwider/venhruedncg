package me.qwider.sundlc.mixin;

import net.minecraft.client.gui.hud.PlayerListHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PlayerListHud.class)
public interface PlayerListHudAccessor {
    // Даем доступ к приватному полю 'visible'
    @Accessor("visible")
    boolean isVisible();
}