package me.qwider.sundlc.module;

import me.qwider.sundlc.module.modules.visuals.ArrayListModule;
import me.qwider.sundlc.module.modules.visuals.TargetHUD;
import java.util.ArrayList;
import java.util.List;

public class ModuleManager {
    private static final List<Module> modules = new ArrayList<>();

    public static void init() {
        add(new TargetHUD());
        add(new ArrayListModule());

        add(new Module("ClickGUI", Category.GLOBALS) {});
        add(new Module("ESP", Category.VISUALS) {});
    }

    private static void add(Module m) { modules.add(m); }
    public static List<Module> getModules() { return modules; }
    public static List<Module> getByCategory(Category c) {
        return modules.stream().filter(m -> m.getCategory() == c).toList();
    }
}