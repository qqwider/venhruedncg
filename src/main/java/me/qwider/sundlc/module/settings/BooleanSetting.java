package me.qwider.sundlc.module.settings;
public class BooleanSetting extends Setting {
    public boolean enabled;
    public BooleanSetting(String name, boolean defaultValue) {
        super(name);
        this.enabled = defaultValue;
    }
}