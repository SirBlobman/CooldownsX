package com.github.sirblobman.cooldowns;

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

import com.github.sirblobman.api.configuration.ConfigurationManager;
import com.github.sirblobman.api.core.CorePlugin;
import com.github.sirblobman.api.nms.MultiVersionHandler;
import com.github.sirblobman.api.update.UpdateChecker;
import com.github.sirblobman.api.utility.VersionUtility;
import com.github.sirblobman.cooldowns.listener.ListenerConsume;
import com.github.sirblobman.cooldowns.listener.ListenerInteract;
import com.github.sirblobman.cooldowns.listener.ListenerUndying;
import com.github.sirblobman.cooldowns.manager.CooldownManager;
import com.github.sirblobman.cooldowns.manager.UndyingManager;
import com.github.sirblobman.cooldowns.task.ActionBarTask;

import org.jetbrains.annotations.NotNull;

public final class CooldownPlugin extends JavaPlugin {
    private final ConfigurationManager configurationManager;
    private final CooldownManager cooldownManager;
    private final UndyingManager undyingManager;
    public CooldownPlugin() {
        this.configurationManager = new ConfigurationManager(this);
        this.cooldownManager = new CooldownManager(this);
        this.undyingManager = new UndyingManager(this);
    }

    @Override
    public void onLoad() {
        ConfigurationManager configurationManager = getConfigurationManager();
        configurationManager.saveDefault("config.yml");
        configurationManager.saveDefault("cooldowns.yml");
        configurationManager.saveDefault("undying.yml");
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
        new ListenerUndying(this).register();

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
        configurationManager.reload("undying.yml");

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

    public UndyingManager getUndyingManager() {
        return this.undyingManager;
    }
}