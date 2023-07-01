package com.github.sirblobman.cooldowns.listener;

import java.util.Set;

import org.jetbrains.annotations.NotNull;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityResurrectEvent;

import com.github.sirblobman.cooldowns.api.CooldownsX;
import com.github.sirblobman.cooldowns.api.configuration.Cooldown;
import com.github.sirblobman.cooldowns.api.configuration.CooldownType;
import com.github.sirblobman.cooldowns.api.data.PlayerCooldown;
import com.github.sirblobman.cooldowns.api.listener.CooldownListener;
import com.github.sirblobman.api.shaded.xseries.XMaterial;

public final class ListenerUndying extends CooldownListener {
    public ListenerUndying(@NotNull CooldownsX plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onResurrect(EntityResurrectEvent e) {
        Entity entity = e.getEntity();
        if (!(entity instanceof Player)) {
            return;
        }

        Player player = (Player) entity;
        PlayerCooldown cooldownData = getCooldownData(player);
        Set<Cooldown> activeCooldowns = cooldownData.getActiveCooldowns(CooldownType.UNDYING);

        Cooldown activeCooldown = checkActiveCooldowns(player, activeCooldowns);
        if (activeCooldown != null) {
            e.setCancelled(true);
            sendCooldownMessage(player, activeCooldown, XMaterial.TOTEM_OF_UNDYING);
            updateInventoryLater(player);
            return;
        }

        Set<Cooldown> validCooldowns = fetchCooldowns(CooldownType.UNDYING);
        checkValidCooldowns(player, validCooldowns);
    }
}
