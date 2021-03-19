package com.github.sirblobman.cooldowns;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitScheduler;

import com.github.sirblobman.api.configuration.ConfigurationManager;
import com.github.sirblobman.api.core.plugin.ConfigurablePlugin;
import com.github.sirblobman.api.update.UpdateChecker;
import com.github.sirblobman.api.utility.VersionUtility;
import com.github.sirblobman.api.xseries.XMaterial;
import com.github.sirblobman.cooldowns.command.CommandCooldownsX;
import com.github.sirblobman.cooldowns.listener.ListenerConsume;
import com.github.sirblobman.cooldowns.listener.ListenerInteract;
import com.github.sirblobman.cooldowns.listener.ListenerUndying;
import com.github.sirblobman.cooldowns.manager.CooldownManager;
import com.github.sirblobman.cooldowns.manager.UndyingManager;
import com.github.sirblobman.cooldowns.task.ActionBarTask;

public final class CooldownPlugin extends ConfigurablePlugin {
    private final CooldownManager cooldownManager;
    private final UndyingManager undyingManager;
    private final Map<XMaterial, String> materialDictionaryMap;

    public CooldownPlugin() {
        this.cooldownManager = new CooldownManager(this);
        this.undyingManager = new UndyingManager(this);
        this.materialDictionaryMap = new HashMap<>();
    }

    @Override
    public void onLoad() {
        ConfigurationManager configurationManager = getConfigurationManager();
        configurationManager.saveDefault("config.yml");
        configurationManager.saveDefault("cooldowns.yml");
        configurationManager.saveDefault("material.yml");
        configurationManager.saveDefault("undying.yml");
    }

    @Override
    public void onEnable() {
        int minorVersion = VersionUtility.getMinorVersion();
        if(minorVersion < 8) {
            Logger logger = getLogger();
            logger.warning("This plugin requires version 1.8.8 or above!");
            setEnabled(false);
            return;
        }

        onReload();
        new CommandCooldownsX(this).register();

        new ListenerConsume(this).register();
        new ListenerInteract(this).register();
        new ListenerUndying(this).register();

        UpdateChecker updateChecker = new UpdateChecker(this, 41_981L);
        updateChecker.runCheck();
    }

    @Override
    public void onDisable() {
        BukkitScheduler scheduler = Bukkit.getScheduler();
        scheduler.cancelTasks(this);
    }

    public void onReload() {
        ConfigurationManager configurationManager = getConfigurationManager();
        configurationManager.reload("config.yml");
        configurationManager.reload("cooldowns.yml");
        configurationManager.reload("material.yml");
        configurationManager.reload("undying.yml");

        setupMaterialNames();
        CooldownManager cooldownManager = getCooldownManager();
        cooldownManager.loadCooldowns();

        BukkitScheduler scheduler = Bukkit.getScheduler();
        scheduler.cancelTasks(this);

        YamlConfiguration configuration = configurationManager.get("config.yml");
        if(configuration.getBoolean("use-action-bar")) {
            ActionBarTask actionBarTask = new ActionBarTask(this);
            actionBarTask.start();
        }
    }

    public CooldownManager getCooldownManager() {
        return this.cooldownManager;
    }

    public UndyingManager getUndyingManager() {
        return this.undyingManager;
    }

    public String getMaterialName(XMaterial material) {
        return this.materialDictionaryMap.getOrDefault(material, material.name());
    }

    private void setupMaterialNames() {
        XMaterial[] materialArray = XMaterial.values();
        ConfigurationManager configurationManager = getConfigurationManager();
        YamlConfiguration configuration = configurationManager.get("material.yml");

        for(XMaterial material : materialArray) {
            String materialName = material.name();
            if(!configuration.isSet(materialName)) {
                configuration.set(materialName, materialName);
            }

            String dictionaryName = configuration.getString(materialName);
            this.materialDictionaryMap.put(material, dictionaryName);
        }

        configurationManager.save("material.yml");
    }
}