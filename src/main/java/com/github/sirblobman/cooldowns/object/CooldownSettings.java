package com.github.sirblobman.cooldowns.object;

import java.util.concurrent.TimeUnit;

import org.bukkit.inventory.ItemStack;

import com.github.sirblobman.api.utility.ItemUtility;
import com.github.sirblobman.api.utility.Validate;
import com.github.sirblobman.api.xseries.XMaterial;

import org.jetbrains.annotations.NotNull;

public final class CooldownSettings {
    private final XMaterial material;
    private final CooldownType cooldownType;
    private final int cooldownSeconds;
    private final String bypassPermission;
    private final boolean packetCooldown;
    private final ActionBarSettings actionBarSettings;

    public CooldownSettings(XMaterial material, CooldownType cooldownType, int cooldownSeconds, String bypassPermission, boolean packetCooldown, ActionBarSettings actionBarSettings) {
        this.material = Validate.notNull(material, "material must not be null!");
        if(this.material == XMaterial.AIR) throw new IllegalArgumentException("material must not be AIR!");

        this.cooldownType = Validate.notNull(cooldownType, "cooldownType must not be null!");
        this.cooldownSeconds = Math.max(0, cooldownSeconds);
        this.bypassPermission = bypassPermission;
        this.packetCooldown = packetCooldown;
        this.actionBarSettings = actionBarSettings;
    }

    public static CooldownSettings getDefaultCooldownSettings(XMaterial material) {
        return new CooldownSettings(material, CooldownType.INTERACT, 0, null,
                false, null);
    }

    @NotNull
    public XMaterial getMaterial() {
        return this.material;
    }

    @NotNull
    public CooldownType getCooldownType() {
        return this.cooldownType;
    }

    public int getCooldownSeconds() {
        return this.cooldownSeconds;
    }

    public String getBypassPermission() {
        return this.bypassPermission;
    }

    public long getCooldownMillis() {
        return TimeUnit.SECONDS.toMillis(getCooldownSeconds());
    }

    public boolean hasPacketCooldown() {
        return this.packetCooldown;
    }

    @NotNull
    public ActionBarSettings getActionBarSettings() {
        if(this.actionBarSettings != null) return this.actionBarSettings;
        return ActionBarSettings.getDefaultActionBarSettings();
    }

    public boolean matches(ItemStack item) {
        if(ItemUtility.isAir(item)) return false;
        XMaterial material = XMaterial.matchXMaterial(item);
        return (material == getMaterial());
    }
}
