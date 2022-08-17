package com.github.sirblobman.cooldowns.listener;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.scheduler.BukkitScheduler;

import com.github.sirblobman.api.xseries.XMaterial;
import com.github.sirblobman.cooldowns.CooldownPlugin;
import com.github.sirblobman.cooldowns.configuration.CooldownSettings;
import com.github.sirblobman.cooldowns.object.CooldownData;
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
        CooldownData cooldownData = getCooldownData(player);
        List<CooldownSettings> activeCooldowns = cooldownData.getActiveCooldowns(CooldownType.UNDYING);

        CooldownSettings activeCooldown = checkActiveCooldowns(player, activeCooldowns);
        if(activeCooldown != null) {
            e.setCancelled(true);
            sendCooldownMessage(player, activeCooldown, XMaterial.TOTEM_OF_UNDYING);

            CooldownPlugin plugin = getPlugin();
            BukkitScheduler scheduler = Bukkit.getScheduler();
            scheduler.runTask(plugin, player::updateInventory);
            return;
        }

        List<CooldownSettings> validCooldowns = fetchCooldowns(CooldownType.UNDYING);
        checkValidCooldowns(player, validCooldowns);
    }
}
