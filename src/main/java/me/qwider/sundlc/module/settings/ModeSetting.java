package me.qwider.sundlc.module.settings;

import me.qwider.sundlc.render.Animation;
import java.util.Arrays;
import java.util.List;

public class ModeSetting extends Setting {
    public final List<String> modes;
    public int index;
    public boolean opened; // Для выпадающего списка
    public final Animation openAnim = new Animation(10.0);

    public ModeSetting(String name, String defaultMode, String... modes) {
        super(name);
        this.modes = Arrays.asList(modes);
        this.index = this.modes.indexOf(defaultMode);
    }

    public String getMode() {
        // Если индекс из конфига больше, чем текущее количество режимов, сбрасываем на 0
        if (index >= modes.size() || index < 0) {
            index = 0;
        }
        return modes.get(index);
    }
    
    public void setMode(int newIndex) {
        if (this.index != newIndex) {
            this.index = newIndex;
            this.opened = false; // Закрываем выпадающий список при изменении
        }
    }

    public void next() {
        index = (index + 1) % modes.size();
    }
}