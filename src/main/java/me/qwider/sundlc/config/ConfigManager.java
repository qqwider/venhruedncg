package me.qwider.sundlc.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import me.qwider.sundlc.module.Module;
import me.qwider.sundlc.module.ModuleManager;
import me.qwider.sundlc.module.modules.visuals.TargetHUD;
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

            // 1. Сохраняем модули
            JsonObject modulesJson = new JsonObject();
            for (Module m : ModuleManager.getModules()) {
                JsonObject mJson = new JsonObject();
                mJson.addProperty("enabled", m.isEnabled());
                modulesJson.add(m.getName(), mJson);
            }
            json.add("modules", modulesJson);

            // 2. Сохраняем позиции (TargetHUD и Панели GUI)
            JsonObject posJson = new JsonObject();
            posJson.addProperty("targethud_x", TargetHUD.pos.x);
            posJson.addProperty("targethud_y", TargetHUD.pos.y);

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
                        m.setEnabled(mods.getAsJsonObject(m.getName()).get("enabled").getAsBoolean());
                    }
                }
            }

            if (json.has("positions")) {
                JsonObject pos = json.getAsJsonObject("positions");
                if (pos.has("targethud_x")) TargetHUD.pos.x = pos.get("targethud_x").getAsFloat();
                if (pos.has("targethud_y")) TargetHUD.pos.y = pos.get("targethud_y").getAsFloat();

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