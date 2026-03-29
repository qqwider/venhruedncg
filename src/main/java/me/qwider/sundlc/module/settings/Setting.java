package me.qwider.sundlc.module.settings;

import java.util.function.Supplier;

public abstract class Setting {
    public final String name;
    public Supplier<Boolean> visibility; // Условие видимости
    
    public Setting(String name) { 
        this.name = name;
        this.visibility = () -> true; // По умолчанию всегда видна
    }
    
    public Setting setVisibility(Supplier<Boolean> visibility) {
        this.visibility = visibility;
        return this;
    }
    
    public boolean isVisible() {
        return visibility.get();
    }
}