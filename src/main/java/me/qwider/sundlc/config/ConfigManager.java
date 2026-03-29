package me.qwider.sundlc.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import me.qwider.sundlc.module.Module;
import me.qwider.sundlc.module.ModuleManager;
import me.qwider.sundlc.module.modules.visuals.TargetHUD;
import me.qwider.sundlc.module.modules.visuals.Potions;
import me.qwider.sundlc.module.settings.*;
import me.qwider.sundlc.render.gui.Panel;
import me.qwider.sundlc.render.gui.SunClickGui;
import net.minecraft.client.MinecraftClient;

import java.io.File;
import java.nio.file.Files;

public class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File FILE = new File(MinecraftClient.getInstance().runDirectory, "sundlc/config.json");

    public static void save() {
        try {
            if (!FILE.getParentFile().exists()) FILE.getParentFile().mkdirs();
            JsonObject json = new JsonObject();

            // 1. Сохраняем модули и их настройки
            JsonObject modulesJson = new JsonObject();
            for (Module m : ModuleManager.getModules()) {
                JsonObject mJson = new JsonObject();
                mJson.addProperty("enabled", m.isEnabled());

                // СОХРАНЕНИЕ НАСТРОЕК
                if (!m.settings.isEmpty()) {
                    JsonObject settingsJson = new JsonObject();
                    for (Setting s : m.settings) {
                        if (s instanceof BooleanSetting bs) settingsJson.addProperty(s.name, bs.enabled);
                        if (s instanceof NumberSetting ns) settingsJson.addProperty(s.name, ns.value);
                        if (s instanceof ColorSetting cs) settingsJson.addProperty(s.name, cs.color);
                        if (s instanceof ModeSetting ms) settingsJson.addProperty(s.name, ms.index);
                    }
                    mJson.add("settings", settingsJson);
                }

                modulesJson.add(m.getName(), mJson);
            }
            json.add("modules", modulesJson);

            // 2. Сохраняем позиции
            JsonObject posJson = new JsonObject();
            posJson.addProperty("targethud_x", TargetHUD.pos.x);
            posJson.addProperty("targethud_y", TargetHUD.pos.y);
            posJson.addProperty("potions_x", Potions.pos.x);
            posJson.addProperty("potions_y", Potions.pos.y);
            // Добавим координаты, если они есть
            try {
                posJson.addProperty("coords_x", me.qwider.sundlc.module.modules.visuals.Coordinates.pos.x);
                posJson.addProperty("coords_y", me.qwider.sundlc.module.modules.visuals.Coordinates.pos.y);
            } catch (Exception ignored) {}

            for (Panel p : SunClickGui.getPanels()) {
                JsonObject pPos = new JsonObject();
                pPos.addProperty("x", p.drag.x);
                pPos.addProperty("y", p.drag.y);
                pPos.addProperty("open", p.isOpen());
                posJson.add("panel_" + p.category.name(), pPos);
            }
            json.add("positions", posJson);

            Files.writeString(FILE.toPath(), GSON.toJson(json));
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void load() {
        if (!FILE.exists()) return;
        try {
            JsonObject json = GSON.fromJson(Files.readString(FILE.toPath()), JsonObject.class);

            if (json.has("modules")) {
                JsonObject mods = json.getAsJsonObject("modules");
                for (Module m : ModuleManager.getModules()) {
                    if (mods.has(m.getName())) {
                        JsonObject mJson = mods.getAsJsonObject(m.getName());
                        m.setEnabled(mJson.get("enabled").getAsBoolean());

                        // ЗАГРУЗКА НАСТРОЕК
                        if (mJson.has("settings")) {
                            JsonObject sJson = mJson.getAsJsonObject("settings");
                            for (Setting s : m.settings) {
                                if (sJson.has(s.name)) {
                                    if (s instanceof BooleanSetting bs) bs.enabled = sJson.get(s.name).getAsBoolean();
                                    if (s instanceof NumberSetting ns) ns.value = sJson.get(s.name).getAsDouble();
                                    if (s instanceof ModeSetting ms) ms.index = sJson.get(s.name).getAsInt();
                                    if (s instanceof ColorSetting cs) {
                                        cs.color = sJson.get(s.name).getAsInt();
                                        cs.updateHSB();
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (json.has("positions")) {
                JsonObject pos = json.getAsJsonObject("positions");
                if (pos.has("targethud_x")) TargetHUD.pos.x = pos.get("targethud_x").getAsFloat();
                if (pos.has("targethud_y")) TargetHUD.pos.y = pos.get("targethud_y").getAsFloat();
                if (pos.has("potions_x")) Potions.pos.x = pos.get("potions_x").getAsFloat();
                if (pos.has("potions_y")) Potions.pos.y = pos.get("potions_y").getAsFloat();
                if (pos.has("coords_x")) me.qwider.sundlc.module.modules.visuals.Coordinates.pos.x = pos.get("coords_x").getAsFloat();
                if (pos.has("coords_y")) me.qwider.sundlc.module.modules.visuals.Coordinates.pos.y = pos.get("coords_y").getAsFloat();

                for (Panel p : SunClickGui.getPanels()) {
                    String key = "panel_" + p.category.name();
                    if (pos.has(key)) {
                        JsonObject pPos = pos.getAsJsonObject(key);
                        p.drag.x = pPos.get("x").getAsFloat();
                        p.drag.y = pPos.get("y").getAsFloat();
                        p.setOpen(pPos.get("open").getAsBoolean());
                    }
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
}