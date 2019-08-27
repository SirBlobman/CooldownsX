package com.SirBlobman.enderpearl.cooldown.api;

import com.SirBlobman.api.utility.Util;
import com.SirBlobman.enderpearl.cooldown.EnderpearlCooldown;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class ECooldownAPI {
    private static final Map<UUID, Long> cooldownMap = Util.newMap();

    private static EnderpearlCooldown getPlugin() {
        return JavaPlugin.getPlugin(EnderpearlCooldown.class);
    }

    private static List<String> getMatchingPermissions(Player player) {
        if(player == null) return Util.newList();

        List<String> permissionList = Util.newList();
        Set<PermissionAttachmentInfo> permissionAttachmentSet = player.getEffectivePermissions();
        for(PermissionAttachmentInfo info : permissionAttachmentSet) {
            if(!info.getValue()) continue;

            String permission = info.getPermission();
            if(permission.startsWith("enderpearl.cooldown.timer.")) permissionList.add(permission);
        }
        return permissionList;
    }

    private static int getNormalSecondsToAdd() {
        EnderpearlCooldown plugin = getPlugin();
        FileConfiguration config = plugin.getConfig();

        return config.getInt("default timer");
    }

    private static int getPermissionBasedSecondsToAdd(Player player) {
        int cooldownSeconds = getNormalSecondsToAdd();

        List<String> permissionList = getMatchingPermissions(player);
        for(String permission : permissionList) {
            String secondsString = permission.substring("enderpearl.cooldown.timer.".length());
            try {
                int seconds = Integer.parseInt(secondsString);
                if(seconds < cooldownSeconds) {
                    cooldownSeconds = seconds;
                }
            } catch(NumberFormatException ignored) {}
        }

        return cooldownSeconds;
    }

    private static int getSecondsToAdd(Player player) {
        EnderpearlCooldown plugin = getPlugin();
        FileConfiguration config = plugin.getConfig();

        String mode = config.getString("cooldown mode");
        if(mode.equals("PERMISSION_BASED")) return getPermissionBasedSecondsToAdd(player);
        return getNormalSecondsToAdd();
    }

    public static boolean canBypass(Player player) {
        if(player == null) return false;

        EnderpearlCooldown plugin = getPlugin();
        FileConfiguration config = plugin.getConfig();
        String bypassPermission = config.getString("bypass permission");
        return player.hasPermission(bypassPermission);
    }

    public static boolean isInCooldown(Player player) {
        if(player == null) return false;

        UUID uuid = player.getUniqueId();
        return cooldownMap.containsKey(uuid);
    }

    public static void addToCooldown(Player player) {
        if(player == null) return;

        UUID uuid = player.getUniqueId();
        int secondsToAdd = getSecondsToAdd(player);
        long millisToAdd = (secondsToAdd * 1000L);
        long systemMillis = System.currentTimeMillis();
        long expireTime = (systemMillis + millisToAdd);

        cooldownMap.put(uuid, expireTime);
    }

    public static void removeFromCooldown(Player player) {
        if(player == null) return;

        UUID uuid = player.getUniqueId();
        cooldownMap.remove(uuid);
    }

    public static long getTimeLeftMillis(Player player) {
        if(!isInCooldown(player)) return 0L;

        UUID uuid = player.getUniqueId();
        long expireTime = cooldownMap.get(uuid);
        long systemTime = System.currentTimeMillis();
        return (expireTime - systemTime);
    }

    public static int getTimeLeftSeconds(Player player) {
        long timeLeftMillis = getTimeLeftMillis(player);
        long timeLeftSeconds = (timeLeftMillis / 1000L);
        return (int) timeLeftSeconds;
    }
}