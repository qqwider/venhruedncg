package me.qwider.sundlc.module.modules.visuals;

import me.qwider.sundlc.module.Category;
import me.qwider.sundlc.module.Module;
import me.qwider.sundlc.render.gui.Draggable;

public class Potions extends Module {
    public static final Draggable pos = new Draggable("Potions", 100, 45, 100, 50);
    
    public Potions() {
        super("Potions", Category.OVERLAY);
        setEnabled(true); // Включен по умолчанию
    }
}