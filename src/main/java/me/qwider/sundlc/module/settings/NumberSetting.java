package me.qwider.sundlc.module.settings;
public class NumberSetting extends Setting {
    public double min, max, value;
    public boolean dragging;
    public boolean isInteger; // Флаг для целых чисел
    
    public NumberSetting(String name, double min, double max, double defaultValue) {
        super(name);
        this.min = min; 
        this.max = max; 
        this.value = defaultValue;
        this.isInteger = false;
    }
    
    public NumberSetting setInteger(boolean isInteger) {
        this.isInteger = isInteger;
        if (isInteger) {
            this.value = Math.round(this.value);
        }
        return this;
    }
}