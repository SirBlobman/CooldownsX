package com.github.sirblobman.cooldowns;

import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import com.github.sirblobman.api.shaded.bstats.bukkit.Metrics;
import com.github.sirblobman.api.shaded.bstats.charts.SimplePie;
import com.github.sirblobman.api.configuration.ConfigurationManager;
import com.github.sirblobman.api.core.CorePlugin;
import com.github.sirblobman.api.language.Language;
import com.github.sirblobman.api.language.LanguageManager;
import com.github.sirblobman.api.plugin.ConfigurablePlugin;
import com.github.sirblobman.api.update.UpdateManager;
import com.github.sirblobman.api.utility.VersionUtility;
import com.github.sirblobman.cooldowns.api.ICooldownsX;
import com.github.sirblobman.cooldowns.command.CommandCooldownsX;
import com.github.sirblobman.cooldowns.dictionary.MaterialDictionary;
import com.github.sirblobman.cooldowns.dictionary.PotionDictionary;
import com.github.sirblobman.cooldowns.listener.ListenerConsume;
import com.github.sirblobman.cooldowns.listener.ListenerInteract;
import com.github.sirblobman.cooldowns.listener.ListenerPotionLegacy;
import com.github.sirblobman.cooldowns.listener.ListenerPotionModern;
import com.github.sirblobman.cooldowns.listener.ListenerPotionThrow;
import com.github.sirblobman.cooldowns.listener.ListenerUndying;
import com.github.sirblobman.cooldowns.manager.CooldownManager;
import com.github.sirblobman.cooldowns.placeholder.HookPlaceholderAPI;
import com.github.sirblobman.cooldowns.task.ActionBarTask;
import com.github.sirblobman.cooldowns.task.ExpireTask;

public final class CooldownPlugin extends ConfigurablePlugin implements ICooldownsX {
    private final CooldownManager cooldownManager;
    private final MaterialDictionary materialDictionary;
    private final PotionDictionary potionDictionary;

    public CooldownPlugin() {
        this.cooldownManager = new CooldownManager(this);
        this.materialDictionary = new MaterialDictionary(this);
        this.potionDictionary = new PotionDictionary(this);
    }

    @Override
    public CooldownPlugin getPlugin() {
        return this;
    }

    @Override
    public void onLoad() {
        ConfigurationManager configurationManager = getConfigurationManager();
        configurationManager.saveDefault("config.yml");
        configurationManager.saveDefault("cooldowns.yml");
        configurationManager.saveDefault("dictionary/material.yml");
        configurationManager.saveDefault("dictionary/potion.yml");

        LanguageManager languageManager = getLanguageManager();
        languageManager.saveDefaultLanguageFiles();
    }

    @Override
    public void onEnable() {
        int minorVersion = VersionUtility.getMinorVersion();
        if (minorVersion < 8) {
            Logger logger = getLogger();
            logger.warning("This plugin requires version 1.8.8 or above!");
            setEnabled(false);
            return;
        }

        reloadConfiguration();

        LanguageManager languageManager = getLanguageManager();
        languageManager.onPluginEnable();

        registerCommands();
        registerListeners(minorVersion);
        registerHooks();

        registerUpdateChecker();
        register_bStats();
    }

    @Override
    public void onDisable() {
        BukkitScheduler scheduler = Bukkit.getScheduler();
        scheduler.cancelTasks(this);
    }

    @Override
    public void reloadConfiguration() {
        ConfigurationManager configurationManager = getConfigurationManager();
        configurationManager.reload("config.yml");
        configurationManager.reload("cooldowns.yml");
        configurationManager.reload("dictionary/material.yml");
        configurationManager.reload("dictionary/potion.yml");

        LanguageManager languageManager = getLanguageManager();
        languageManager.reloadLanguages();

        MaterialDictionary materialDictionary = getMaterialDictionary();
        materialDictionary.reloadConfiguration();
        materialDictionary.saveConfiguration();

        PotionDictionary potionDictionary = getPotionDictionary();
        potionDictionary.reloadConfiguration();
        potionDictionary.saveConfiguration();

        CooldownManager cooldownManager = getCooldownManager();
        cooldownManager.reloadConfig();

        BukkitScheduler scheduler = Bukkit.getScheduler();
        scheduler.cancelTasks(this);
        registerTasks();
    }

    public CooldownManager getCooldownManager() {
        return this.cooldownManager;
    }

    public MaterialDictionary getMaterialDictionary() {
        return this.materialDictionary;
    }

    public PotionDictionary getPotionDictionary() {
        return this.potionDictionary;
    }

    private void registerCommands() {
        new CommandCooldownsX(this).register();
    }

    private void registerListeners(int minorVersion) {
        new ListenerConsume(this).register();
        new ListenerInteract(this).register();
        new ListenerPotionThrow(this).register();

        if (minorVersion >= 11) {
            new ListenerUndying(this).register();
        }

        if (minorVersion >= 13) {
            new ListenerPotionModern(this).register();
        } else {
            new ListenerPotionLegacy(this).register();
        }
    }

    private void registerTasks() {
        ConfigurationManager configurationManager = getConfigurationManager();
        YamlConfiguration configuration = configurationManager.get("config.yml");
        if (configuration.getBoolean("use-action-bar")) {
            ActionBarTask actionBarTask = new ActionBarTask(this);
            actionBarTask.startAsync();
        }

        ExpireTask expireTask = new ExpireTask(this);
        expireTask.startAsync();
    }

    private void registerHooks() {
        PluginManager pluginManager = Bukkit.getPluginManager();
        if (pluginManager.isPluginEnabled("PlaceholderAPI")) {
            new HookPlaceholderAPI(this).register();
        }
    }

    private void registerUpdateChecker() {
        CorePlugin corePlugin = JavaPlugin.getPlugin(CorePlugin.class);
        UpdateManager updateManager = corePlugin.getUpdateManager();
        updateManager.addResource(this, 41981L);
    }

    private void register_bStats() {
        Metrics metrics = new Metrics(this, 16126);
        SimplePie languagePie = new SimplePie("selected_language", this::getDefaultLanguageCode);
        metrics.addCustomChart(languagePie);
    }

    private String getDefaultLanguageCode() {
        LanguageManager languageManager = getLanguageManager();
        Language defaultLanguage = languageManager.getDefaultLanguage();
        return (defaultLanguage == null ? "none" : defaultLanguage.getLanguageName());
    }
}
