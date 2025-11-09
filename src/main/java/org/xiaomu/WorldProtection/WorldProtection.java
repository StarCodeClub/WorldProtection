package org.xiaomu.WorldProtection;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class WorldProtection extends JavaPlugin {
    private static WorldProtection instance;

    @Override
    public void onEnable() {
        instance = this;
        
        this.saveDefaultConfig();
        this.reloadConfig();

        if (Bukkit.getPluginCommand("wp") != null) {
            Bukkit.getPluginCommand("wp").setExecutor(new Commander());
            Bukkit.getPluginCommand("wp").setTabCompleter(new WPTabCompleter());
        }

        Bukkit.getPluginManager().registerEvents(new Protector(), this);

        this.getLogger().info("插件已加载");
    }

    @Override
    public void onDisable() {
        this.getLogger().info("插件已卸载");
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();
        Protector.reloadConfigs();
    }

    public static WorldProtection getInstance() {
        return instance;
    }
}
