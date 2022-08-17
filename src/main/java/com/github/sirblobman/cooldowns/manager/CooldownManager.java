package com.github.sirblobman.cooldowns.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import com.github.sirblobman.api.configuration.ConfigurationManager;
import com.github.sirblobman.api.utility.Validate;
import com.github.sirblobman.api.xseries.XMaterial;
import com.github.sirblobman.api.xseries.XPotion;
import com.github.sirblobman.cooldowns.CooldownPlugin;
import com.github.sirblobman.cooldowns.configuration.ActionBarSettings;
import com.github.sirblobman.cooldowns.configuration.CooldownSettings;
import com.github.sirblobman.cooldowns.object.CombatMode;
import com.github.sirblobman.cooldowns.object.CooldownData;
import com.github.sirblobman.cooldowns.object.CooldownType;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class CooldownManager {
    private final CooldownPlugin plugin;
    private final Map<UUID, CooldownData> cooldownDataMap;
    private final Map<String, CooldownSettings> cooldownSettingsMap;

    public CooldownManager(CooldownPlugin plugin) {
        this.plugin = Validate.notNull(plugin, "plugin must not be null!");
        this.cooldownDataMap = new ConcurrentHashMap<>();
        this.cooldownSettingsMap = new HashMap<>();
    }

    @NotNull
    public CooldownData getData(OfflinePlayer player) {
        UUID playerId = player.getUniqueId();
        CooldownData cooldownData = this.cooldownDataMap.getOrDefault(playerId, null);
        if (cooldownData != null) {
            return cooldownData;
        }

        CooldownData newData = new CooldownData(player);
        this.cooldownDataMap.put(playerId, newData);
        return newData;
    }

    @Nullable
    public CooldownSettings getCooldownSettings(String id) {
        return this.cooldownSettingsMap.get(id);
    }

    @NotNull
    public List<CooldownSettings> getAllCooldownSettings() {
        Collection<CooldownSettings> valueCollection = this.cooldownSettingsMap.values();
        List<CooldownSettings> cooldownSettingsList = new ArrayList<>(valueCollection);
        return Collections.unmodifiableList(cooldownSettingsList);
    }

    public void reloadConfig() {
        printDebug("Reload Cooldown Settings Start");

        this.cooldownSettingsMap.clear();
        printDebug("Removed old cooldown settings.");

        this.cooldownDataMap.clear();
        printDebug("Removed all current player cooldowns.");

        ConfigurationManager configurationManager = getConfigurationManager();
        YamlConfiguration configuration = configurationManager.get("cooldowns.yml");
        printDebug("Loaded cooldowns.yml");

        Set<String> cooldownIdSet = configuration.getKeys(false);
        for (String cooldownId : cooldownIdSet) {
            printDebug("Loading cooldown '" + cooldownId + "'.");

            ConfigurationSection section = configuration.getConfigurationSection(cooldownId);
            if(section == null) {
                printDebug("'" + cooldownId + "' is not a valid cooldown section.");
                continue;
            }

            int amount = section.getInt("amount", 1);
            boolean resetAmount = section.getBoolean("reset-amount", false);
            int cooldownSeconds = section.getInt("cooldown", 10);
            String cooldownTypeName = section.getString("cooldown-type", "INTERACT_ITEM");
            List<String> materialNameList = section.getStringList("material");
            List<String> potionNameList = section.getStringList("potion-effect");
            String bypassPermissionName = section.getString("bypass-permission");
            boolean packetCooldown = section.getBoolean("packet-cooldown", false);
            String combatModeName = section.getString("combat-mode", "IGNORE");
            int combatCooldownSeconds = section.getInt("combat-cooldown-seconds", 5);
            List<String> disabledWorldList = section.getStringList("disabled-world-list");
            boolean invertDisabledWorldList = section.getBoolean("invert-disabled-world-list", false);
            String messageFormat = section.getString("message-format");

            boolean actionBarEnabled = section.getBoolean("action-bar.enabled", false);
            int actionBarPriority = section.getInt("action-bar.priority", 0);
            String actionBarMessageFormat = section.getString("action-bar.message-format");

            printDebug("Amount: " + amount);
            printDebug("Reset Amount: " + resetAmount);
            printDebug("Cooldown Seconds: " + cooldownSeconds);
            printDebug("Cooldown Type: " + cooldownTypeName);
            printDebug("Material List: " + materialNameList);
            printDebug("Potion List: " + potionNameList);
            printDebug("Bypass Permission: " + bypassPermissionName);
            printDebug("Packet Cooldown: " + packetCooldown);
            printDebug("Combat Mode: " + combatModeName);
            printDebug("Combat Cooldown Seconds: " + combatCooldownSeconds);
            printDebug("Disabled World List: " + disabledWorldList);
            printDebug("Invert Disabled World List: " + invertDisabledWorldList);
            printDebug("Message: " + messageFormat);
            printDebug("Action Bar Enabled: " + actionBarEnabled);
            printDebug("Action Bar Priority: " + actionBarPriority);
            printDebug("Action Bar Message: " + actionBarMessageFormat);

            try {
                CooldownType cooldownType = CooldownType.valueOf(cooldownTypeName);
                List<XMaterial> materialList = materialNameList.stream().map(XMaterial::matchXMaterial)
                        .filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
                List<XPotion> potionList = potionNameList.stream().map(XPotion::matchXPotion)
                        .filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
                CombatMode combatMode = CombatMode.valueOf(combatModeName);

                CooldownSettings cooldownSettings = new CooldownSettings(cooldownId);
                cooldownSettings.setAmount(amount);
                cooldownSettings.setResetAmount(resetAmount);
                cooldownSettings.setCooldownSeconds(cooldownSeconds);
                cooldownSettings.setCooldownType(cooldownType);
                cooldownSettings.setMaterialList(materialList);
                cooldownSettings.setPotionList(potionList);
                cooldownSettings.setBypassPermissionName(bypassPermissionName);
                cooldownSettings.setUsePacketCooldown(packetCooldown);
                cooldownSettings.setCombatMode(combatMode);
                cooldownSettings.setCombatCooldownSeconds(combatCooldownSeconds);
                cooldownSettings.setDisabledWorldList(disabledWorldList);
                cooldownSettings.setInvertWorldList(invertDisabledWorldList);
                cooldownSettings.setMessageFormat(messageFormat);

                ActionBarSettings actionBarSettings = cooldownSettings.getActionBarSettings();
                actionBarSettings.setEnabled(actionBarEnabled);
                actionBarSettings.setPriority(actionBarPriority);
                actionBarSettings.setMessageFormat(actionBarMessageFormat);
                cooldownSettings.setActionBarSettings(actionBarSettings);

                this.cooldownSettingsMap.put(cooldownId, cooldownSettings);
                printDebug("Successfully loaded cooldown settings '" + cooldownId + "'.");
            } catch(Exception ex) {
                printDebug("Failed to load cooldown settings '" + cooldownId + "' because an error occurred:");
                printDebug(ex);
            }
        }

        printDebug("Successfully loaded " + this.cooldownSettingsMap.size() + " cooldown(s).");
        printDebug("Reload Cooldown Settings End");
    }

    private CooldownPlugin getPlugin() {
        return this.plugin;
    }

    private ConfigurationManager getConfigurationManager() {
        CooldownPlugin plugin = getPlugin();
        return plugin.getConfigurationManager();
    }

    private Logger getLogger() {
        CooldownPlugin plugin = getPlugin();
        return plugin.getLogger();
    }

    private boolean isDebugModeDisabled() {
        ConfigurationManager configurationManager = getConfigurationManager();
        YamlConfiguration configuration = configurationManager.get("config.yml");
        return !configuration.getBoolean("debug-mode", false);
    }

    private void printDebug(String message) {
        if (isDebugModeDisabled()) {
            return;
        }

        String finalMessage = String.format(Locale.US, "[Debug] %s", message);
        Logger logger = getLogger();
        logger.info(finalMessage);
    }

    private void printDebug(Throwable throwable) {
        if (isDebugModeDisabled()) {
            return;
        }

        Logger logger = getLogger();
        logger.log(Level.WARNING, "[Debug]:", throwable);
    }
}
