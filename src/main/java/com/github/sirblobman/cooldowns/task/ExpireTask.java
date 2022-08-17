package com.github.sirblobman.cooldowns.task;

import java.util.Collection;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.github.sirblobman.cooldowns.CooldownPlugin;
import com.github.sirblobman.cooldowns.configuration.CooldownSettings;
import com.github.sirblobman.cooldowns.object.CooldownData;

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
        CooldownData cooldownData = getCooldownData(player);
        Set<CooldownSettings> activeCooldownSet = cooldownData.getActiveCooldowns();
        for (CooldownSettings cooldown : activeCooldownSet) {
            checkCooldown(cooldownData, cooldown);
        }
    }

    private void checkCooldown(CooldownData data, CooldownSettings cooldown) {
        long expireMillis = data.getCooldownExpireTime(cooldown);
        long systemMillis = System.currentTimeMillis();
        if(systemMillis >= expireMillis) {
            data.removeCooldown(cooldown);
        }
    }
}
