package me.qwider.sundlc;

import me.qwider.sundlc.module.Module;
import me.qwider.sundlc.module.ModuleManager;
import me.qwider.sundlc.render.DiscordRPCManager;
import me.qwider.sundlc.render.ShaderManager;
import me.qwider.sundlc.render.gui.SunClickGui;
import me.qwider.sundlc.render.particle.ParticleRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import org.lwjgl.glfw.GLFW;

public class SunDLC implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // 1. Отключаем варнинги системы (JNA/OSHI), чтобы консоль была чистой
        try {
            org.apache.logging.log4j.core.config.Configurator.setLevel("oshi", org.apache.logging.log4j.Level.OFF);
            org.apache.logging.log4j.core.config.Configurator.setLevel("net.minecraft.util.SystemDetails", org.apache.logging.log4j.Level.OFF);
        } catch (Exception ignored) {}

        // 2. Инициализация базовых систем
        ShaderManager.init();
        ModuleManager.init();
        me.qwider.sundlc.config.ConfigManager.load();
        me.qwider.sundlc.util.AltManager.load();

        // 3. Обработка нажатия Right Shift для открытия GUI
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (GLFW.glfwGetKey(client.getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS) {
                if (client.currentScreen == null) {
                    client.setScreen(new SunClickGui());
                }
            }
        });

        // 4. Обновление логики модулей и Discord RPC (Тики)
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            // Авто-сохранение конфига каждые 30 секунд (600 тиков)
            if (client.player.age % 600 == 0) me.qwider.sundlc.config.ConfigManager.save();

            // Тик каждого модуля
            for (Module m : ModuleManager.getModules()) {
                m.onTick();
            }

            // Логика Discord RPC
            Module rpcMod = ModuleManager.getModules().stream()
                    .filter(m -> m.getName().equalsIgnoreCase("DiscordRPC"))
                    .findFirst().orElse(null);

            if (rpcMod != null && rpcMod.isEnabled()) {
                if (client.world != null) {
                    String server = client.isInSingleplayer() ? "Одиночный мир" :
                            (client.getCurrentServerEntry() != null ? client.getCurrentServerEntry().address : "Сервер");
                    DiscordRPCManager.update("Играет на " + server, "Ник: " + client.getSession().getUsername());
                } else {
                    DiscordRPCManager.update("В главном меню", "Выбирает сервер");
                }
            }
        });

        // 5. Единый блок рендеринга в мире (Render World Last)
        WorldRenderEvents.LAST.register(context -> {
            me.qwider.sundlc.render.particle.ParticleRenderer.render(context.matrixStack(), context.consumers());
            me.qwider.sundlc.render.NameTagsRenderer.render(context.matrixStack(), context.consumers());
            me.qwider.sundlc.render.TargetESPRenderer.render(context.matrixStack());
        });
    }
}