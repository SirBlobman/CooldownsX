package com.github.sirblobman.cooldowns.listener;

import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityResurrectEvent;

import com.github.sirblobman.api.xseries.XMaterial;
import com.github.sirblobman.cooldowns.CooldownPlugin;
import com.github.sirblobman.cooldowns.manager.CooldownManager;
import com.github.sirblobman.cooldowns.object.CooldownSettings;
import com.github.sirblobman.cooldowns.object.CooldownType;

public final class ListenerUndying extends CooldownListener {
    public ListenerUndying(CooldownPlugin plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onResurrect(EntityResurrectEvent e) {
        Entity entity = e.getEntity();
        if (!(entity instanceof Player)) {
            return;
        }

        Player player = (Player) entity;
        XMaterial material = XMaterial.TOTEM_OF_UNDYING;
        CooldownManager cooldownManager = getCooldownManager();
        if (cooldownManager.canBypass(player, material)) {
            return;
        }

        CooldownSettings cooldownSettings = cooldownManager.getCooldownSettings(material);
        CooldownType cooldownType = cooldownSettings.getCooldownType();
        if (cooldownType != CooldownType.UNDYING) {
            return;
        }

        World world = player.getWorld();
        if (cooldownSettings.isDisabledWorld(world)) {
            return;
        }

        if (checkCooldown(player, material)) {
            e.setCancelled(true);
        }
    }
}
