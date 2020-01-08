package com.SirBlobman.enderpearl.cooldown.hook;

import java.text.DecimalFormat;

import com.SirBlobman.enderpearl.cooldown.EnderpearlCooldown;
import com.SirBlobman.enderpearl.cooldown.utility.EnderpearlCooldownManager;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;

public interface PlaceholderHook {
    default String getPlaceholder(EnderpearlCooldown plugin, Player player, String id) {
        if(plugin == null || player == null || id == null || id.isEmpty()) return null;

        switch(id) {
            case "time_left": return getTimeLeft(plugin, player);
            case "time_left_decimal": return getTimeLeftDecimal(plugin, player);

            default: return null;
        }
    }

    default String getTimeLeft(EnderpearlCooldown plugin, Player player) {
        long secondsLeft = EnderpearlCooldownManager.getTimeLeftSeconds(player);
        if(secondsLeft <= 0) return getZeroTimeLeft(plugin, player);

        return Long.toString(secondsLeft);
    }

    default String getTimeLeftDecimal(EnderpearlCooldown plugin, Player player) {
        double millisLeft = EnderpearlCooldownManager.getTimeLeftMillis(player);
        if(millisLeft <= 0) return getZeroTimeLeft(plugin, player);

        double secondsLeft = (millisLeft / 1_000.0D);
        DecimalFormat format = new DecimalFormat("0.0");

        return format.format(secondsLeft);
    }

    default String getZeroTimeLeft(EnderpearlCooldown plugin, Player player) {
        String message = plugin.getConfigMessage("messages.zero-time-left");
        return (message == null || message.isEmpty() ? "0" : message);
    }

    boolean register();
}