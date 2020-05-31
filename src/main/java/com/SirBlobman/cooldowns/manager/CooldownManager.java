package com.SirBlobman.cooldowns.manager;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import com.SirBlobman.cooldowns.CooldownPlugin;
import com.SirBlobman.cooldowns.object.CooldownInfo;

import org.bukkit.entity.Player;

public final class CooldownManager {
    private final CooldownPlugin plugin;
    private final Map<UUID, CooldownInfo> cooldownInfoMap;
    public CooldownManager(CooldownPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin must not be null!");
        this.cooldownInfoMap = new HashMap<>();
    }
    
    public CooldownPlugin getPlugin() {
        return this.plugin;
    }
    
    public CooldownInfo getCooldownInfo(Player player) {
        UUID uuid = player.getUniqueId();
        CooldownInfo cooldownInfo = this.cooldownInfoMap.getOrDefault(uuid, null);
        return (cooldownInfo == null ? createCooldownInfo(player) : cooldownInfo);
    }
    
    private CooldownInfo createCooldownInfo(Player player) {
        UUID uuid = player.getUniqueId();
        CooldownInfo cooldownInfo = new CooldownInfo(player);
        
        this.cooldownInfoMap.put(uuid, cooldownInfo);
        return cooldownInfo;
    }
}