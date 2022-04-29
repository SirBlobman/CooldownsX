package com.github.sirblobman.cooldowns.object;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import com.github.sirblobman.api.utility.ItemUtility;
import com.github.sirblobman.api.utility.Validate;
import com.github.sirblobman.api.xseries.XMaterial;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class CooldownSettings {
    private final XMaterial material;
    private CooldownType cooldownType;
    private int cooldownSeconds;
    private String bypassPermission;
    private boolean packetCooldown;
    private List<String> disabledWorldNameList;
    private boolean disabledWorldNameListInverted;
    private ActionBarSettings actionBarSettings;

    public CooldownSettings(XMaterial material) {
        this.material = Validate.notNull(material, "material must not be null!");
        if(this.material == XMaterial.AIR) {
            throw new IllegalArgumentException("material must not be AIR!");
        }

        this.cooldownType = CooldownType.INTERACT;
        this.cooldownSeconds = 0;
        this.bypassPermission = null;
        this.packetCooldown = false;
        this.disabledWorldNameList = new ArrayList<>();
        this.disabledWorldNameListInverted = false;

        this.actionBarSettings = new ActionBarSettings();
    }

    public void load(ConfigurationSection config) {
        String cooldownTypeName = config.getString("cooldown-type");
        try {
            CooldownType cooldownType = CooldownType.valueOf(cooldownTypeName);
            setCooldownType(cooldownType);
        } catch(IllegalArgumentException ignored) {
        }

        int cooldownSeconds = config.getInt("cooldown", 0);
        setCooldownSeconds(cooldownSeconds);

        String bypassPermission = config.getString("bypass-permission");
        setBypassPermission(bypassPermission);

        boolean packetCooldown = config.getBoolean("packet-cooldown", false);
        setPacketCooldown(packetCooldown);

        List<String> disabledWorldNameList = config.getStringList("disabled-world-list");
        setDisabledWorldNames(disabledWorldNameList);

        boolean disabledWorldNameListInverted = config.getBoolean("disabled-world-list-inverted", false);
        setDisabledWorldListInverted(disabledWorldNameListInverted);

        ConfigurationSection sectionActionBar = config.getConfigurationSection("action-bar");
        if(sectionActionBar != null) {
            boolean enabled = sectionActionBar.getBoolean("enabled", false);
            int priority = sectionActionBar.getInt("priority", 0);
            String message = sectionActionBar.getString("message");

            ActionBarSettings actionBarSettings = new ActionBarSettings(enabled, priority, message);
            setActionBarSettings(actionBarSettings);
        }
    }

    @Deprecated
    public static CooldownSettings getDefaultCooldownSettings(XMaterial material) {
        return new CooldownSettings(material);
    }
    
    @NotNull
    public XMaterial getMaterial() {
        return this.material;
    }
    
    @NotNull
    public CooldownType getCooldownType() {
        return this.cooldownType;
    }

    public void setCooldownType(CooldownType cooldownType) {
        Validate.notNull(cooldownType, "cooldownType must not be null!");
        this.cooldownType = cooldownType;
    }
    
    public int getCooldownSeconds() {
        return this.cooldownSeconds;
    }

    public long getCooldownMillis() {
        int seconds = getCooldownSeconds();
        return TimeUnit.SECONDS.toMillis(seconds);
    }

    public void setCooldownSeconds(int cooldownSeconds) {
        this.cooldownSeconds = cooldownSeconds;
    }

    @Nullable
    public String getBypassPermission() {
        return this.bypassPermission;
    }

    public void setBypassPermission(@Nullable String bypassPermission) {
        this.bypassPermission = bypassPermission;
    }
    
    public boolean hasPacketCooldown() {
        return this.packetCooldown;
    }

    public void setPacketCooldown(boolean packetCooldown) {
        this.packetCooldown = packetCooldown;
    }

    public List<String> getDisabledWorldNames() {
        return Collections.unmodifiableList(this.disabledWorldNameList);
    }

    public void setDisabledWorldNames(Collection<String> worldNames) {
        Validate.notNull(worldNames, "worldNames must not be null!");

        this.disabledWorldNameList.clear();
        this.disabledWorldNameList.addAll(worldNames);
    }

    public boolean isDisabledWorldNameListInverted() {
        return this.disabledWorldNameListInverted;
    }

    public void setDisabledWorldListInverted(boolean inverted) {
        this.disabledWorldNameListInverted = inverted;
    }

    public boolean isDisabledWorld(World world) {
        Validate.notNull(world, "world must not be null!");
        String worldName = world.getName();

        List<String> disabledWorldNameList = getDisabledWorldNames();
        boolean contains = disabledWorldNameList.contains(worldName);
        boolean inverted = isDisabledWorldNameListInverted();
        return (contains != inverted);
    }
    
    @NotNull
    public ActionBarSettings getActionBarSettings() {
        return this.actionBarSettings;
    }

    public void setActionBarSettings(@Nullable ActionBarSettings settings) {
        if(settings != null) {
            this.actionBarSettings = settings;
        } else {
            this.actionBarSettings = new ActionBarSettings();
        }
    }
    
    public boolean matches(ItemStack item) {
        if(ItemUtility.isAir(item)) {
            return false;
        }

        XMaterial thisMaterial = getMaterial();
        XMaterial itemMaterial = XMaterial.matchXMaterial(item);
        return (thisMaterial == itemMaterial);
    }
}
