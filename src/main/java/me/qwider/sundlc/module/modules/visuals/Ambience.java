package me.qwider.sundlc.module.modules.visuals;

import me.qwider.sundlc.module.Category;
import me.qwider.sundlc.module.Module;
import me.qwider.sundlc.module.settings.ColorSetting;
import java.awt.Color;

public class Ambience extends Module {
    public static ColorSetting skyColor = new ColorSetting("Sky Color", new Color(136, 119, 255));

    public Ambience() {
        super("Ambience", Category.VISUALS);
        addSettings(skyColor);
    }
}