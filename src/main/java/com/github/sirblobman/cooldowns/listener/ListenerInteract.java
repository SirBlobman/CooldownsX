package com.github.sirblobman.cooldowns.listener;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CrossbowMeta;
import org.bukkit.inventory.meta.ItemMeta;

import com.github.sirblobman.api.utility.VersionUtility;
import com.github.sirblobman.api.xseries.XMaterial;
import com.github.sirblobman.cooldowns.CooldownPlugin;
import com.github.sirblobman.cooldowns.manager.CooldownManager;
import com.github.sirblobman.cooldowns.object.CooldownSettings;
import com.github.sirblobman.cooldowns.object.CooldownType;

public final class ListenerInteract extends CooldownListener {
    public ListenerInteract(CooldownPlugin plugin) {
        super(plugin);
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onInteract(PlayerInteractEvent e) {
        Action action = e.getAction();
        if(action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        
        ItemStack item = e.getItem();
        if(item == null || isCrossbowReloading(item)) {
            return;
        }
        
        XMaterial material = getXMaterial(item);
        if(material == null) {
            return;
        }
        
        Player player = e.getPlayer();
        CooldownManager cooldownManager = getCooldownManager();
        if(cooldownManager.canBypass(player, material)) {
            return;
        }
        
        CooldownSettings cooldownSettings = cooldownManager.getCooldownSettings(material);
        CooldownType cooldownType = cooldownSettings.getCooldownType();
        if(cooldownType != CooldownType.INTERACT) {
            return;
        }

        World world = player.getWorld();
        if(cooldownSettings.isDisabledWorld(world)) {
            return;
        }
        
        if(checkCooldown(player, material)) {
            e.setUseItemInHand(Result.DENY);
        }
    }
    
    private boolean isCrossbowReloading(ItemStack item) {
        int minorVersion = VersionUtility.getMinorVersion();
        if(minorVersion >= 14) {
            ItemMeta meta = item.getItemMeta();
            if(meta instanceof CrossbowMeta) {
                CrossbowMeta crossbow = (CrossbowMeta) meta;
                return !crossbow.hasChargedProjectiles();
            }
        }
        
        return false;
    }
}
