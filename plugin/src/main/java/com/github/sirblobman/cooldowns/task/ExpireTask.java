package com.github.sirblobman.cooldowns.task;

import java.util.Collection;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.github.sirblobman.cooldowns.CooldownPlugin;
import com.github.sirblobman.cooldowns.api.configuration.ICooldownSettings;
import com.github.sirblobman.cooldowns.api.data.ICooldownData;

public final class ExpireTask extends CooldownTask {
    public ExpireTask(CooldownPlugin plugin) {
        super(plugin);
    }

    @Override
    public void run() {
        Collection<? extends Player> onlinePlayerCollection = Bukkit.getOnlinePlayers();
        for (Player player : onlinePlayerCollection) {
            checkPlayer(player);
        }
    }

    private void checkPlayer(Player player) {
        ICooldownData cooldownData = getCooldownData(player);
        Set<ICooldownSettings> activeCooldownSet = cooldownData.getActiveCooldowns();
        for (ICooldownSettings cooldown : activeCooldownSet) {
            checkCooldown(cooldownData, cooldown);
        }
    }

    private void checkCooldown(ICooldownData data, ICooldownSettings cooldown) {
        long expireMillis = data.getCooldownExpireTime(cooldown);
        long systemMillis = System.currentTimeMillis();
        if(systemMillis >= expireMillis) {
            data.removeCooldown(cooldown);
        }
    }
}
