package me.qwider.sundlc.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.session.Session;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class AltManager {
    public static final List<AltAccount> accounts = new ArrayList<>();
    private static final File FILE = new File(MinecraftClient.getInstance().runDirectory, "sundlc/alts.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void save() {
        try {
            if (!FILE.getParentFile().exists()) FILE.getParentFile().mkdirs();
            JsonObject json = new JsonObject();
            JsonArray array = new JsonArray();
            for (AltAccount acc : accounts) array.add(acc.name());
            json.add("accounts", array);

            // Сохраняем ник текущей сессии
            json.addProperty("lastNickname", MinecraftClient.getInstance().getSession().getUsername());

            Files.writeString(FILE.toPath(), GSON.toJson(json));
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void load() {
        if (!FILE.exists()) return;
        try {
            JsonObject json = GSON.fromJson(Files.readString(FILE.toPath()), JsonObject.class);
            accounts.clear();
            if (json.has("accounts")) {
                json.getAsJsonArray("accounts").forEach(e -> accounts.add(new AltAccount(e.getAsString(), null, null, "legacy")));
            }

            // АВТОВЫБОР: Если в конфиге есть последний ник — заходим под ним сразу
            if (json.has("lastNickname")) {
                login(json.get("lastNickname").getAsString());
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    // Универсальный метод логина (теперь доступен отовсюду)
    public static void login(String name) {
        if (name == null || name.isEmpty()) return;
        UUID u = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes());
        SessionChanger.setSession(new Session(name, u, "0", Optional.empty(), Optional.empty(), Session.AccountType.LEGACY));
    }
}