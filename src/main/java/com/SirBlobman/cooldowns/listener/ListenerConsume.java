package com.SirBlobman.cooldowns.listener;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

import com.SirBlobman.cooldowns.CooldownPlugin;
import com.SirBlobman.cooldowns.manager.CooldownManager;

public final class ListenerConsume extends CooldownListener {
    public ListenerConsume(CooldownPlugin plugin) {
        super(plugin);
    }

    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
    public void onInteract(PlayerItemConsumeEvent e) {
        ItemStack item = e.getItem();
        Material material = item.getType();

        Player player = e.getPlayer();
        CooldownManager cooldownManager = getCooldownManager();

        if(cooldownManager.canBypass(player, material)) return;
        if(checkCooldown(player, material)) {
            e.setCancelled(true);
        }
    }
}