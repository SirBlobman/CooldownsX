package com.github.sirblobman.cooldowns.api.configuration;

import java.util.List;
import java.util.Optional;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.Permission;

import com.github.sirblobman.api.xseries.XMaterial;
import com.github.sirblobman.api.xseries.XPotion;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ICooldownSettings {
    @NotNull String getId();

    int getAmount();

    int getCooldownSeconds();

    @NotNull CooldownType getCooldownType();

    @NotNull Optional<List<XMaterial>> getMaterialList();

    @NotNull Optional<List<XPotion>> getPotionList();

    @Nullable String getBypassPermissionName();

    @Nullable Permission getBypassPermission();

    boolean isUsePacketCooldown();

    @NotNull CombatMode getCombatMode();

    int getCombatCooldownSeconds();

    @NotNull List<String> getDisabledWorldList();

    boolean isInvertWorldList();

    @NotNull IActionBarSettings getActionBarSettings();

    @Nullable String getMessageFormat();

    boolean isResetAmount();

    int getCooldownSeconds(Player player);

    boolean hasMaterial(XMaterial material);

    boolean hasPotion(XPotion potion);

    boolean isDisabled(World world);

    boolean canBypass(Permissible permissible);
}
