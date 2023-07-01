package com.github.sirblobman.cooldowns.task;

import java.util.Collection;
import java.util.Set;

import org.jetbrains.annotations.NotNull;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.github.sirblobman.cooldowns.api.CooldownsX;
import com.github.sirblobman.cooldowns.api.configuration.Cooldown;
import com.github.sirblobman.cooldowns.api.data.PlayerCooldown;

public final class ExpireTask extends CooldownTask {
    public ExpireTask(@NotNull CooldownsX plugin) {
        super(plugin);
    }

    @Override
    public void run() {
        Collection<? extends Player> onlinePlayerCollection = Bukkit.getOnlinePlayers();
        for (Player player : onlinePlayerCollection) {
            checkPlayer(player);
        }
    }

    private void checkPlayer(@NotNull Player player) {
        PlayerCooldown cooldownData = getCooldownData(player);
        Set<Cooldown> activeCooldownSet = cooldownData.getActiveCooldowns();
        for (Cooldown cooldown : activeCooldownSet) {
            checkCooldown(cooldownData, cooldown);
        }
    }

    private void checkCooldown(@NotNull PlayerCooldown data, @NotNull Cooldown cooldown) {
        long expireMillis = data.getCooldownExpireTime(cooldown);
        long systemMillis = System.currentTimeMillis();
        if (systemMillis >= expireMillis) {
            data.removeCooldown(cooldown);
        }
    }
}
