package com.github.sirblobman.cooldowns.task;

import org.jetbrains.annotations.NotNull;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.github.sirblobman.api.language.LanguageManager;
import com.github.sirblobman.cooldowns.api.ICooldownsX;
import com.github.sirblobman.cooldowns.api.data.ICooldownData;
import com.github.sirblobman.cooldowns.api.manager.ICooldownManager;

public abstract class CooldownTask extends BukkitRunnable {
    private final ICooldownsX plugin;

    public CooldownTask(@NotNull ICooldownsX plugin) {
        this.plugin = plugin;
    }

    protected final ICooldownsX getCooldownsX() {
        return this.plugin;
    }

    protected final @NotNull JavaPlugin getJavaPlugin() {
        ICooldownsX plugin = getCooldownsX();
        return plugin.getPlugin();
    }

    protected final @NotNull LanguageManager getLanguageManager() {
        ICooldownsX plugin = getCooldownsX();
        return plugin.getLanguageManager();
    }

    protected final @NotNull ICooldownManager getCooldownManager() {
        ICooldownsX plugin = getCooldownsX();
        return plugin.getCooldownManager();
    }

    protected final @NotNull ICooldownData getCooldownData(Player player) {
        ICooldownManager cooldownManager = getCooldownManager();
        return cooldownManager.getData(player);
    }

    public void startAsync() {
        JavaPlugin plugin = getJavaPlugin();
        runTaskTimerAsynchronously(plugin, 1L, 1L);
    }

    @Override
    public abstract void run();
}
