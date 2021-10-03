package com.github.sirblobman.cooldowns.task;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.github.sirblobman.api.utility.MessageUtility;
import com.github.sirblobman.api.utility.Validate;
import com.github.sirblobman.api.xseries.XMaterial;
import com.github.sirblobman.cooldowns.CooldownPlugin;
import com.github.sirblobman.cooldowns.manager.CooldownManager;
import com.github.sirblobman.cooldowns.object.ActionBarSettings;
import com.github.sirblobman.cooldowns.object.CooldownData;
import com.github.sirblobman.cooldowns.object.CooldownSettings;

public final class ActionBarTask extends BukkitRunnable {
    private final CooldownPlugin plugin;
    
    public ActionBarTask(CooldownPlugin plugin) {
        this.plugin = Validate.notNull(plugin, "plugin must not be null!");
    }
    
    public void start() {
        runTaskTimerAsynchronously(this.plugin, 1L, 1L);
    }
    
    @Override
    public void run() {
        Collection<? extends Player> onlinePlayerCollection = Bukkit.getOnlinePlayers();
        for(Player player : onlinePlayerCollection) checkActionBar(player);
    }
    
    private void checkActionBar(Player player) {
        CooldownManager cooldownManager = this.plugin.getCooldownManager();
        CooldownData cooldownData = cooldownManager.getData(player);
        Set<XMaterial> activeCooldownSet = cooldownData.getActiveCooldowns(this.plugin);
        
        int highestPriority = Integer.MIN_VALUE;
        CooldownSettings highestSettings = null;
        for(XMaterial material : activeCooldownSet) {
            CooldownSettings cooldownSettings = cooldownManager.getCooldownSettings(material);
            if(cooldownSettings == null) continue;
            
            ActionBarSettings actionBarSettings = cooldownSettings.getActionBarSettings();
            if(actionBarSettings.isEnabled()) {
                int priority = actionBarSettings.getPriority();
                if(priority > highestPriority) {
                    highestPriority = priority;
                    highestSettings = cooldownSettings;
                }
            }
        }
        
        if(highestSettings != null) {
            sendActionBar(player, highestSettings);
        }
    }
    
    private void sendActionBar(Player player, CooldownSettings settings) {
        CooldownManager cooldownManager = this.plugin.getCooldownManager();
        CooldownData cooldownData = cooldownManager.getData(player);
        ActionBarSettings actionBarSettings = settings.getActionBarSettings();
        XMaterial material = settings.getMaterial();
        
        long expireMillis = cooldownData.getCooldownExpireTime(material);
        long systemMillis = System.currentTimeMillis();
        long subtractMillis = (expireMillis - systemMillis);
        long secondsLeft = TimeUnit.MILLISECONDS.toSeconds(subtractMillis);
        String secondsLeftString = Long.toString(secondsLeft);
        
        String messageFormat = actionBarSettings.getMessageFormat();
        String message = MessageUtility.color(messageFormat).replace("{time_left}", secondsLeftString);
        this.plugin.getMultiVersionHandler().getPlayerHandler().sendActionBar(player, message);
    }
}
