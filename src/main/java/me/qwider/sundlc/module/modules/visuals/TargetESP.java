package me.qwider.sundlc.module.modules.visuals;

import me.qwider.sundlc.module.Category;
import me.qwider.sundlc.module.Module;
import me.qwider.sundlc.module.settings.*;
import java.awt.Color;

public class TargetESP extends Module {
    public static ModeSetting mode = new ModeSetting("Mode", "Ghosts", "Ghosts", "Cubes", "Marker");
    public static ColorSetting color = new ColorSetting("Color", new Color(136, 119, 255));

    // Настройки для режима Ghosts (уникальные имена)
    public static NumberSetting ghostsSpeed = new NumberSetting("G-Speed", 0.01, 0.5, 0.15);
    public static NumberSetting ghostsSegments = new NumberSetting("G-Segments", 40, 200, 120);
    public static NumberSetting ghostsRadius = new NumberSetting("G-Radius", 0.3, 2.0, 0.8);

    // Настройки для режима Cubes (уникальные имена)
    public static NumberSetting cubesSpeed = new NumberSetting("C-Speed", 1, 50, 15);
    public static NumberSetting cubesSize = new NumberSetting("C-Size", 0.01, 0.5, 0.15);
    public static NumberSetting cubesCount = new NumberSetting("C-Count", 3, 12, 6);

    // Настройки для режима Marker (уникальные имена)
    public static NumberSetting markerSpeed = new NumberSetting("M-Speed", 0.5, 15, 4);
    public static NumberSetting markerSize = new NumberSetting("M-Size", 0.1, 2.0, 0.75);

    public TargetESP() {
        super("TargetESP", Category.VISUALS);

        // Делаем Count и Segments целыми числами
        ghostsSegments.setInteger(true);
        cubesCount.setInteger(true);

        // Настройки Ghosts видны только когда выбран режим Ghosts
        ghostsSpeed.setVisibility(() -> mode.getMode().equals("Ghosts"));
        ghostsSegments.setVisibility(() -> mode.getMode().equals("Ghosts"));
        ghostsRadius.setVisibility(() -> mode.getMode().equals("Ghosts"));

        // Настройки Cubes видны только когда выбран режим Cubes
        cubesSpeed.setVisibility(() -> mode.getMode().equals("Cubes"));
        cubesSize.setVisibility(() -> mode.getMode().equals("Cubes"));
        cubesCount.setVisibility(() -> mode.getMode().equals("Cubes"));

        // Настройки Marker видны только когда выбран режим Marker
        markerSpeed.setVisibility(() -> mode.getMode().equals("Marker"));
        markerSize.setVisibility(() -> mode.getMode().equals("Marker"));

        addSettings(mode, color,
            ghostsSpeed, ghostsSegments, ghostsRadius,
            cubesSpeed, cubesSize, cubesCount,
            markerSpeed, markerSize);
    }
}