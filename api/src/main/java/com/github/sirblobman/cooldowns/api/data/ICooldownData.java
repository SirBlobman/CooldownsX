package com.github.sirblobman.cooldowns.api.data;

import java.util.Set;
import java.util.UUID;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import com.github.sirblobman.cooldowns.api.configuration.CooldownType;
import com.github.sirblobman.cooldowns.api.configuration.ICooldownSettings;

public interface ICooldownData {
    UUID getPlayerId();
    OfflinePlayer getOfflinePlayer();
    Player getPlayer();

    Set<ICooldownSettings> getActiveCooldowns();
    Set<ICooldownSettings> getActiveCooldowns(CooldownType cooldownType);
    long getCooldownExpireTime(ICooldownSettings settings);
    void setCooldown(ICooldownSettings settings, long expireTime);
    void removeCooldown(ICooldownSettings settings);

    int getActionCount(ICooldownSettings settings);
    void setActionCount(ICooldownSettings settings, int count);
    void loadActionCounts();
    void saveActionCounts();
}
