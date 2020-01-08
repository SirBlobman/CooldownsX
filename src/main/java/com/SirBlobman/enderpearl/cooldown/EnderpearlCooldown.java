package com.SirBlobman.enderpearl.cooldown;

import java.util.List;
import java.util.logging.Logger;

import com.SirBlobman.enderpearl.cooldown.hook.HookMVdWPlaceholderAPI;
import com.SirBlobman.enderpearl.cooldown.hook.HookPlaceholderAPI;
import com.SirBlobman.enderpearl.cooldown.listener.ListenerEnderPearlCooldown;
import com.SirBlobman.enderpearl.cooldown.task.EnderpearlCooldownTask;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class EnderpearlCooldown extends JavaPlugin {
    @Override
    public void onEnable() {
        saveDefaultConfig();

        PluginManager manager = Bukkit.getPluginManager();
        Logger logger = getLogger();

        EnderpearlCooldownTask cooldownTask = new EnderpearlCooldownTask(this);
        cooldownTask.runTaskTimerAsynchronously(this, 0L, 1L);

        Listener listener = new ListenerEnderPearlCooldown(this);
        manager.registerEvents(listener, this);

        hookIntoMVdWPlaceholderAPI(logger);
        hookIntoPlaceholderAPI(logger);
    }

    @Override
    public void onDisable() {

    }

    public String getConfigMessage(String path) {
        if(path == null || path.isEmpty()) return "";

        FileConfiguration config = getConfig();
        if(config == null) return path;

        if(config.isList(path)) {
            List<String> stringList = config.getStringList(path);
            String string = String.join("\n", stringList);
            return ChatColor.translateAlternateColorCodes('&', string);
        }

        if(config.isString(path)) {
            String string = config.getString(path);
            return ChatColor.translateAlternateColorCodes('&', string == null ? path : string);
        }

        return path;
    }

    private void hookIntoMVdWPlaceholderAPI(Logger logger) {
        PluginManager manager = Bukkit.getPluginManager();
        if(!manager.isPluginEnabled("MVdWPlaceholderAPI")) return;

        Plugin plugin = manager.getPlugin("MVdWPlaceholderAPI");
        if(plugin == null) return;

        HookMVdWPlaceholderAPI hook = new HookMVdWPlaceholderAPI(this);
        hook.register();

        PluginDescriptionFile description = plugin.getDescription();
        String version = description.getVersion();
        logger.info("Successfully hooked into MVdWPlaceholderAPI v" + version);
    }

    private void hookIntoPlaceholderAPI(Logger logger) {
        PluginManager manager = Bukkit.getPluginManager();
        if(!manager.isPluginEnabled("PlaceholderAPI")) return;

        Plugin plugin = manager.getPlugin("PlaceholderAPI");
        if(plugin == null) return;

        HookPlaceholderAPI hook = new HookPlaceholderAPI(this);
        hook.register();

        PluginDescriptionFile description = plugin.getDescription();
        String version = description.getVersion();
        logger.info("Successfully hooked into PlaceholderAPI v" + version);
    }
}