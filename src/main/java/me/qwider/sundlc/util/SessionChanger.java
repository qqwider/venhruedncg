package me.qwider.sundlc.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.session.Session;
import java.lang.reflect.Field;

public class SessionChanger {
    public static void setSession(Session session) {
        try {
            Field sessionField = MinecraftClient.class.getDeclaredField("session");
            sessionField.setAccessible(true);
            sessionField.set(MinecraftClient.getInstance(), session);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}