package me.qwider.sundlc.render.gui;

import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

public class Draggable {
    public float x, y, width, height;
    private float dragX, dragY;
    private boolean dragging;

    public String id;
    public Draggable(String id, float x, float y, float width, float height) {
        this.id = id;
        this.x = x; this.y = y; this.width = width; this.height = height;
    }

    public void onMouseClick(int mouseX, int mouseY, int button) {
        if (button == 0 && isHovered(mouseX, mouseY, x, y, width, height)) {
            dragging = true;
            dragX = x - mouseX;
            dragY = y - mouseY;
        }
    }

    public void onMouseRelease(int button) {
        if (button == 0) dragging = false;
    }

    public void update(int mouseX, int mouseY) {
        // Проверка: если левая кнопка мыши НЕ зажата, прекращаем перетаскивание
        if (org.lwjgl.glfw.GLFW.glfwGetMouseButton(MinecraftClient.getInstance().getWindow().getHandle(), 0) != 1) {
            dragging = false;
        }

        if (dragging) {
            x = mouseX + dragX;
            y = mouseY + dragY;
        }
    }

    private boolean isHovered(int mx, int my, float x, float y, float w, float h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }
}