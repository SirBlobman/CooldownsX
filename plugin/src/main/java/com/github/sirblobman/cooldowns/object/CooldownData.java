package com.github.sirblobman.cooldowns.object;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.github.sirblobman.api.configuration.PlayerDataManager;
import com.github.sirblobman.api.utility.Validate;
import com.github.sirblobman.cooldowns.CooldownPlugin;
import com.github.sirblobman.cooldowns.api.configuration.CooldownType;
import com.github.sirblobman.cooldowns.api.configuration.ICooldownSettings;
import com.github.sirblobman.cooldowns.api.data.ICooldownData;
import com.github.sirblobman.cooldowns.manager.CooldownManager;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class CooldownData implements ICooldownData {
    private final CooldownPlugin plugin;
    private final UUID playerId;
    private final Map<ICooldownSettings, Long> activeCooldownMap;
    private final Map<ICooldownSettings, Integer> amountCooldownMap;

    public CooldownData(CooldownPlugin plugin, OfflinePlayer player) {
        Validate.notNull(player, "player must not be null!");

        this.plugin = Validate.notNull(plugin, "plugin must not be null!");
        this.playerId = player.getUniqueId();
        this.activeCooldownMap = new ConcurrentHashMap<>();
        this.amountCooldownMap = new ConcurrentHashMap<>();
    }

    @NotNull
    @Override
    public UUID getPlayerId() {
        return this.playerId;
    }

    @NotNull
    @Override
    public OfflinePlayer getOfflinePlayer() {
        UUID playerId = getPlayerId();
        return Bukkit.getOfflinePlayer(playerId);
    }

    @Nullable
    @Override
    public Player getPlayer() {
        UUID playerId = getPlayerId();
        return Bukkit.getPlayer(playerId);
    }

    @Override
    public Set<ICooldownSettings> getActiveCooldowns() {
        Set<ICooldownSettings> keySet = this.activeCooldownMap.keySet();
        Set<ICooldownSettings> activeCooldownSet = new HashSet<>(keySet);
        return Collections.unmodifiableSet(activeCooldownSet);
    }

    @Override
    public Set<ICooldownSettings> getActiveCooldowns(CooldownType cooldownType) {
        Set<ICooldownSettings> allActiveCooldowns = getActiveCooldowns();
        Set<ICooldownSettings> matchingSet = new HashSet<>();

        for (ICooldownSettings activeCooldown : allActiveCooldowns) {
            CooldownType activeType = activeCooldown.getCooldownType();
            if (cooldownType == activeType) {
                matchingSet.add(activeCooldown);
            }
        }

        return Collections.unmodifiableSet(matchingSet);
    }

    @Override
    public long getCooldownExpireTime(ICooldownSettings settings) {
        Validate.notNull(settings, "settings must not be null!");
        return this.activeCooldownMap.getOrDefault(settings, 0L);
    }

    @Override
    public void setCooldown(ICooldownSettings settings, long expireMillis) {
        Validate.notNull(settings, "settings must not be null!");
        long systemMillis = System.currentTimeMillis();
        if(systemMillis >= expireMillis) {
            return;
        }

        this.activeCooldownMap.put(settings, expireMillis);
    }

    @Override
    public void removeCooldown(ICooldownSettings settings) {
        Validate.notNull(settings, "settings must not be null!");
        this.activeCooldownMap.remove(settings);
    }

    @Override
    public int getActionCount(ICooldownSettings settings) {
        Validate.notNull(settings, "settings must not be null!");
        return this.amountCooldownMap.getOrDefault(settings, 0);
    }

    @Override
    public void setActionCount(ICooldownSettings settings, int amount) {
        Validate.notNull(settings, "settings must not be null!");
        this.amountCooldownMap.put(settings, amount);
    }

    public void loadActionCounts() {
        CooldownPlugin plugin = getPlugin();
        PlayerDataManager playerDataManager = plugin.getPlayerDataManager();
        this.amountCooldownMap.clear();

        OfflinePlayer player = getOfflinePlayer();
        YamlConfiguration playerData = playerDataManager.get(player);
        ConfigurationSection actionCountSection = playerData.getConfigurationSection("action-count");
        if(actionCountSection == null) {
            return;
        }

        CooldownManager cooldownManager = plugin.getCooldownManager();
        Set<String> cooldownIdSet = actionCountSection.getKeys(false);
        for (String cooldownId : cooldownIdSet) {
            ICooldownSettings settings = cooldownManager.getCooldownSettings(cooldownId);
            if(settings == null) {
                continue;
            }

            int count = actionCountSection.getInt(cooldownId);
            this.amountCooldownMap.put(settings, count);
        }
    }

    @Override
    public void saveActionCounts() {
        CooldownPlugin plugin = getPlugin();
        PlayerDataManager playerDataManager = plugin.getPlayerDataManager();

        OfflinePlayer player = getOfflinePlayer();
        YamlConfiguration playerData = playerDataManager.get(player);
        playerData.set("action-count", null);

        ConfigurationSection actionCountSection = playerData.createSection("action-count");
        Set<Entry<ICooldownSettings, Integer>> amountMapEntrySet = this.amountCooldownMap.entrySet();
        for (Entry<ICooldownSettings, Integer> entry : amountMapEntrySet) {
            ICooldownSettings settings = entry.getKey();
            String id = settings.getId();
            int count = entry.getValue();
            actionCountSection.set(id, count);
        }

        playerDataManager.save(player);
    }

    private CooldownPlugin getPlugin() {
        return this.plugin;
    }
}
