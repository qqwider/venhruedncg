package me.qwider.sundlc.module;

import me.qwider.sundlc.render.Animation;
import net.minecraft.client.MinecraftClient;

public abstract class Module {
    protected static final MinecraftClient mc = MinecraftClient.getInstance();

    private final String name;
    private final Category category;
    private boolean enabled;
    public final Animation animation = new Animation(8.0);

    public Module(String name, Category category) {
        this.name = name;
        this.category = category;
    }

    // Метод для обновления логики (будет переопределен в TargetHUD)
    public void onTick() {}

    public void toggle() {
        setEnabled(!enabled);
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (enabled) onEnable(); else onDisable();
        me.qwider.sundlc.config.ConfigManager.save(); // Сохраняем состояние сразу
    }

    public void onEnable() {}
    public void onDisable() {}

    public boolean isEnabled() { return enabled; }
    public String getName() { return name; }
    public Category getCategory() { return category; }
}