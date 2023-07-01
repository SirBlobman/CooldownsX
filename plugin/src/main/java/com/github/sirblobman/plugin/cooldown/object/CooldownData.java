package com.github.sirblobman.plugin.cooldown.object;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.github.sirblobman.api.configuration.PlayerDataManager;
import com.github.sirblobman.plugin.cooldown.api.CooldownsX;
import com.github.sirblobman.plugin.cooldown.api.configuration.Cooldown;
import com.github.sirblobman.plugin.cooldown.api.configuration.CooldownType;
import com.github.sirblobman.plugin.cooldown.api.data.PlayerCooldown;
import com.github.sirblobman.plugin.cooldown.api.data.PlayerCooldownManager;

public final class CooldownData implements PlayerCooldown {
    private final CooldownsX plugin;
    private final UUID playerId;
    private final Map<Cooldown, Long> activeCooldownMap;
    private final Map<Cooldown, Integer> amountCooldownMap;

    public CooldownData(@NotNull CooldownsX plugin, @NotNull OfflinePlayer player) {
        this.plugin = plugin;
        this.playerId = player.getUniqueId();
        this.activeCooldownMap = new ConcurrentHashMap<>();
        this.amountCooldownMap = new ConcurrentHashMap<>();
    }

    @Override
    public @NotNull UUID getPlayerId() {
        return this.playerId;
    }

    @Override
    public @NotNull OfflinePlayer getOfflinePlayer() {
        UUID playerId = getPlayerId();
        return Bukkit.getOfflinePlayer(playerId);
    }

    @Override
    public @Nullable Player getPlayer() {
        UUID playerId = getPlayerId();
        return Bukkit.getPlayer(playerId);
    }

    @Override
    public @NotNull Set<Cooldown> getActiveCooldowns() {
        Set<Cooldown> keySet = this.activeCooldownMap.keySet();
        Set<Cooldown> activeCooldownSet = new HashSet<>(keySet);
        return Collections.unmodifiableSet(activeCooldownSet);
    }

    @Override
    public @NotNull Set<Cooldown> getActiveCooldowns(@NotNull CooldownType cooldownType) {
        Set<Cooldown> allActiveCooldowns = getActiveCooldowns();
        Set<Cooldown> matchingSet = new HashSet<>();

        for (Cooldown activeCooldown : allActiveCooldowns) {
            CooldownType activeType = activeCooldown.getCooldownType();
            if (cooldownType == activeType) {
                matchingSet.add(activeCooldown);
            }
        }

        return Collections.unmodifiableSet(matchingSet);
    }

    @Override
    public long getCooldownExpireTime(@NotNull Cooldown settings) {
        return this.activeCooldownMap.getOrDefault(settings, 0L);
    }

    @Override
    public void setCooldown(@NotNull Cooldown settings, long expireMillis) {
        long systemMillis = System.currentTimeMillis();
        if (systemMillis >= expireMillis) {
            return;
        }

        this.activeCooldownMap.put(settings, expireMillis);
    }

    @Override
    public void removeCooldown(@NotNull Cooldown settings) {
        this.activeCooldownMap.remove(settings);
    }

    @Override
    public int getActionCount(@NotNull Cooldown settings) {
        return this.amountCooldownMap.getOrDefault(settings, 0);
    }

    @Override
    public void setActionCount(@NotNull Cooldown settings, int amount) {
        this.amountCooldownMap.put(settings, amount);
    }

    public void loadActionCounts() {
        CooldownsX plugin = getCooldownsX();
        PlayerDataManager playerDataManager = plugin.getPlayerDataManager();
        this.amountCooldownMap.clear();

        OfflinePlayer player = getOfflinePlayer();
        YamlConfiguration playerData = playerDataManager.get(player);
        ConfigurationSection actionCountSection = playerData.getConfigurationSection("action-count");
        if (actionCountSection == null) {
            return;
        }

        PlayerCooldownManager cooldownManager = plugin.getCooldownManager();
        Set<String> cooldownIdSet = actionCountSection.getKeys(false);
        for (String cooldownId : cooldownIdSet) {
            Cooldown settings = cooldownManager.getCooldownSettings(cooldownId);
            if (settings == null) {
                continue;
            }

            int count = actionCountSection.getInt(cooldownId);
            this.amountCooldownMap.put(settings, count);
        }
    }

    @Override
    public void saveActionCounts() {
        CooldownsX plugin = getCooldownsX();
        PlayerDataManager playerDataManager = plugin.getPlayerDataManager();

        OfflinePlayer player = getOfflinePlayer();
        YamlConfiguration playerData = playerDataManager.get(player);
        playerData.set("action-count", null);

        ConfigurationSection actionCountSection = playerData.createSection("action-count");
        Set<Entry<Cooldown, Integer>> amountMapEntrySet = this.amountCooldownMap.entrySet();
        for (Entry<Cooldown, Integer> entry : amountMapEntrySet) {
            Cooldown settings = entry.getKey();
            String id = settings.getId();
            int count = entry.getValue();
            actionCountSection.set(id, count);
        }

        playerDataManager.save(player);
    }

    private @NotNull CooldownsX getCooldownsX() {
        return this.plugin;
    }
}
