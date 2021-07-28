package com.github.sirblobman.cooldowns.manager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.github.sirblobman.api.configuration.ConfigurationManager;
import com.github.sirblobman.api.utility.Validate;
import com.github.sirblobman.cooldowns.CooldownPlugin;

public final class UndyingManager {
    private final CooldownPlugin plugin;
    private final Map<UUID, Long> cooldownExpireMap;

    public UndyingManager(CooldownPlugin plugin) {
        this.plugin = Validate.notNull(plugin, "plugin must not be null!");
        this.cooldownExpireMap = new HashMap<>();
    }

    public boolean hasCooldown(Player player) {
        UUID uuid = player.getUniqueId();
        long expireMillis = this.cooldownExpireMap.getOrDefault(uuid, -1L);
        long systemMillis = System.currentTimeMillis();
        return (systemMillis < expireMillis);
    }

    public void addCooldown(Player player) {
        ConfigurationManager configurationManager = this.plugin.getConfigurationManager();
        YamlConfiguration configuration = configurationManager.get("undying.yml");
        long cooldownMillis = configuration.getLong("totem-cooldown");
        if(cooldownMillis < 1L) return;

        UUID uuid = player.getUniqueId();
        long systemMillis = System.currentTimeMillis();
        long expireMillis = (systemMillis + cooldownMillis);
        this.cooldownExpireMap.put(uuid, expireMillis);
    }

    public long getCooldownMillisLeft(Player player) {
        if(!hasCooldown(player)) return -1L;
        UUID uuid = player.getUniqueId();
        long expireMillis = this.cooldownExpireMap.getOrDefault(uuid, -1L);

        long systemMillis = System.currentTimeMillis();
        return (expireMillis - systemMillis);
    }
}
