package com.SirBlobman.cooldowns;

import java.util.logging.Logger;

import com.SirBlobman.api.configuration.ConfigManager;
import com.SirBlobman.api.plugin.SirBlobmanPlugin;
import com.SirBlobman.cooldowns.listener.ListenerEnderPearlCooldown;
import com.SirBlobman.cooldowns.listener.ListenerGoldenAppleCooldown;
import com.SirBlobman.cooldowns.manager.CooldownManager;
import com.SirBlobman.cooldowns.task.CooldownTimerTask;

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
        
        logger.info("Successfully enabled CooldownsX");
    }
    
    @Override
    public void onDisable() {
        Logger logger = getLogger();
        logger.info("Disabling CooldownsX...");
        logger.info("Successfully disabled CooldownsX");
    }
    
    public CooldownManager getCooldownManager() {
        return this.cooldownManager;
    }
}