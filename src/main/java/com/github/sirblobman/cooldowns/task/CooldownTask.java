package com.github.sirblobman.cooldowns.task;

import java.util.Locale;
import java.util.logging.Logger;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.github.sirblobman.api.configuration.ConfigurationManager;
import com.github.sirblobman.api.language.LanguageManager;
import com.github.sirblobman.api.utility.Validate;
import com.github.sirblobman.cooldowns.CooldownPlugin;
import com.github.sirblobman.cooldowns.manager.CooldownManager;
import com.github.sirblobman.cooldowns.object.CooldownData;

import org.jetbrains.annotations.NotNull;

public abstract class CooldownTask extends BukkitRunnable {
    private final CooldownPlugin plugin;

    public CooldownTask(CooldownPlugin plugin) {
        this.plugin = Validate.notNull(plugin, "plugin must not be null!");
    }

    protected final CooldownPlugin getPlugin() {
        return this.plugin;
    }

    protected final LanguageManager getLanguageManager() {
        CooldownPlugin plugin = getPlugin();
        return plugin.getLanguageManager();
    }

    protected final CooldownManager getCooldownManager() {
        CooldownPlugin plugin = getPlugin();
        return plugin.getCooldownManager();
    }

    @NotNull
    protected final CooldownData getCooldownData(Player player) {
        CooldownManager cooldownManager = getCooldownManager();
        return cooldownManager.getData(player);
    }

    private boolean isDebugModeDisabled() {
        CooldownPlugin plugin = getPlugin();
        ConfigurationManager configurationManager = plugin.getConfigurationManager();
        YamlConfiguration configuration = configurationManager.get("config.yml");
        return !configuration.getBoolean("debug-mode", false);
    }

    protected final void printDebug(String message) {
        if(isDebugModeDisabled()) {
            return;
        }

        Class<? extends CooldownTask> thisClass = getClass();
        String className = thisClass.getSimpleName();

        CooldownPlugin plugin = getPlugin();
        Logger logger = plugin.getLogger();
        String logMessage = String.format(Locale.US, "[Debug] [%s] %s", className, message);
        logger.info(logMessage);
    }

    public void startAsync() {
        CooldownPlugin plugin = getPlugin();
        runTaskTimerAsynchronously(plugin, 1L, 1L);
    }

    @Override public abstract void run();
}
