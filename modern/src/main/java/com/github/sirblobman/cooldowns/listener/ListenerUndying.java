package com.github.sirblobman.cooldowns.listener;

import java.util.Set;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityResurrectEvent;

import com.github.sirblobman.api.xseries.XMaterial;
import com.github.sirblobman.cooldowns.api.ICooldownsX;
import com.github.sirblobman.cooldowns.api.configuration.CooldownType;
import com.github.sirblobman.cooldowns.api.configuration.ICooldownSettings;
import com.github.sirblobman.cooldowns.api.data.ICooldownData;
import com.github.sirblobman.cooldowns.api.listener.CooldownListener;

public final class ListenerUndying extends CooldownListener {
    public ListenerUndying(ICooldownsX plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onResurrect(EntityResurrectEvent e) {
        Entity entity = e.getEntity();
        if (!(entity instanceof Player)) {
            return;
        }

        Player player = (Player) entity;
        ICooldownData cooldownData = getCooldownData(player);
        Set<ICooldownSettings> activeCooldowns = cooldownData.getActiveCooldowns(CooldownType.UNDYING);

        ICooldownSettings activeCooldown = checkActiveCooldowns(player, activeCooldowns);
        if (activeCooldown != null) {
            e.setCancelled(true);
            sendCooldownMessage(player, activeCooldown, XMaterial.TOTEM_OF_UNDYING);
            updateInventoryLater(player);
            return;
        }

        Set<ICooldownSettings> validCooldowns = fetchCooldowns(CooldownType.UNDYING);
        checkValidCooldowns(player, validCooldowns);
    }
}
