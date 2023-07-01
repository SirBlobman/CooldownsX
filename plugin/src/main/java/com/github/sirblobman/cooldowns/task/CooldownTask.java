package com.github.sirblobman.cooldowns.task;

import org.jetbrains.annotations.NotNull;

import org.bukkit.entity.Player;

import com.github.sirblobman.api.folia.FoliaHelper;
import com.github.sirblobman.api.folia.details.TaskDetails;
import com.github.sirblobman.api.folia.scheduler.TaskScheduler;
import com.github.sirblobman.api.language.LanguageManager;
import com.github.sirblobman.cooldowns.api.CooldownsX;
import com.github.sirblobman.cooldowns.api.data.PlayerCooldown;
import com.github.sirblobman.cooldowns.api.data.PlayerCooldownManager;

public abstract class CooldownTask extends TaskDetails {
    private final CooldownsX plugin;

    public CooldownTask(@NotNull CooldownsX plugin) {
        super(plugin.getPlugin());
        this.plugin = plugin;
    }

    protected final @NotNull CooldownsX getCooldownsX() {
        return this.plugin;
    }

    protected final @NotNull LanguageManager getLanguageManager() {
        CooldownsX plugin = getCooldownsX();
        return plugin.getLanguageManager();
    }

    protected final @NotNull PlayerCooldownManager getCooldownManager() {
        CooldownsX plugin = getCooldownsX();
        return plugin.getCooldownManager();
    }

    protected final @NotNull PlayerCooldown getCooldownData(Player player) {
        PlayerCooldownManager cooldownManager = getCooldownManager();
        return cooldownManager.getData(player);
    }

    public void startAsync() {
        setDelay(1L);
        setPeriod(1L);

        CooldownsX cooldownsX = getCooldownsX();
        FoliaHelper foliaHelper = cooldownsX.getFoliaHelper();
        TaskScheduler scheduler = foliaHelper.getScheduler();
        scheduler.scheduleAsyncTask(this);
    }

    @Override
    public abstract void run();
}
