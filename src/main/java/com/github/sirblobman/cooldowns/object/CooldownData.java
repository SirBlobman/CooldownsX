package com.github.sirblobman.cooldowns.object;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import com.github.sirblobman.api.utility.Validate;
import com.github.sirblobman.cooldowns.configuration.CooldownSettings;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class CooldownData {
    private final UUID playerId;
    private final Map<CooldownSettings, Long> activeCooldownMap;
    private final Map<CooldownSettings, Integer> amountCooldownMap;

    public CooldownData(OfflinePlayer player) {
        Validate.notNull(player, "player must not be null!");
        this.playerId = player.getUniqueId();
        this.activeCooldownMap = new ConcurrentHashMap<>();
        this.amountCooldownMap = new ConcurrentHashMap<>();
    }

    @NotNull
    public UUID getPlayerId() {
        return this.playerId;
    }

    @NotNull
    public OfflinePlayer getOfflinePlayer() {
        UUID playerId = getPlayerId();
        return Bukkit.getOfflinePlayer(playerId);
    }

    @Nullable
    public Player getPlayer() {
        UUID playerId = getPlayerId();
        return Bukkit.getPlayer(playerId);
    }

    public Set<CooldownSettings> getActiveCooldowns() {
        Set<CooldownSettings> activeCooldownSet = new HashSet<>();
        Set<Entry<CooldownSettings, Long>> entrySet = new HashMap<>(this.activeCooldownMap).entrySet();
        for (Entry<CooldownSettings, Long> entry : entrySet) {
            CooldownSettings key = entry.getKey();
            activeCooldownSet.add(key);
        }

        return Collections.unmodifiableSet(activeCooldownSet);
    }

    public List<CooldownSettings> getActiveCooldowns(CooldownType cooldownType) {
        Set<CooldownSettings> allActiveCooldowns = getActiveCooldowns();
        List<CooldownSettings> matchingList = new ArrayList<>();

        for (CooldownSettings activeCooldown : allActiveCooldowns) {
            CooldownType activeType = activeCooldown.getCooldownType();
            if (cooldownType == activeType) {
                matchingList.add(activeCooldown);
            }
        }

        return Collections.unmodifiableList(matchingList);
    }

    public long getCooldownExpireTime(CooldownSettings cooldownSettings) {
        return this.activeCooldownMap.getOrDefault(cooldownSettings, 0L);
    }

    public void setCooldown(CooldownSettings cooldownSettings, long expireMillis) {
        Validate.notNull(cooldownSettings, "cooldownSettings must not be null!");

        long systemMillis = System.currentTimeMillis();
        if(systemMillis >= expireMillis) {
            return;
        }

        this.activeCooldownMap.put(cooldownSettings, expireMillis);
    }

    public int getActionCount(CooldownSettings cooldown) {
        return this.amountCooldownMap.getOrDefault(cooldown, 0);
    }
    
    public void setActionCount(CooldownSettings cooldown, int amount) {
        this.amountCooldownMap.put(cooldown, amount);
        // TODO
    }

    public void loadActionCounts() {
        // TODO
    }

    public void removeCooldown(CooldownSettings cooldown) {
        this.activeCooldownMap.remove(cooldown);
    }
}
