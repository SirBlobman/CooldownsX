package com.SirBlobman.enderpearl.listener;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventException;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.RegisteredListener;

import com.SirBlobman.enderpearl.EnderpearlCooldown;
import com.SirBlobman.enderpearl.config.ConfigSettings;
import com.SirBlobman.enderpearl.utility.ECooldownUtil;
import com.SirBlobman.enderpearl.utility.Util;

public class ListenEnderpearl implements Listener {
    @EventHandler(priority=EventPriority.LOWEST, ignoreCancelled=false)
    public void beforeLaunch(PlayerInteractEvent e) {
        Util.debug("PlayerInteractEvent triggered, checking for enderpearl...");
        
        Action action = e.getAction();
        if(action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
            Util.debug("Action was left click (punch), which cannot launch an enderpearl. Ignoring event.");
            return;
        }
        
        Player player = e.getPlayer();
        World world = player.getWorld();
        String worldName = world.getName();
        List<String> disabledWorldList = ConfigSettings.getOption("options.disabled worlds", Util.newList());
        if(disabledWorldList.contains(worldName)) {
            Util.debug("The world '" + worldName + " is disabled. Ignoring event.");
            return;
        }
        
        ItemStack itemUsed = e.getItem();
        if(itemUsed == null) {
            Util.debug("The item in the event is null. Ignoring event.");
            return;
        }
        
        Block block = e.getClickedBlock();
        if(block != null) {
        	BlockState state = block.getState();
        	if(state instanceof InventoryHolder) return;
        }
        
        Material type = itemUsed.getType();
        if(type != Material.ENDER_PEARL) {
            Util.debug("The item in the event is not an ender pearl. Ignoring event.");
            return;
        }
        
        Util.debug("Checking player '" + player.getName() + "'...");
        
        Permission permission = new Permission(ConfigSettings.getOption("options.bypass permission", "epearlcooldown.bypass"), "Bypass the ender pearl cooldown.", PermissionDefault.FALSE);
        if(player.hasPermission(permission)) {
            Util.debug("Player has permission '" + permission.getName() + "'. Ignoring event.");
            return;
        }
        
        if(!ECooldownUtil.isInCooldown(player)) {
            Util.debug("Player is not in cooldown, adding for next ender pearl...");
            ECooldownUtil.addToCooldown(player);
            return;
        }
        
        Util.debug("Player is in cooldown, cancelling ender pearl...");
        
        e.setCancelled(true);
        int timeLeft = ECooldownUtil.getSecondsLeft(player);
        
        String messageBlocked = ConfigSettings.getOption("messages.blocked", "").replace("{time_left}", Integer.toString(timeLeft));
        Util.sendMessage(player, messageBlocked);
        
        Bukkit.getScheduler().runTaskLater(EnderpearlCooldown.INSTANCE, () -> player.updateInventory(), 1L);
    }
    
    @SuppressWarnings("deprecation")
    @EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=false)
    public void onInteractDebug(PlayerInteractEvent e) {
        if(!ConfigSettings.getOption("debug", false)) return;
        
        ItemStack item = e.getItem();
        if(item == null) return;
        
        Material type = item.getType();
        if(type != Material.ENDER_PEARL) return;
        
        if(e.isCancelled()) Util.debug("Successfully cancelled event.");
        else {
            Util.debug("Event not cancelled. The conditions were not met or another plugin interfered.");
            Util.debug("Event Handlers for PlayerInteractEvent: ");
            HandlerList handlerList = e.getHandlers();
            for(RegisteredListener listenReg : handlerList.getRegisteredListeners()) {
                Listener listen = listenReg.getListener();
                Class<?> listenClass = listen.getClass();
                Util.debug(" - " + listenClass.getName());
                
                if(!ConfigSettings.getOption("extreme debug", false)) continue;
                
                e.setCancelled(true);
                try {
                    listenReg.callEvent(e);
                    if(!e.isCancelled()) {
                        Util.debug("    * Possible issue with plugin '" + listenReg.getPlugin().getName() + "'. This plugin is un-cancelling the PlayerInteractEvent!");
                    }
                } catch(EventException ex) {
                    Util.debug("An error occurred while trying to check the event.");
                    ex.printStackTrace();
                }
            }
        }
    }
}