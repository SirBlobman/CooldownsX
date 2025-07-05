package com.github.sirblobman.plugin.cooldown;

import java.util.logging.Logger;

import org.jetbrains.annotations.NotNull;

import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.sirblobman.api.configuration.ConfigurationManager;
import com.github.sirblobman.api.core.CorePlugin;
import com.github.sirblobman.api.language.Language;
import com.github.sirblobman.api.language.LanguageManager;
import com.github.sirblobman.api.plugin.ConfigurablePlugin;
import com.github.sirblobman.api.update.SpigotUpdateManager;
import com.github.sirblobman.api.utility.VersionUtility;
import com.github.sirblobman.plugin.cooldown.api.CooldownsX;
import com.github.sirblobman.plugin.cooldown.api.configuration.EnumDictionary;
import com.github.sirblobman.plugin.cooldown.api.data.PlayerCooldownManager;
import com.github.sirblobman.plugin.cooldown.command.CommandCooldownsX;
import com.github.sirblobman.plugin.cooldown.api.configuration.MainConfiguration;
import com.github.sirblobman.plugin.cooldown.dictionary.EntityDictionary;
import com.github.sirblobman.plugin.cooldown.dictionary.MaterialDictionary;
import com.github.sirblobman.plugin.cooldown.dictionary.PotionDictionary;
import com.github.sirblobman.plugin.cooldown.listener.ListenerConsume;
import com.github.sirblobman.plugin.cooldown.listener.ListenerInteract;
import com.github.sirblobman.plugin.cooldown.listener.ListenerPlaceEntity;
import com.github.sirblobman.plugin.cooldown.listener.ListenerPotionModern;
import com.github.sirblobman.plugin.cooldown.listener.ListenerPotionThrow;
import com.github.sirblobman.plugin.cooldown.listener.ListenerUndying;
import com.github.sirblobman.plugin.cooldown.manager.CooldownManager;
import com.github.sirblobman.plugin.cooldown.placeholder.HookPlaceholderAPI;
import com.github.sirblobman.plugin.cooldown.task.ActionBarTask;
import com.github.sirblobman.plugin.cooldown.task.ExpireTask;
import com.github.sirblobman.api.shaded.bstats.bukkit.Metrics;
import com.github.sirblobman.api.shaded.bstats.charts.SimplePie;
import com.github.sirblobman.api.shaded.xseries.XMaterial;
import com.github.sirblobman.api.shaded.xseries.XPotion;

public final class CooldownPlugin extends ConfigurablePlugin implements CooldownsX {
    private final CooldownManager cooldownManager;
    private final MaterialDictionary materialDictionary;
    private final PotionDictionary potionDictionary;
    private final EntityDictionary entityDictionary;

    private final MainConfiguration configuration;

    public CooldownPlugin() {
        this.cooldownManager = new CooldownManager(this);
        this.materialDictionary = new MaterialDictionary(this);
        this.potionDictionary = new PotionDictionary(this);
        this.entityDictionary = new EntityDictionary(this);
        this.configuration = new MainConfiguration();
    }

    @Override
    public @NotNull ConfigurablePlugin getPlugin() {
        return this;
    }

    @Override
    public void onLoad() {
        ConfigurationManager configurationManager = getConfigurationManager();
        configurationManager.saveDefault("config.yml");
        configurationManager.saveDefault("cooldowns.yml");
        configurationManager.saveDefault("dictionary/material.yml");
        configurationManager.saveDefault("dictionary/potion.yml");
        configurationManager.saveDefault("dictionary/entity.yml");

        LanguageManager languageManager = getLanguageManager();
        languageManager.saveDefaultLanguageFiles();
    }

    @Override
    public void onEnable() {
        int minorVersion = VersionUtility.getMinorVersion();
        if (minorVersion < 21) {
            Logger logger = getLogger();
            logger.warning("This plugin requires version 1.21.7 or above!");
            setEnabled(false);
            return;
        }

        reloadConfiguration();

        LanguageManager languageManager = getLanguageManager();
        languageManager.onPluginEnable();

        registerCommands();
        registerListeners();
        registerHooks();

        registerUpdateChecker();
        register_bStats();
    }

    @Override
    public void onDisable() {
        // Empty Method
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

        EnumDictionary<XMaterial> materialDictionary = getMaterialDictionary();
        materialDictionary.reloadConfiguration();
        materialDictionary.saveConfiguration();

        EnumDictionary<XPotion> potionDictionary = getPotionDictionary();
        potionDictionary.reloadConfiguration();
        potionDictionary.saveConfiguration();

        EnumDictionary<EntityType> entityDictionary = getEntityDictionary();
        entityDictionary.reloadConfiguration();
        entityDictionary.saveConfiguration();

        MainConfiguration mainConfig = getConfiguration();
        mainConfig.load(configurationManager.get("config.yml"));

        PlayerCooldownManager cooldownManager = getCooldownManager();
        cooldownManager.reloadConfig();
        registerTasks();
    }

    @Override
    public @NotNull PlayerCooldownManager getCooldownManager() {
        return this.cooldownManager;
    }

    @Override
    public @NotNull EnumDictionary<XMaterial> getMaterialDictionary() {
        return this.materialDictionary;
    }

    @Override
    public @NotNull EnumDictionary<XPotion> getPotionDictionary() {
        return this.potionDictionary;
    }

    @Override
    public @NotNull EnumDictionary<EntityType> getEntityDictionary() {
        return this.entityDictionary;
    }

    @Override
    public @NotNull MainConfiguration getConfiguration() {
        return this.configuration;
    }

    private void registerCommands() {
        new CommandCooldownsX(this).register();
    }

    private void registerListeners() {
        new ListenerConsume(this).register();
        new ListenerInteract(this).register();
        new ListenerPotionThrow(this).register();
        new ListenerUndying(this).register();
        new ListenerPotionModern(this).register();
        new ListenerPlaceEntity(this).register();
    }

    private void registerTasks() {
        MainConfiguration configuration = getConfiguration();
        if(configuration.isUseActionBar()) {
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
        SpigotUpdateManager updateManager = corePlugin.getSpigotUpdateManager();
        updateManager.addResource(this, 41981L);
    }

    private void register_bStats() {
        Metrics metrics = new Metrics(this, 16126);
        SimplePie languagePie = new SimplePie("selected_language", this::getDefaultLanguageCode);
        metrics.addCustomChart(languagePie);
    }

    private @NotNull String getDefaultLanguageCode() {
        LanguageManager languageManager = getLanguageManager();
        Language defaultLanguage = languageManager.getDefaultLanguage();
        return (defaultLanguage == null ? "none" : defaultLanguage.getLanguageName());
    }
}
