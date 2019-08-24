package com.SirBlobman.enderpearl.hook;

import org.bukkit.entity.Player;

import com.SirBlobman.enderpearl.EnderpearlCooldown;
import com.SirBlobman.enderpearl.config.ConfigSettings;
import com.SirBlobman.enderpearl.utility.ECooldownUtil;
import com.SirBlobman.enderpearl.utility.Util;

import java.text.DecimalFormat;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;

public class HookPlaceholdersClip extends PlaceholderExpansion {
    public boolean persist() {return true;}
    public String getIdentifier() {return "ec";}
    public String getPlugin() {return null;}
    public String getAuthor() {return "SirBlobman";}
    public String getVersion() {return EnderpearlCooldown.INSTANCE.getDescription().getVersion();}
    
    @Override
    public String onPlaceholderRequest(Player player, String id) {
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