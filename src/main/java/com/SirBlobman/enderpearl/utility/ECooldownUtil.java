package com.SirBlobman.enderpearl.utility;

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.SirBlobman.api.nms.NMS_Handler;
import com.SirBlobman.enderpearl.config.ConfigSettings;

public class ECooldownUtil extends Util implements Runnable {
    @Override
    public void run() {
        Collection<? extends Player> playerList = Bukkit.getOnlinePlayers();
        for(Player player : playerList) {
            if(!isInCooldown(player)) continue;
            
            NMS_Handler nms = NMS_Handler.getHandler();
            int timeLeft = getSecondsLeft(player);
            double timeLeftMillis = ECooldownUtil.getMillisLeft(player);
            double timeLeftSeconds = (timeLeftMillis / 1000.0D);
            DecimalFormat format = new DecimalFormat("0.0");
            
            String timeLeftNormal = Integer.toString(timeLeft);
            String timeLeftDecimal = format.format(timeLeftSeconds);
            
            if(timeLeft > 0) {
                String message = color(ConfigSettings.getOption("messages.action bar", "")).replace("{time_left}", timeLeftNormal).replace("{time_left_decimal}", timeLeftDecimal);
                nms.sendActionBar(player, message);
                continue;
            }
            
            removeFromCooldown(player);
            String message = color(ConfigSettings.getOption("messages.end cooldown", ""));
            nms.sendActionBar(player, message);
        }
    }
    
    public static final Map<UUID, Long> COOLDOWN = Util.newMap();
    
    /**
     * Gets the amount of time left for a player
     * @param player
     */
    public static int getSecondsLeft(Player player) {
        long millisLeft = getMillisLeft(player);
        if(millisLeft <= 0) return 0;
        
        int secondsLeft = (int) (millisLeft / 1000L);
        return secondsLeft;
    }
    
    /**
     * Gets the amount of milliseconds left for a player
     * @param player
     */
    public static long getMillisLeft(Player player) {
        if(!isInCooldown(player)) return 0;
        
        UUID uuid = player.getUniqueId();
        long expireTime = COOLDOWN.get(uuid);
        long systemTime = System.currentTimeMillis();
        long subTime = (expireTime - systemTime);
        
        return (subTime <= 0 ? 0 : subTime);
    }
    
    /**
     * Check if a player needs to wait to use an enderpearl
     * @param player The player to check
     * @return {@code true} if they have time left in the cooldown, {@code false} otherwise
     */
    public static boolean isInCooldown(Player player) {
        UUID uuid = player.getUniqueId();
        return COOLDOWN.containsKey(uuid);
    }
    
    public static void addToCooldown(Player player) {
        UUID uuid = player.getUniqueId();
        int timer = ConfigSettings.getOption("options.time in seconds", 30);
        long timerMillis = (timer * 1000L);
        long systemMillis = System.currentTimeMillis();
        long expireTime = systemMillis + timerMillis;
        COOLDOWN.put(uuid, expireTime);
    }
    
    public static void removeFromCooldown(Player player) {
        UUID uuid = player.getUniqueId();
        COOLDOWN.remove(uuid);
    }
}