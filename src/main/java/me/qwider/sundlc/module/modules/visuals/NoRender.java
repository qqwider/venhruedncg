package me.qwider.sundlc.module.modules.visuals;

import me.qwider.sundlc.module.Category;
import me.qwider.sundlc.module.Module;
import me.qwider.sundlc.module.settings.BooleanSetting;

public class NoRender extends Module {
    public static BooleanSetting clouds = new BooleanSetting("Clouds", true);
    public static BooleanSetting weather = new BooleanSetting("Weather", false);
    public static BooleanSetting fire = new BooleanSetting("Fire Overlay", true);
    public static BooleanSetting scoreboard = new BooleanSetting("Scoreboard", false);

    public NoRender() {
        super("NoRender", Category.VISUALS);
        // Убрали overlays из списка настроек
        addSettings(clouds, weather, fire, scoreboard);
    }
}