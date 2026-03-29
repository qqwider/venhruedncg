package me.qwider.sundlc.module.modules.visuals;

import me.qwider.sundlc.module.Category;
import me.qwider.sundlc.module.Module;
import me.qwider.sundlc.render.gui.Draggable;

public class Coordinates extends Module {
    // Начальная позиция слева внизу
    public static final Draggable pos = new Draggable("Coordinates", 5, 200, 75, 18);

    public Coordinates() {
        super("Coordinates", Category.OVERLAY);
    }
}