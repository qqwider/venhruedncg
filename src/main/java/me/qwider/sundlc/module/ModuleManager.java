package me.qwider.sundlc.module;

import me.qwider.sundlc.module.modules.visuals.NameTags;
import me.qwider.sundlc.module.modules.visuals.Potions;
import me.qwider.sundlc.module.modules.visuals.TargetHUD;
import java.util.ArrayList;
import java.util.List;

public class ModuleManager {
    private static final List<Module> modules = new ArrayList<>();

    public static void init() {
        add(new TargetHUD());
        add(new NameTags());
        add(new Potions());

    }

    private static void add(Module m) { modules.add(m); }
    public static List<Module> getModules() { return modules; }
    public static List<Module> getByCategory(Category c) {
        return modules.stream().filter(m -> m.getCategory() == c).toList();
    }
}