package com.SirBlobman.enderpearl.cooldown.hook.placeholder;

import com.SirBlobman.enderpearl.cooldown.EnderpearlCooldown;
import com.SirBlobman.enderpearl.cooldown.api.ECooldownAPI;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;

public class PlaceholderHook {
    private final EnderpearlCooldown plugin;
    public PlaceholderHook(EnderpearlCooldown plugin) {
        this.plugin = plugin;
    }

    public EnderpearlCooldown getPlugin() {
        return this.plugin;
    }

    public final String getPlaceholder(Player player, String id) {
        if(player == null || id == null) return null;

        switch(id) {
            case "time_left": return getTimeLeft(player);
            case "time_left_decimal": return getTimeLeftDecimal(player);
            default: return null;
        }
    }

    private String getTimeLeft(Player player) {
        int timeLeft = ECooldownAPI.getTimeLeftSeconds(player);
        if(timeLeft <= 0) return this.plugin.getConfigMessage("action bar.end timer");

        return Integer.toString(timeLeft);
    }

    private String getTimeLeftDecimal(Player player) {
        double timeLeftMillis = ECooldownAPI.getTimeLeftMillis(player);
        if(timeLeftMillis <= 0.0D) return this.plugin.getConfigMessage("action bar.end timer");

        double timeLeftSeconds = (timeLeftMillis / 1000.0D);
        DecimalFormat format = new DecimalFormat("0.0");
        return format.format(timeLeftSeconds);
    }
}