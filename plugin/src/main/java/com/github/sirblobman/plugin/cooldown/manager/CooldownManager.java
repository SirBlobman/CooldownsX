package com.github.sirblobman.plugin.cooldown.manager;

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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.sirblobman.api.configuration.ConfigurationManager;
import com.github.sirblobman.api.utility.ConfigurationHelper;
import com.github.sirblobman.plugin.cooldown.api.CooldownsX;
import com.github.sirblobman.plugin.cooldown.api.configuration.CombatMode;
import com.github.sirblobman.plugin.cooldown.api.configuration.Cooldown;
import com.github.sirblobman.plugin.cooldown.api.configuration.CooldownType;
import com.github.sirblobman.plugin.cooldown.api.configuration.MainConfiguration;
import com.github.sirblobman.plugin.cooldown.api.data.PlayerCooldown;
import com.github.sirblobman.plugin.cooldown.api.data.PlayerCooldownManager;
import com.github.sirblobman.plugin.cooldown.configuration.ActionBarSettings;
import com.github.sirblobman.plugin.cooldown.configuration.CooldownSettings;
import com.github.sirblobman.plugin.cooldown.object.CooldownData;
import com.github.sirblobman.api.shaded.xseries.XMaterial;
import com.github.sirblobman.api.shaded.xseries.XPotion;

public final class CooldownManager implements PlayerCooldownManager {
    private final CooldownsX plugin;
    private final Map<UUID, PlayerCooldown> cooldownDataMap;
    private final Map<String, Cooldown> cooldownSettingsMap;

    public CooldownManager(@NotNull CooldownsX plugin) {
        this.plugin = plugin;
        this.cooldownDataMap = new ConcurrentHashMap<>();
        this.cooldownSettingsMap = new HashMap<>();
    }

    @Override
    public @NotNull PlayerCooldown getData(@NotNull OfflinePlayer player) {
        UUID playerId = player.getUniqueId();
        PlayerCooldown cooldownData = this.cooldownDataMap.getOrDefault(playerId, null);
        if (cooldownData != null) {
            return cooldownData;
        }

        CooldownsX plugin = getCooldownsX();
        PlayerCooldown newData = new CooldownData(plugin, player);

        MainConfiguration configuration = plugin.getConfiguration();
        if (configuration.isSaveAmountsUsed()) {
            newData.loadActionCounts();
        }

        this.cooldownDataMap.put(playerId, newData);
        return newData;
    }

    @Override
    public @Nullable Cooldown getCooldownSettings(@NotNull String id) {
        return this.cooldownSettingsMap.get(id);
    }

    public @NotNull List<Cooldown> getAllCooldownSettings() {
        Collection<Cooldown> valueCollection = this.cooldownSettingsMap.values();
        List<Cooldown> cooldownSettingsList = new ArrayList<>(valueCollection);
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
            if (section == null) {
                printDebug("'" + cooldownId + "' is not a valid cooldown section.");
                continue;
            }

            int amount = section.getInt("amount", 1);
            boolean resetAmount = section.getBoolean("reset-amount", false);
            int cooldownSeconds = section.getInt("cooldown", 10);
            String cooldownTypeName = section.getString("cooldown-type", "INTERACT_ITEM");
            List<String> materialNameList = section.getStringList("material");
            List<String> potionNameList = section.getStringList("potion-effect");
            List<String> entityTypeList = section.getStringList("entity");
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
                List<EntityType> entityList = new ArrayList<>(ConfigurationHelper.parseEnums(entityTypeList,
                        EntityType.class));
                CombatMode combatMode = CombatMode.valueOf(combatModeName);

                CooldownSettings cooldownSettings = new CooldownSettings(cooldownId);
                cooldownSettings.setAmount(amount);
                cooldownSettings.setResetAmount(resetAmount);
                cooldownSettings.setCooldownSeconds(cooldownSeconds);
                cooldownSettings.setCooldownType(cooldownType);
                cooldownSettings.setMaterialList(materialList);
                cooldownSettings.setPotionList(potionList);
                cooldownSettings.setEntityList(entityList);
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
            } catch (Exception ex) {
                printDebug("Failed to load cooldown settings '" + cooldownId + "':", ex);
            }
        }

        printDebug("Successfully loaded " + this.cooldownSettingsMap.size() + " cooldown(s).");
        printDebug("Reload Cooldown Settings End");
    }

    private @NotNull CooldownsX getCooldownsX() {
        return this.plugin;
    }

    private @NotNull JavaPlugin getJavaPlugin() {
        CooldownsX plugin = getCooldownsX();
        return plugin.getPlugin();
    }

    private @NotNull ConfigurationManager getConfigurationManager() {
        CooldownsX plugin = getCooldownsX();
        return plugin.getConfigurationManager();
    }

    private @NotNull Logger getLogger() {
        JavaPlugin plugin = getJavaPlugin();
        return plugin.getLogger();
    }

    private boolean isDebugModeDisabled() {
        CooldownsX plugin = getCooldownsX();
        MainConfiguration configuration = plugin.getConfiguration();
        return !configuration.isDebugMode();
    }

    private void printDebug(@NotNull String message) {
        printDebug(message, null);
    }

    private void printDebug(@NotNull String message, @Nullable Throwable throwable) {
        if (isDebugModeDisabled()) {
            return;
        }

        Logger logger = getLogger();
        String logMessage = String.format(Locale.US, "[Debug] %s", message);
        if (throwable == null) {
            logger.info(logMessage);
        } else {
            logger.log(Level.WARNING, message, throwable);
        }
    }
}
