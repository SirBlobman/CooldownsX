package com.SirBlobman.cooldowns;

import java.util.logging.Logger;

import com.SirBlobman.api.configuration.ConfigManager;
import com.SirBlobman.api.plugin.SirBlobmanPlugin;
import com.SirBlobman.cooldowns.hook.HookMVdWPlaceholderAPI;
import com.SirBlobman.cooldowns.hook.HookPlaceholderAPI;
import com.SirBlobman.cooldowns.listener.ListenerEnderPearlCooldown;
import com.SirBlobman.cooldowns.listener.ListenerGoldenAppleCooldown;
import com.SirBlobman.cooldowns.manager.CooldownManager;
import com.SirBlobman.cooldowns.task.CooldownTimerTask;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;

public final class CooldownPlugin extends SirBlobmanPlugin<CooldownPlugin> {
    private final CooldownManager cooldownManager;
    
    public CooldownPlugin() {
        this.cooldownManager = new CooldownManager(this);
    }
    
    @Override
    public void onLoad() {
        Logger logger = getLogger();
        logger.info("Loading CooldownsX...");
    
        ConfigManager<?> configManager = getConfigManager();
        configManager.saveDefaultConfig("config.yml");
        configManager.saveDefaultConfig("language.yml");
        configManager.saveDefaultConfig("ender_pearl.yml");
        configManager.saveDefaultConfig("golden_apple.yml");
    
        logger.info("Successfully loaded CooldownsX");
    }
    
    @Override
    public void onEnable() {
        Logger logger = getLogger();
        logger.info("Enabling CooldownsX...");
    
        CooldownTimerTask timerTask = new CooldownTimerTask(this);
        timerTask.runTaskTimerAsynchronously(this, 1L, 1L);
        
        registerListener(new ListenerEnderPearlCooldown(this));
        registerListener(new ListenerGoldenAppleCooldown(this));
        registerHooks();
        
        logger.info("Successfully enabled CooldownsX");
    }
    
    @Override
    public void onDisable() {
        Logger logger = getLogger();
        logger.info("Disabling CooldownsX...");
        logger.info("Successfully disabled CooldownsX");
    }
    
    @Override
    public void reloadConfig() {
        ConfigManager<?> configManager = getConfigManager();
        configManager.reloadConfig("config.yml");
        configManager.reloadConfig("language.yml");
        configManager.reloadConfig("ender_pearl.yml");
        configManager.reloadConfig("golden_apple.yml");
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String commandName = command.getName().toLowerCase();
        if(!commandName.equals("cooldownsx")) return false;
        
        String sub = args[0].toLowerCase();
        if(!sub.equals("reload")) return false;
        
        reloadConfig();
        sender.sendMessage("Successfully reloaded the CooldownsX configuration files.");
        return true;
    }
    
    public CooldownManager getCooldownManager() {
        return this.cooldownManager;
    }
    
    private void registerHooks() {
        if(hookInto("MVdWPlaceholderAPI")) {
            HookMVdWPlaceholderAPI hook = new HookMVdWPlaceholderAPI(this);
            hook.register();
        }
        
        if(hookInto("PlaceholderAPI")) {
            HookPlaceholderAPI hook = new HookPlaceholderAPI(this);
            hook.register();
        }
    }
    
    private boolean hookInto(String pluginName) {
        PluginManager manager = Bukkit.getPluginManager();
        if(!manager.isPluginEnabled(pluginName)) return false;
        
        Plugin plugin = manager.getPlugin(pluginName);
        if(plugin == null) return false;
    
        PluginDescriptionFile description = plugin.getDescription();
        String fullName = description.getFullName();
        
        Logger logger = getLogger();
        logger.info("Successfully hooked into plugin '" + fullName + "'.");
        return true;
    }
}