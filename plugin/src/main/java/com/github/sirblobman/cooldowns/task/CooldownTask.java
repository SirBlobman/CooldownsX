package com.github.sirblobman.cooldowns.task;

import org.jetbrains.annotations.NotNull;

import org.bukkit.entity.Player;

import com.github.sirblobman.api.folia.FoliaHelper;
import com.github.sirblobman.api.folia.details.TaskDetails;
import com.github.sirblobman.api.folia.scheduler.TaskScheduler;
import com.github.sirblobman.api.language.LanguageManager;
import com.github.sirblobman.api.plugin.ConfigurablePlugin;
import com.github.sirblobman.cooldowns.api.ICooldownsX;
import com.github.sirblobman.cooldowns.api.data.ICooldownData;
import com.github.sirblobman.cooldowns.api.manager.ICooldownManager;

public abstract class CooldownTask extends TaskDetails<ConfigurablePlugin> {
    private final ICooldownsX plugin;

    public CooldownTask(@NotNull ICooldownsX plugin) {
        super(plugin.getPlugin());
        this.plugin = plugin;
    }

    protected final ICooldownsX getCooldownsX() {
        return this.plugin;
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
        setDelay(1L);
        setPeriod(1L);

        ICooldownsX cooldownsX = getCooldownsX();
        FoliaHelper<ConfigurablePlugin> foliaHelper = cooldownsX.getFoliaHelper();
        TaskScheduler<ConfigurablePlugin> scheduler = foliaHelper.getScheduler();
        scheduler.scheduleAsyncTask(this);
    }

    @Override
    public abstract void run();
}
