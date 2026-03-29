package me.qwider.sundlc.module.modules.other;

import me.qwider.sundlc.module.Category;
import me.qwider.sundlc.module.Module;
import me.qwider.sundlc.render.DiscordRPCManager;

public class DiscordRPC extends Module {
    public DiscordRPC() {
        super("DiscordRPC", Category.GLOBALS);
    }

    @Override
    public void onEnable() {
        DiscordRPCManager.start();
    }

    @Override
    public void onDisable() {
        DiscordRPCManager.stop();
    }
}