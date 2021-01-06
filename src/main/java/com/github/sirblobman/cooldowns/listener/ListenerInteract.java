package com.github.sirblobman.cooldowns.listener;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.github.sirblobman.cooldowns.CooldownPlugin;
import com.github.sirblobman.cooldowns.manager.CooldownManager;

public final class ListenerInteract extends CooldownListener {
    public ListenerInteract(CooldownPlugin plugin) {
        super(plugin);
    }

    @EventHandler(priority=EventPriority.NORMAL)
    public void onInteract(PlayerInteractEvent e) {
        Action action = e.getAction();
        if(action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;

        ItemStack item = e.getItem();
        if(item == null) return;

        Player player = e.getPlayer();
        Material material = item.getType();
        if(material.isEdible()) return;

        CooldownManager cooldownManager = getCooldownManager();
        if(cooldownManager.canBypass(player, material)) return;
        if(checkCooldown(player, material)) {
            e.setUseItemInHand(Result.DENY);
        }
    }
}