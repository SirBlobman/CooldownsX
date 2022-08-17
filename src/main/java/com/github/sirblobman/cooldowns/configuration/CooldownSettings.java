package com.github.sirblobman.cooldowns.configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;

import com.github.sirblobman.api.utility.Validate;
import com.github.sirblobman.api.xseries.XMaterial;
import com.github.sirblobman.api.xseries.XPotion;
import com.github.sirblobman.combatlogx.api.ICombatLogX;
import com.github.sirblobman.combatlogx.api.manager.ICombatManager;
import com.github.sirblobman.cooldowns.object.CombatMode;
import com.github.sirblobman.cooldowns.object.CooldownType;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class CooldownSettings {
    private final String id;

    private int amount;
    private boolean resetAmount;
    private int cooldownSeconds;
    private CooldownType cooldownType;

    private List<XMaterial> materialList;
    private List<XPotion> potionList;

    private String bypassPermissionName;
    private transient Permission bypassPermission;
    private boolean usePacketCooldown;

    private CombatMode combatMode;
    private int combatCooldownSeconds;

    private List<String> disabledWorldList;
    private boolean invertWorldList;

    private ActionBarSettings actionBarSettings;

    private String messageFormat;

    public CooldownSettings(String id) {
        this.id = Validate.notEmpty(id, "id must not be empty!");

        this.amount = 1;
        this.resetAmount = false;
        this.cooldownSeconds = 10;
        this.cooldownType = CooldownType.INTERACT_ITEM;

        this.materialList = null;
        this.potionList = null;

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

    @NotNull
    public String getId() {
        return this.id;
    }

    public int getAmount() {
        return this.amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

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

    public CooldownType getCooldownType() {
        return cooldownType;
    }

    public void setCooldownType(CooldownType cooldownType) {
        this.cooldownType = cooldownType;
    }

    public Optional<List<XMaterial>> getMaterialList() {
        return Optional.ofNullable(this.materialList);
    }

    public void setMaterialList(@Nullable List<XMaterial> materialList) {
        this.materialList = materialList;
    }

    public Optional<List<XPotion>> getPotionList() {
        return Optional.ofNullable(this.potionList);
    }

    public void setPotionList(@Nullable List<XPotion> potionList) {
        this.potionList = potionList;
    }

    @Nullable
    public String getBypassPermissionName() {
        return bypassPermissionName;
    }

    public void setBypassPermissionName(String bypassPermissionName) {
        this.bypassPermissionName = bypassPermissionName;
        this.bypassPermission = null;
    }

    @Nullable
    public Permission getBypassPermission() {
        if(this.bypassPermission != null) {
            return this.bypassPermission;
        }

        String bypassPermissionName = getBypassPermissionName();
        if(bypassPermissionName == null || bypassPermissionName.isEmpty()) {
            return null;
        }

        String description = "Custom CooldownsX Permission";
        this.bypassPermission = new Permission(bypassPermissionName, description, PermissionDefault.FALSE);
        return this.bypassPermission;
    }

    public boolean canBypass(Permissible permissible) {
        Permission permission = getBypassPermission();
        if(permission == null) {
            return false;
        }

        return permissible.hasPermission(permission);
    }

    public boolean isUsePacketCooldown() {
        return usePacketCooldown;
    }

    public void setUsePacketCooldown(boolean usePacketCooldown) {
        this.usePacketCooldown = usePacketCooldown;
    }

    public CombatMode getCombatMode() {
        return combatMode;
    }

    public void setCombatMode(CombatMode combatMode) {
        this.combatMode = combatMode;
    }

    public int getCombatCooldownSeconds() {
        return combatCooldownSeconds;
    }

    public void setCombatCooldownSeconds(int combatCooldownSeconds) {
        CombatMode combatMode = getCombatMode();
        if (combatMode == CombatMode.DIFFERENT) {
            this.combatCooldownSeconds = combatCooldownSeconds;
        }
    }

    public List<String> getDisabledWorldList() {
        return disabledWorldList;
    }

    public void setDisabledWorldList(List<String> disabledWorldList) {
        Validate.notNull(disabledWorldList, "disabledWorldList must not be null!");
        this.disabledWorldList = disabledWorldList;
    }

    public boolean isInvertWorldList() {
        return invertWorldList;
    }

    public void setInvertWorldList(boolean invertWorldList) {
        this.invertWorldList = invertWorldList;
    }

    public boolean isDisabled(World world) {
        Validate.notNull(world, "world must not be null!");
        String worldName = world.getName();

        List<String> disabledWorldList = getDisabledWorldList();
        boolean contains = disabledWorldList.contains(worldName);
        boolean invert = isInvertWorldList();
        return (invert != contains);
    }

    @NotNull
    public ActionBarSettings getActionBarSettings() {
        return this.actionBarSettings;
    }

    public void setActionBarSettings(ActionBarSettings actionBarSettings) {
        Validate.notNull(actionBarSettings, "actionBarSettings must not be null!");
        this.actionBarSettings = actionBarSettings;
    }

    public int getCooldownSeconds(Player player) {
        CombatMode combatMode = getCombatMode();
        if(combatMode == CombatMode.IGNORE || isCombatLogXMissing()) {
            return getCooldownSeconds();
        }

        PluginManager pluginManager = Bukkit.getPluginManager();
        ICombatLogX combatLogX = (ICombatLogX) pluginManager.getPlugin("ConmbatLogX");
        if(combatLogX == null) {
            throw new IllegalStateException("CombatLogX is null but the plugin is enabled?");
        }

        ICombatManager combatManager = combatLogX.getCombatManager();
        boolean combat = combatManager.isInCombat(player);

        switch(combatMode) {
            case TRUE: return (combat ? getCooldownSeconds() : 0);
            case FALSE: return (combat ? 0 : getCooldownSeconds());
            case DIFFERENT: return (combat ? getCombatCooldownSeconds() : getCooldownSeconds());
            default: break;
        }

        return 0;
    }

    private boolean isCombatLogXMissing() {
        PluginManager pluginManager = Bukkit.getPluginManager();
        return !pluginManager.isPluginEnabled("CombatLogX");
    }

    @Override
    public String toString() {
        String id = getId();
        return String.format(Locale.US, "CooldownSettings{%s}", id);
    }

    @Override
    public int hashCode() {
        String id = getId();
        return Objects.hashCode(id);
    }

    @Override
    public boolean equals(Object object) {
        if(super.equals(object)) {
            return true;
        }

        if(!(object instanceof CooldownSettings)) {
            return false;
        }

        CooldownSettings other = (CooldownSettings) object;
        String id1 = this.getId();
        String id2 = other.getId();
        return Objects.equals(id1, id2);
    }

    public boolean hasMaterial(XMaterial material) {
        Optional<List<XMaterial>> optionalMaterialList = getMaterialList();
        if(optionalMaterialList.isPresent()) {
            List<XMaterial> materialList = optionalMaterialList.get();
            return materialList.contains(material);
        }
        
        return false;
    }
    
    public boolean hasPotion(XPotion potion) {
        Optional<List<XPotion>> optionalPotionList = getPotionList();
        if(optionalPotionList.isPresent()) {
            List<XPotion> potionList = optionalPotionList.get();
            return potionList.contains(potion);
        }

        return false;
    }

    public String getMessageFormat() {
        return messageFormat;
    }

    public void setMessageFormat(String messageFormat) {
        this.messageFormat = messageFormat;
    }

    public boolean isResetAmount() {
        return resetAmount;
    }

    public void setResetAmount(boolean resetAmount) {
        this.resetAmount = resetAmount;
    }
}
