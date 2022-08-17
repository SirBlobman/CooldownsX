package com.github.sirblobman.cooldowns.listener;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitScheduler;

import com.github.sirblobman.api.xseries.XMaterial;
import com.github.sirblobman.cooldowns.CooldownPlugin;
import com.github.sirblobman.cooldowns.configuration.CooldownSettings;
import com.github.sirblobman.cooldowns.object.CooldownData;
import com.github.sirblobman.cooldowns.object.CooldownType;

public final class ListenerConsume extends CooldownListener {
    public ListenerConsume(CooldownPlugin plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerItemConsume(PlayerItemConsumeEvent e) {
        Player player = e.getPlayer();

    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onConsume(PlayerItemConsumeEvent e) {
        ItemStack item = e.getItem();
        Player player = e.getPlayer();
        XMaterial material = getXMaterial(item);
        if (material == XMaterial.AIR) {
            return;
        }

        List<CooldownSettings> cooldownSettingsList = fetchCooldowns(CooldownType.CONSUME_ITEM);
        if(cooldownSettingsList.isEmpty()) {
            return;
        }

        CooldownData cooldownData = getCooldownData(player);
        List<CooldownSettings> allActiveCooldowns = cooldownData.getActiveCooldowns(CooldownType.CONSUME_ITEM);
        List<CooldownSettings> activeCooldowns = filter(allActiveCooldowns, material);

        CooldownSettings activeCooldown = checkActiveCooldowns(player, activeCooldowns);
        if(activeCooldown != null) {
            e.setCancelled(true);
            sendCooldownMessage(player, activeCooldown, material);

            CooldownPlugin plugin = getPlugin();
            BukkitScheduler scheduler = Bukkit.getScheduler();
            scheduler.runTask(plugin, player::updateInventory);
            return;
        }

        List<CooldownSettings> allValidCooldowns = fetchCooldowns(CooldownType.CONSUME_ITEM);
        List<CooldownSettings> validCooldowns = filter(allValidCooldowns, material);
        checkValidCooldowns(player, validCooldowns);
    }
}
