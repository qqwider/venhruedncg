package me.qwider.sundlc.render;

import club.minnced.discord.rpc.DiscordEventHandlers;
import club.minnced.discord.rpc.DiscordRPC;
import club.minnced.discord.rpc.DiscordRichPresence;
import java.util.HashMap;
import java.util.Map;

public class DiscordRPCManager {
    private static final DiscordRPC lib = DiscordRPC.INSTANCE;
    private static final DiscordRichPresence presence = new DiscordRichPresence();
    private static final String APP_ID = "1487418585864470668"; // Вставь свой ID

    // Список красивых имен серверов
    private static final Map<String, String> SERVER_NAMES = new HashMap<>();
    static {
        SERVER_NAMES.put("funtime.su", "FunTime");
        SERVER_NAMES.put("reallyworld.ru", "ReallyWorld");
        SERVER_NAMES.put("holyworld.ru", "HolyWorld");
        SERVER_NAMES.put("x35.joinserver.xyz:25570", "DomikLand");
        SERVER_NAMES.put("mstnetwork.ru", "MstNetwork");
    }

    public static void start() {
        DiscordEventHandlers handlers = new DiscordEventHandlers();
        lib.Discord_Initialize(APP_ID, handlers, true, null);

        presence.startTimestamp = System.currentTimeMillis() / 1000L;
        presence.largeImageKey = "logo_large";
        presence.largeImageText = "SunDLC Client | v1.21.1";
        presence.smallImageKey = "logo_small";

        // Поток нужен только для обработки системных ответов (callbacks)
        new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                lib.Discord_RunCallbacks();
                try { Thread.sleep(2000); } catch (InterruptedException e) { break; }
            }
        }, "RPC-Callback-Handler").start();
    }

    public static void stop() {
        lib.Discord_Shutdown();
    }

    // ТОТ САМЫЙ МЕТОД, КОТОРЫЙ ИСКАЛА JAVA
    public static void update(String details, String state) {
        // Умная проверка: если в строке details (где сервер) есть знакомый IP - меняем его
        String finalDetails = details;
        for (String ip : SERVER_NAMES.keySet()) {
            if (details.toLowerCase().contains(ip)) {
                finalDetails = "Играет на " + SERVER_NAMES.get(ip);
                break;
            }
        }

        presence.details = finalDetails;
        presence.state = state;
        lib.Discord_UpdatePresence(presence);
    }
}