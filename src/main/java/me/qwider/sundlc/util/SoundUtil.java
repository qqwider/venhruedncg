package me.qwider.sundlc.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class SoundUtil {
    // Регистрация идентификаторов
    private static final Identifier ENABLE_ID = Identifier.of("sundlc", "module_enable");
    private static final Identifier DISABLE_ID = Identifier.of("sundlc", "module_disable");
    private static final Identifier CLICK_ID = Identifier.of("sundlc", "ui_click");

    // Создание событий
    public static final SoundEvent ENABLE_EVENT = SoundEvent.of(ENABLE_ID);
    public static final SoundEvent DISABLE_EVENT = SoundEvent.of(DISABLE_ID);
    public static final SoundEvent CLICK_EVENT = SoundEvent.of(CLICK_ID);

    public static void playEnable() {
        playSound(ENABLE_EVENT, 1.0f);
    }

    public static void playDisable() {
        playSound(DISABLE_EVENT, 1.0f);
    }

    public static void playClick() {
        playSound(CLICK_EVENT, 1.0f);
    }

    private static void playSound(SoundEvent event, float pitch) {
        MinecraftClient client = MinecraftClient.getInstance();

        // Проверяем, готов ли клиент и звуковой менеджер
        if (client == null || client.getSoundManager() == null) {
            return;
        }

        // Дополнительно проверяем, находится ли игрок в мире,
        // чтобы звуки не играли при загрузке конфигов в меню
        if (client.world == null) {
            return;
        }

        client.getSoundManager().play(
                PositionedSoundInstance.master(event, pitch)
        );
    }
}