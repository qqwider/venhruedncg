package me.qwider.sundlc.module;

import me.qwider.sundlc.module.modules.other.DiscordRPC;
import me.qwider.sundlc.module.modules.visuals.*;

import java.util.ArrayList;
import java.util.List;

public class ModuleManager {
    private static final List<Module> modules = new ArrayList<>();

    public static void init() {
        add(new TargetHUD());
        add(new NameTags());
        add(new Potions());
        add(new NoHurtCam());
        add(new Coordinates());
        add(new DiscordRPC());
        add(new Particles());
        add(new Ambience());
        add(new NoRender());
        add(new TargetESP());
        // add(new HitParticles()); // Временно отключен
    }

    private static void add(Module m) { modules.add(m); }
    public static List<Module> getModules() { return modules; }
    public static List<Module> getByCategory(Category c) {
        return modules.stream().filter(m -> m.getCategory() == c).toList();
    }
    
    public static Module getModule(String name) {
        return modules.stream().filter(m -> m.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }
}