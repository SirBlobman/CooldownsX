package com.github.sirblobman.cooldowns.api.task;

import org.jetbrains.annotations.NotNull;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.github.sirblobman.api.folia.details.EntityTaskDetails;

public final class UpdateInventoryTask extends EntityTaskDetails<Player> {
    public UpdateInventoryTask(@NotNull Plugin plugin, @NotNull Player entity) {
        super(plugin, entity);
        setDelay(1L);
    }

    @Override
    public void run() {
        Player entity = getEntity();
        if (entity != null) {
            entity.updateInventory();
        }
    }
}
