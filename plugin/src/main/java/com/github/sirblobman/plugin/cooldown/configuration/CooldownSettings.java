package com.github.sirblobman.plugin.cooldown.configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import com.github.sirblobman.api.utility.Validate;
import com.github.sirblobman.combatlogx.api.ICombatLogX;
import com.github.sirblobman.combatlogx.api.manager.ICombatManager;
import com.github.sirblobman.plugin.cooldown.api.configuration.CombatMode;
import com.github.sirblobman.plugin.cooldown.api.configuration.CooldownType;
import com.github.sirblobman.plugin.cooldown.api.configuration.Cooldown;
import com.github.sirblobman.api.shaded.xseries.XMaterial;
import com.github.sirblobman.api.shaded.xseries.XPotion;

public final class CooldownSettings implements Cooldown {
    private final String id;

    private int amount;
    private boolean resetAmount;
    private int cooldownSeconds;
    private CooldownType cooldownType;

    private List<XMaterial> materialList;
    private List<XPotion> potionList;
    private List<EntityType> entityList;

    private String bypassPermissionName;
    private transient Permission bypassPermission;
    private boolean usePacketCooldown;

    private CombatMode combatMode;
    private int combatCooldownSeconds;

    private List<String> disabledWorldList;
    private boolean invertWorldList;

    private ActionBarSettings actionBarSettings;

    private String messageFormat;

    public CooldownSettings(@NotNull String id) {
        this.id = id;

        this.amount = 1;
        this.resetAmount = false;
        this.cooldownSeconds = 10;
        this.cooldownType = CooldownType.INTERACT_ITEM;

        this.materialList = null;
        this.potionList = null;
        this.entityList = null;

        this.bypassPermissionName = null;
        this.bypassPermission = null;
        this.usePacketCooldown = false;

        this.combatMode = CombatMode.IGNORE;
        this.combatCooldownSeconds = cooldownSeconds;

        this.disabledWorldList = new ArrayList<>();
        this.invertWorldList = false;

        this.actionBarSettings = new ActionBarSettings();

        this.messageFormat = null;
    }

    @Override
    public @NotNull String getId() {
        return this.id;
    }

