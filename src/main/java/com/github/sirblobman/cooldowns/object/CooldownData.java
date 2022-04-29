package com.github.sirblobman.cooldowns.object;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;

import com.github.sirblobman.api.nms.MultiVersionHandler;
import com.github.sirblobman.api.nms.PlayerHandler;
import com.github.sirblobman.api.utility.Validate;
import com.github.sirblobman.api.xseries.XMaterial;
import com.github.sirblobman.cooldowns.CooldownPlugin;
import com.github.sirblobman.cooldowns.manager.CooldownManager;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class CooldownData {
    private final UUID playerId;
    private final Map<XMaterial, Long> cooldownMap;

    public CooldownData(OfflinePlayer player) {
        Validate.notNull(player, "player must not be null!");
        this.playerId = player.getUniqueId();
        this.cooldownMap = new EnumMap<>(XMaterial.class);
    }

    @NotNull
    public UUID getPlayerId() {
        return this.playerId;
    }

    @NotNull
    public OfflinePlayer getOfflinePlayer() {
        UUID uuid = getPlayerId();
        return Bukkit.getOfflinePlayer(uuid);
    }

    @Nullable
    public Player getPlayer() {
        OfflinePlayer offlinePlayer = getOfflinePlayer();
        return offlinePlayer.getPlayer();
    }

    public Set<XMaterial> getActiveCooldowns(CooldownPlugin plugin) {
        Set<XMaterial> materialSet = this.cooldownMap.keySet();
        Set<XMaterial> activeSet = new HashSet<>();

        for (XMaterial material : materialSet) {
            if (hasCooldown(plugin, material)) {
                activeSet.add(material);
            }
        }

        return activeSet;
    }

    public boolean hasCooldown(CooldownPlugin plugin, XMaterial material) {
        if (!this.cooldownMap.containsKey(material)) {
            return false;
        }

        long expireMillis = getCooldownExpireTime(material);
        if (expireMillis <= 0) {
            return false;
        }

        long systemMillis = System.currentTimeMillis();
        if (systemMillis <= expireMillis) {
            return true;
        }

        removeCooldown(plugin, material);
        return false;
    }

    public long getCooldownExpireTime(XMaterial material) {
        return this.cooldownMap.getOrDefault(material, -1L);
    }

    public void setCooldown(CooldownPlugin plugin, XMaterial material, long expireMillis) {
        Validate.notNull(plugin, "plugin must not be null!");
        Validate.notNull(material, "material must not be null!");
        this.cooldownMap.put(material, expireMillis);

        long systemMillis = System.currentTimeMillis();
        long millisLeft = (expireMillis - systemMillis);
        int ticksLeft = (int) (millisLeft / 50L);

        CooldownManager cooldownManager = plugin.getCooldownManager();
        CooldownSettings cooldownSettings = cooldownManager.getCooldownSettings(material);
        if (cooldownSettings.hasPacketCooldown()) {
            sendPacket(plugin, material, ticksLeft);
        }
    }

    public void removeCooldown(CooldownPlugin plugin, XMaterial material) {
        Validate.notNull(plugin, "plugin must not be null!");
        Validate.notNull(material, "material must not be null!");
        this.cooldownMap.remove(material);

        CooldownManager cooldownManager = plugin.getCooldownManager();
        CooldownSettings cooldownSettings = cooldownManager.getCooldownSettings(material);
        if (cooldownSettings.hasPacketCooldown()) {
            sendPacket(plugin, material, 0);
        }
    }

    private void sendPacket(CooldownPlugin plugin, XMaterial material, int ticksLeft) {
        Player player = getPlayer();
        if (player == null) {
            return;
        }

        Material bukkitMaterial = material.parseMaterial();
        if (bukkitMaterial == null) {
            return;
        }

        Runnable task = () -> {
            MultiVersionHandler multiVersionHandler = plugin.getMultiVersionHandler();
            PlayerHandler playerHandler = multiVersionHandler.getPlayerHandler();
            playerHandler.sendCooldownPacket(player, bukkitMaterial, ticksLeft);
        };

        BukkitScheduler scheduler = Bukkit.getScheduler();
        scheduler.scheduleSyncDelayedTask(plugin, task, 1L);
    }
}
