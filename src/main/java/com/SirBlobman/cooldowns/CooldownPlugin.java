package com.SirBlobman.cooldowns;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import com.SirBlobman.api.configuration.ConfigurationManager;
import com.SirBlobman.api.nms.MultiVersionHandler;
import com.SirBlobman.api.update.UpdateChecker;
import com.SirBlobman.api.utility.VersionUtility;
import com.SirBlobman.cooldowns.listener.ListenerConsume;
import com.SirBlobman.cooldowns.listener.ListenerInteract;
import com.SirBlobman.cooldowns.manager.CooldownManager;
import com.SirBlobman.cooldowns.task.ActionBarTask;
import com.SirBlobman.core.CorePlugin;

public final class CooldownPlugin extends JavaPlugin {
    private final ConfigurationManager configurationManager;
    private final CooldownManager cooldownManager;
    public CooldownPlugin() {
        this.configurationManager = new ConfigurationManager(this);
        this.cooldownManager = new CooldownManager(this);
    }

    @Override
    public void onLoad() {
        ConfigurationManager configurationManager = getConfigurationManager();
        configurationManager.saveDefault("config.yml");
        configurationManager.saveDefault("cooldowns.yml");
    }

    @Override
    public void onEnable() {
        int minorVersion = VersionUtility.getMinorVersion();
        if(minorVersion < 13) {
            Logger logger = getLogger();
            logger.warning("This plugin requires version 1.13.2 or above!");
            setEnabled(false);
            return;
        }

        reloadConfig();
        new ListenerConsume(this).register();
        new ListenerInteract(this).register();

        UpdateChecker updateChecker = new UpdateChecker(this, 41981L);
        updateChecker.runCheck();
    }

    @Override
    public void onDisable() {
        // Do Nothing
    }

    @Override
    @NotNull
    public YamlConfiguration getConfig() {
        ConfigurationManager configurationManager = getConfigurationManager();
        return configurationManager.get("config.yml");
    }

    @Override
    public void reloadConfig() {
        ConfigurationManager configurationManager = getConfigurationManager();
        configurationManager.reload("config.yml");
        configurationManager.reload("cooldowns.yml");

        CooldownManager cooldownManager = getCooldownManager();
        cooldownManager.loadDefaultCooldowns();

        BukkitScheduler scheduler = Bukkit.getScheduler();
        scheduler.cancelTasks(this);

        YamlConfiguration configuration = configurationManager.get("config.yml");
        if(configuration.getBoolean("use-action-bar")) {
            ActionBarTask actionBarTask = new ActionBarTask(this);
            actionBarTask.start();
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, Command command, @NotNull String label, String[] args) {
        String commandName = command.getName().toLowerCase();
        return (commandName.equals("cooldownsx") ? Collections.emptyList() : super.onTabComplete(sender, command, label, args));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, Command command, @NotNull String label, String[] args) {
        String commandName = command.getName().toLowerCase();
        if(!commandName.equals("cooldownsx")) return super.onCommand(sender, command, label, args);

        reloadConfig();
        sender.sendMessage(ChatColor.GREEN + "Successfully reloaded the configuration files for CooldownsX.");
        return true;
    }

    public MultiVersionHandler getMultiVersionHandler() {
        CorePlugin plugin = JavaPlugin.getPlugin(CorePlugin.class);
        return plugin.getMultiVersionHandler();
    }

    public ConfigurationManager getConfigurationManager() {
        return this.configurationManager;
    }

    public CooldownManager getCooldownManager() {
        return this.cooldownManager;
    }
}