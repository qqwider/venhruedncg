package me.qwider.sundlc.module.modules.visuals;

import me.qwider.sundlc.module.Category;
import me.qwider.sundlc.module.Module;
import me.qwider.sundlc.module.settings.*;

public class Particles extends Module {
    public static ModeSetting mode = new ModeSetting("Type", "STAR", "STAR", "HEART", "SNOW");
    public static NumberSetting amount = new NumberSetting("Amount", 10, 500, 100);
    public static ColorSetting color = new ColorSetting("Color", new java.awt.Color(255, 255, 255));

    public Particles() {
        super("Particles", Category.VISUALS);
        addSettings(mode, amount, color);
    }

    // Спавн партиклов теперь полностью в ParticleRenderer
    @Override
    public void onTick() {
        // Ничего не делаем, ParticleRenderer сам управляет спавном
    }
}