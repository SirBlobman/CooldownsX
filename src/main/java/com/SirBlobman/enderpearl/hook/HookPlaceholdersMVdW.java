package com.SirBlobman.enderpearl.hook;

import org.bukkit.entity.Player;

import com.SirBlobman.enderpearl.EnderpearlCooldown;
import com.SirBlobman.enderpearl.config.ConfigSettings;
import com.SirBlobman.enderpearl.utility.ECooldownUtil;
import com.SirBlobman.enderpearl.utility.Util;

import java.text.DecimalFormat;

import be.maximvdw.placeholderapi.PlaceholderAPI;
import be.maximvdw.placeholderapi.PlaceholderReplaceEvent;
import be.maximvdw.placeholderapi.PlaceholderReplacer;

public class HookPlaceholdersMVdW implements PlaceholderReplacer {
    private final EnderpearlCooldown plugin;
    public HookPlaceholdersMVdW(EnderpearlCooldown plugin) {
        this.plugin = plugin;
    }
    
    public void register() {
        PlaceholderAPI.registerPlaceholder(plugin, "ec_time_left", this);
        PlaceholderAPI.registerPlaceholder(plugin, "ec_time_left_decimal", this);
    }
    
    @Override
    public String onPlaceholderReplace(PlaceholderReplaceEvent e) {
        String placeholder = e.getPlaceholder();
        if(!placeholder.startsWith("ec_")) return null;
        
        String id = placeholder.substring(3);
        Player player = e.getPlayer();
        
        if(id.equals("time_left")) {
            int timeLeft = ECooldownUtil.getSecondsLeft(player);
            if(timeLeft <= 0) {
                String message = Util.color(ConfigSettings.getOption("messages.zero seconds", "&aReady"));
                return message;
            }
            
            return Integer.toString(timeLeft);
        }
        
        if(id.equals("time_left_decimal")) {
            double timeLeftMillis = ECooldownUtil.getMillisLeft(player);
            double timeLeftSeconds = (timeLeftMillis / 1000.0D);
            if(timeLeftSeconds <= 0.0D) {
                String message = Util.color(ConfigSettings.getOption("messages.zero seconds", "&aReady"));
                return message;
            }
            
            DecimalFormat format = new DecimalFormat("0.0");
            return format.format(timeLeftSeconds);
        }
        
        return null;
    }
}