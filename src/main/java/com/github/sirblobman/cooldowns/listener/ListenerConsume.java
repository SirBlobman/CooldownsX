package com.github.sirblobman.cooldowns.listener;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

import com.github.sirblobman.api.xseries.XMaterial;
import com.github.sirblobman.cooldowns.CooldownPlugin;
import com.github.sirblobman.cooldowns.manager.CooldownManager;
import com.github.sirblobman.cooldowns.object.CooldownSettings;
import com.github.sirblobman.cooldowns.object.CooldownType;

public final class ListenerConsume extends CooldownListener {
    public ListenerConsume(CooldownPlugin plugin) {
        super(plugin);
    }
    
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onConsume(PlayerItemConsumeEvent e) {
        ItemStack item = e.getItem();
        Player player = e.getPlayer();
        
        XMaterial material = getXMaterial(item);
        if(material == null) {
            return;
        }
        
        CooldownManager cooldownManager = getCooldownManager();
        if(cooldownManager.canBypass(player, material)) {
            return;
        }
        
        CooldownSettings cooldownSettings = cooldownManager.getCooldownSettings(material);
        CooldownType cooldownType = cooldownSettings.getCooldownType();
        if(cooldownType != CooldownType.CONSUME) {
            return;
        }

        World world = player.getWorld();
        if(cooldownSettings.isDisabledWorld(world)) {
            return;
        }
        
        if(checkCooldown(player, material)) {
            e.setCancelled(true);
        }
    }
}
