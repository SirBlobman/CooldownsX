package com.SirBlobman.cooldowns.manager;

import java.util.*;
import java.util.logging.Logger;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import com.SirBlobman.api.configuration.ConfigurationManager;
import com.SirBlobman.api.utility.Validate;
import com.SirBlobman.cooldowns.CooldownPlugin;
import com.SirBlobman.cooldowns.object.CooldownData;

public final class CooldownManager {
    private final CooldownPlugin plugin;
    private final Map<UUID, CooldownData> cooldownDataMap;
    private final Map<Material, Long> defaultCooldownMap;
    private final Map<Material, String> defaultBypassPermissionMap;
    public CooldownManager(CooldownPlugin plugin) {
        this.plugin = Validate.notNull(plugin, "plugin must not be null!");
        this.cooldownDataMap = new HashMap<>();
        this.defaultCooldownMap = new EnumMap<>(Material.class);
        this.defaultBypassPermissionMap = new EnumMap<>(Material.class);
    }

    public CooldownPlugin getPlugin() {
        return this.plugin;
    }

    public void loadDefaultCooldowns() {
        CooldownPlugin plugin = getPlugin();
        ConfigurationManager configurationManager = plugin.getConfigurationManager();
        YamlConfiguration configuration = configurationManager.get("cooldowns.yml");
        this.defaultCooldownMap.clear();

        Set<String> materialNameSet = configuration.getKeys(false);
        for(String materialName : materialNameSet) {
            Material material = Material.matchMaterial(materialName, false);
            long cooldown = configuration.getLong(materialName + ".cooldown", 0L);
            if(material == null || cooldown <= 0L) continue;
            setCooldown(material, cooldown * 1_000L);

            String permission = configuration.getString(materialName + ".permission");
            if(permission != null) this.defaultBypassPermissionMap.put(material, permission);
        }

        long defaultCooldownMapSize = this.defaultCooldownMap.size();
        Logger logger = plugin.getLogger();
        logger.info("Successfully loaded " + defaultCooldownMapSize + " item cooldown(s).");
    }

    public CooldownData getData(OfflinePlayer player) {
        UUID uuid = player.getUniqueId();
        CooldownData cooldownData = this.cooldownDataMap.getOrDefault(uuid, null);
        if(cooldownData != null) return cooldownData;

        CooldownData newData = new CooldownData(player);
        this.cooldownDataMap.put(uuid, newData);
        return newData;
    }

    public boolean hasCooldown(Material material) {
        return this.defaultCooldownMap.containsKey(material);
    }

    public long getCooldown(Material material) {
        return this.defaultCooldownMap.getOrDefault(material, -1L);
    }

    public void setCooldown(Material material, long millis) {
        this.defaultCooldownMap.put(material, millis);
    }

    public boolean canBypass(Player player, Material material) {
        if(hasCooldown(material)) {
            String permissionName = this.defaultBypassPermissionMap.getOrDefault(material, null);
            if(permissionName == null) return false;

            Permission permission = new Permission(permissionName, "CooldownsX Bypass Permission", PermissionDefault.FALSE);
            return player.hasPermission(permission);
        }

        return true;
    }
}