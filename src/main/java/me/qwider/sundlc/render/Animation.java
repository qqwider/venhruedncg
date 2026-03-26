package me.qwider.sundlc.render;

public class Animation {
    private double value;
    private long lastMS;
    private final double speed;

    public Animation(double speed) {
        this.speed = speed;
        this.lastMS = System.currentTimeMillis();
    }

    public void update(boolean state) {
        long currentMS = System.currentTimeMillis();
        long delta = currentMS - lastMS;

        // Если дельта слишком большая (например, первый запуск или лаг),
        // ограничиваем её, чтобы не было мгновенного прыжка
        if (delta > 100) {
            delta = 16;
            lastMS = currentMS - 16; // Корректируем lastMS
        }

        lastMS = currentMS;

        double step = delta * (speed / 1000.0);

        if (state) {
            value = Math.min(1.0, value + step);
        } else {
            value = Math.max(0.0, value - step);
        }
    }

    public double getValue() {
        // Easing: Quadratic Out (Делает анимацию "сочной" в конце)
        return value * (2 - value);
    }

    // Позволяет получить "сырое" значение без сглаживания
    public double getRawValue() { return value; }
}