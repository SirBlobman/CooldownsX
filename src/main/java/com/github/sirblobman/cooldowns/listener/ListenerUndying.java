package com.github.sirblobman.cooldowns.listener;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import com.github.sirblobman.api.configuration.ConfigurationManager;
import com.github.sirblobman.api.utility.MessageUtility;
import com.github.sirblobman.cooldowns.CooldownPlugin;
import com.github.sirblobman.cooldowns.manager.UndyingManager;

public final class ListenerUndying extends CooldownListener {
    public ListenerUndying(CooldownPlugin plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onResurrect(EntityResurrectEvent e) {
        LivingEntity entity = e.getEntity();
        if (!(entity instanceof Player)) {
            return;
        }

        Player player = (Player) entity;
        if (hasBypass(player)) {
            return;
        }

        UndyingManager undyingManager = getUndyingManager();
        if (!undyingManager.hasCooldown(player)) {
            undyingManager.addCooldown(player);
            return;
        }

        e.setCancelled(true);
        sendMessage(player);
    }

    private void sendMessage(Player player) {
        CooldownPlugin plugin = getPlugin();
        ConfigurationManager configurationManager = plugin.getConfigurationManager();
        YamlConfiguration configuration = configurationManager.get("undying.yml");

        String message = configuration.getString("totem-message");
        if (message == null || message.isEmpty()) {
            return;
        }

        String timeLeft = getTimeLeft(player);
        String realMessage = MessageUtility.color(message).replace("{time_left}", timeLeft);
        player.sendMessage(realMessage);
    }

    private String getTimeLeft(Player player) {
        UndyingManager undyingManager = getUndyingManager();
        double millisLeft = undyingManager.getCooldownMillisLeft(player);
        long timeLeftSeconds = (long) Math.ceil(millisLeft / 1_000.0D);
        return Long.toString(timeLeftSeconds);
    }

    private boolean hasBypass(Player player) {
        CooldownPlugin plugin = getPlugin();
        ConfigurationManager configurationManager = plugin.getConfigurationManager();
        YamlConfiguration configuration = configurationManager.get("undying.yml");

        String bypassPermissionName = configuration.getString("totem-bypass-permission");
        if (bypassPermissionName == null || bypassPermissionName.isEmpty()) {
            return false;
        }

        Permission bypassPermission = new Permission(bypassPermissionName, "CooldownsX Bypass Permission", PermissionDefault.FALSE);
        return player.hasPermission(bypassPermission);
    }
}
