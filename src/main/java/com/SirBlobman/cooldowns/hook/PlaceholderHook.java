package com.SirBlobman.cooldowns.hook;

import java.text.DecimalFormat;

import com.SirBlobman.cooldowns.CooldownPlugin;
import com.SirBlobman.cooldowns.manager.CooldownManager;
import com.SirBlobman.cooldowns.object.CooldownInfo;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public interface PlaceholderHook {
    CooldownPlugin getCooldownPlugin();
    
    default String getPlaceholder(Player player, String id) {
        switch(id) {
            case "ender_pearl": return getTimeLeftEnderPearl(player);
            case "golden_apple": return getTimeLeftGoldenApple(player);
        
            default: break;
        }
        
        return null;
    }
    
    default CooldownInfo getCooldownInfo(Player player) {
        CooldownPlugin cooldownPlugin = getCooldownPlugin();
        CooldownManager cooldownManager = cooldownPlugin.getCooldownManager();
        return cooldownManager.getCooldownInfo(player);
    }
    
    default String getTimeLeftEnderPearl(Player player) {
        CooldownInfo cooldownInfo = getCooldownInfo(player);
        long expireTime = cooldownInfo.getEnderPearlExpireTime();
        return getTimeLeft(expireTime);
    }
    
    default String getTimeLeftGoldenApple(Player player) {
        CooldownInfo cooldownInfo = getCooldownInfo(player);
        long expireTime = cooldownInfo.getGoldenAppleExpireTime();
        return getTimeLeft(expireTime);
    }
    
    default String getTimeLeft(long expireMillis) {
        long systemMillis = System.currentTimeMillis();
        double timeLeftMillis = (expireMillis - systemMillis);
        double timeLeftSeconds = (timeLeftMillis / 1_000.0D);
        if(timeLeftSeconds <= 0.0D) timeLeftSeconds = 0.0D;
    
        YamlConfiguration config = getCooldownPlugin().getConfig();
        String decimalFormatString = config.getString("decimal-format", "0.000");
        if(decimalFormatString == null) decimalFormatString = "0.000";
    
        DecimalFormat decimalFormat = new DecimalFormat(decimalFormatString);
        return decimalFormat.format(timeLeftSeconds);
    }
}