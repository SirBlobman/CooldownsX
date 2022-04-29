package com.github.sirblobman.cooldowns.manager;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import com.github.sirblobman.api.configuration.ConfigurationManager;
import com.github.sirblobman.api.utility.Validate;
import com.github.sirblobman.api.xseries.XMaterial;
import com.github.sirblobman.cooldowns.CooldownPlugin;
import com.github.sirblobman.cooldowns.object.CooldownData;
import com.github.sirblobman.cooldowns.object.CooldownSettings;

public final class CooldownManager {
    private final CooldownPlugin plugin;
    private final Map<UUID, CooldownData> cooldownDataMap;
    private final Map<XMaterial, CooldownSettings> cooldownSettingsMap;
    
    public CooldownManager(CooldownPlugin plugin) {
        this.plugin = Validate.notNull(plugin, "plugin must not be null!");
        this.cooldownDataMap = new HashMap<>();
        this.cooldownSettingsMap = new HashMap<>();
    }
    
    public CooldownPlugin getPlugin() {
        return this.plugin;
    }
    
    public void loadCooldowns() {
        CooldownPlugin plugin = getPlugin();
        ConfigurationManager configurationManager = plugin.getConfigurationManager();
        YamlConfiguration configuration = configurationManager.get("cooldowns.yml");
        this.cooldownSettingsMap.clear();
        
        Set<String> materialNameSet = configuration.getKeys(false);
        for(String materialName : materialNameSet) {
            debug("Checking section '" + materialName + "' in cooldowns.yml...");
            XMaterial material = XMaterial.matchXMaterial(materialName).orElse(XMaterial.AIR);
            
            if(material == XMaterial.AIR) {
                debug("'" + materialName + "' is not a valid material name.");
                continue;
            }
            
            Material realMaterial = material.parseMaterial();
            if(realMaterial == null) {
                this.plugin.getLogger().warning("The XMaterial named '" + materialName
                        + "' is not valid for your Spigot version.");
                continue;
            }
            
            if(this.cooldownSettingsMap.containsKey(material)) {
                debug("Skipped '" + materialName
                        + "' because it is a duplicate of another material that is already configured.");
                continue;
            }
            
            ConfigurationSection section = configuration.getConfigurationSection(materialName);
            if(section == null) {
                debug("'" + materialName + "' is not a valid section.");
                continue;
            }

            CooldownSettings cooldownSettings = new CooldownSettings(material);
            cooldownSettings.load(section);

            this.cooldownSettingsMap.put(material, cooldownSettings);
            debug("Successfully loaded section '" + materialName + "'.");
        }
        
        long cooldownMapSize = this.cooldownSettingsMap.size();
        debug("Successfully loaded " + cooldownMapSize + " item cooldown(s).");
    }
    
    public CooldownSettings getCooldownSettings(XMaterial material) {
        return this.cooldownSettingsMap.getOrDefault(material, null);
    }
    
    public CooldownData getData(OfflinePlayer player) {
        UUID uuid = player.getUniqueId();
        CooldownData cooldownData = this.cooldownDataMap.getOrDefault(uuid, null);
        if(cooldownData != null) {
            return cooldownData;
        }
        
        CooldownData newData = new CooldownData(player);
        this.cooldownDataMap.put(uuid, newData);
        return newData;
    }
    
    public boolean hasCooldown(XMaterial material) {
        long cooldownMillis = getCooldown(material);
        return (cooldownMillis > 0L);
    }
    
    public long getCooldown(XMaterial material) {
        CooldownSettings cooldownSettings = getCooldownSettings(material);
        if(cooldownSettings == null) {
            return 0L;
        }

        return cooldownSettings.getCooldownMillis();
    }
    
    public void setCooldown(XMaterial material, CooldownSettings cooldownSettings) {
        this.cooldownSettingsMap.put(material, cooldownSettings);
    }
    
    public boolean canBypass(Player player, XMaterial material) {
        if(hasCooldown(material)) {
            CooldownSettings cooldownSettings = getCooldownSettings(material);
            String permissionName = cooldownSettings.getBypassPermission();
            if(permissionName == null || permissionName.isEmpty()) {
                return false;
            }
            
            Permission permission = new Permission(permissionName, "CooldownsX Bypass Permission", PermissionDefault.FALSE);
            return player.hasPermission(permission);
        }
        
        return true;
    }
    
    private void debug(String message) {
        ConfigurationManager configurationManager = this.plugin.getConfigurationManager();
        YamlConfiguration configuration = configurationManager.get("config.yml");
        if(!configuration.getBoolean("debug-mode")) {
            return;
        }
        
        String finalMessage = String.format("[Debug] %s", message);
        Logger logger = this.plugin.getLogger();
        logger.info(finalMessage);
    }
}
