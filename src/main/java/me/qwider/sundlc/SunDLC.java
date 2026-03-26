package me.qwider.sundlc;

import me.qwider.sundlc.module.Module;
import me.qwider.sundlc.module.ModuleManager;
import me.qwider.sundlc.render.MSDFRenderer;
import me.qwider.sundlc.render.ShaderManager;
import me.qwider.sundlc.render.gui.SunClickGui;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class SunDLC implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Регистрируем только шейдеры, они загружаются позже сами
        ShaderManager.init();
        ModuleManager.init();
        me.qwider.sundlc.config.ConfigManager.load();
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (GLFW.glfwGetKey(client.getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS) {
                if (client.currentScreen == null) {
                    client.setScreen(new SunClickGui());
                }
                break;
            }
        });
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;
            if (client.player.age % 600 == 0) me.qwider.sundlc.config.ConfigManager.save();
            for (Module m : ModuleManager.getModules()) {
                m.onTick();
            }
        });
    }
}