    @Override
    public int getAmount() {
        return this.amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    @Override
    public int getCooldownSeconds() {
        return this.cooldownSeconds;
    }

    public void setCooldownSeconds(int cooldownSeconds) {
        this.cooldownSeconds = cooldownSeconds;

        CombatMode combatMode = getCombatMode();
        if (combatMode != CombatMode.DIFFERENT) {
            this.combatCooldownSeconds = cooldownSeconds;
        }
    }

    @Override
    public @NotNull CooldownType getCooldownType() {
        return this.cooldownType;
    }

    public void setCooldownType(@NotNull CooldownType cooldownType) {
        this.cooldownType = cooldownType;
    }

    @Override
    public @NotNull Optional<List<XMaterial>> getMaterialList() {
        return Optional.ofNullable(this.materialList);
    }

    public void setMaterialList(@Nullable List<XMaterial> materialList) {
        this.materialList = materialList;
    }

    @Override
    public @NotNull Optional<List<XPotion>> getPotionList() {
        return Optional.ofNullable(this.potionList);
    }

    public void setPotionList(@Nullable List<XPotion> potionList) {
        this.potionList = potionList;
    }

    @Override
    public @NotNull Optional<List<EntityType>> getEntityList() {
        return Optional.ofNullable(this.entityList);
    }

    public void setEntityList(@Nullable List<EntityType> entityList) {
        this.entityList = entityList;
    }

    @Override
    public @Nullable String getBypassPermissionName() {
        return this.bypassPermissionName;
    }

    public void setBypassPermissionName(@Nullable String bypassPermissionName) {
        this.bypassPermissionName = bypassPermissionName;
        this.bypassPermission = null;
    }

    @Override
    public @Nullable Permission getBypassPermission() {
        if (this.bypassPermission != null) {
            return this.bypassPermission;
        }

        String bypassPermissionName = getBypassPermissionName();
        if (bypassPermissionName == null || bypassPermissionName.isEmpty()) {
            return null;
        }

        String description = "Custom CooldownsX Permission";
        this.bypassPermission = new Permission(bypassPermissionName, description, PermissionDefault.FALSE);
        return this.bypassPermission;
    }

    @Override
    public boolean canBypass(@NotNull Permissible permissible) {
        Permission permission = getBypassPermission();
        if (permission == null) {
            return false;
        }

        return permissible.hasPermission(permission);
    }

    @Override
    public boolean isUsePacketCooldown() {
        return this.usePacketCooldown;
    }

    public void setUsePacketCooldown(boolean usePacketCooldown) {
        this.usePacketCooldown = usePacketCooldown;
    }

    public @NotNull CombatMode getCombatMode() {
        return this.combatMode;
    }

    public void setCombatMode(@NotNull CombatMode combatMode) {
        this.combatMode = combatMode;
    }

    public int getCombatCooldownSeconds() {
        return this.combatCooldownSeconds;
    }

    public void setCombatCooldownSeconds(int combatCooldownSeconds) {
        CombatMode combatMode = getCombatMode();
        if (combatMode == CombatMode.DIFFERENT) {
            this.combatCooldownSeconds = combatCooldownSeconds;
        }
    }

    @Override
    public @NotNull List<String> getDisabledWorldList() {
        return this.disabledWorldList;
    }

    public void setDisabledWorldList(List<String> disabledWorldList) {
        Validate.notNull(disabledWorldList, "disabledWorldList must not be null!");
        this.disabledWorldList = disabledWorldList;
    }

    @Override
    public boolean isInvertWorldList() {
        return this.invertWorldList;
    }

    public void setInvertWorldList(boolean invertWorldList) {
        this.invertWorldList = invertWorldList;
    }

    @Override
    public boolean isDisabled(@NotNull World world) {
        String worldName = world.getName();
        List<String> disabledWorldList = getDisabledWorldList();
        boolean contains = disabledWorldList.contains(worldName);
        boolean invert = isInvertWorldList();
        return (invert != contains);
    }

    @Override
    public @NotNull ActionBarSettings getActionBarSettings() {
        return this.actionBarSettings;
    }

    public void setActionBarSettings(@NotNull ActionBarSettings actionBarSettings) {
        this.actionBarSettings = actionBarSettings;
    }

    @Override
    public int getCooldownSeconds(@NotNull Player player) {
        CombatMode combatMode = getCombatMode();
        if (combatMode == CombatMode.IGNORE || isCombatLogXMissing()) {
            return getCooldownSeconds();
        }

        PluginManager pluginManager = Bukkit.getPluginManager();
        Plugin pluginCombatLogX = pluginManager.getPlugin("CombatLogX");
        if (pluginCombatLogX == null) {
            throw new IllegalStateException("CombatLogX is null but the plugin is enabled?");
        }

        ICombatLogX combatLogX = (ICombatLogX) pluginCombatLogX;
        ICombatManager combatManager = combatLogX.getCombatManager();
        boolean combat = combatManager.isInCombat(player);

        switch (combatMode) {
            case TRUE:
                return (combat ? getCooldownSeconds() : 0);
            case FALSE:
                return (combat ? 0 : getCooldownSeconds());
            case DIFFERENT:
                return (combat ? getCombatCooldownSeconds() : getCooldownSeconds());
            default:
                break;
        }

        return 0;
    }

    private boolean isCombatLogXMissing() {
        PluginManager pluginManager = Bukkit.getPluginManager();
        return !pluginManager.isPluginEnabled("CombatLogX");
    }

    @Override
    public boolean hasMaterial(@NotNull XMaterial material) {
        Optional<List<XMaterial>> optionalMaterialList = getMaterialList();
        if (optionalMaterialList.isPresent()) {
            List<XMaterial> materialList = optionalMaterialList.get();
            return materialList.contains(material);
        }

        return false;
    }

    @Override
    public boolean hasPotion(@NotNull XPotion potion) {
        Optional<List<XPotion>> optionalPotionList = getPotionList();
        if (optionalPotionList.isPresent()) {
            List<XPotion> potionList = optionalPotionList.get();
            return potionList.contains(potion);
        }

        return false;
    }

    @Override
    public boolean hasEntity(@NotNull EntityType entityType) {
        Optional<List<EntityType>> optionalEntityList = getEntityList();
        if (optionalEntityList.isPresent()) {
            List<EntityType> entityList = optionalEntityList.get();
            return entityList.contains(entityType);
        }

        return false;
    }

    @Override
    public @Nullable String getMessageFormat() {
        return this.messageFormat;
    }

    public void setMessageFormat(@Nullable String messageFormat) {
        this.messageFormat = messageFormat;
    }

    @Override
    public boolean isResetAmount() {
        return this.resetAmount;
    }

    public void setResetAmount(boolean resetAmount) {
        this.resetAmount = resetAmount;
    }

    @Override
    public @NotNull String toString() {
        String id = getId();
        return String.format(Locale.US, "CooldownSettings{%s}", id);
    }

    @Override
    public int hashCode() {
        String id = getId();
        return Objects.hashCode(id);
    }

    @Override
    public boolean equals(@Nullable Object object) {
        if (super.equals(object)) {
            return true;
        }

        if (!(object instanceof CooldownSettings)) {
            return false;
        }

        CooldownSettings other = (CooldownSettings) object;
        String id1 = this.getId();
        String id2 = other.getId();
        return Objects.equals(id1, id2);
    }
}
