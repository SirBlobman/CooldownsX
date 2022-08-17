package com.github.sirblobman.cooldowns.listener;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CrossbowMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitScheduler;

import com.github.sirblobman.api.utility.VersionUtility;
import com.github.sirblobman.api.xseries.XMaterial;
import com.github.sirblobman.cooldowns.CooldownPlugin;
import com.github.sirblobman.cooldowns.configuration.CooldownSettings;
import com.github.sirblobman.cooldowns.object.CooldownData;
import com.github.sirblobman.cooldowns.object.CooldownType;

public final class ListenerInteract extends CooldownListener {
    public ListenerInteract(CooldownPlugin plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onInteract(PlayerInteractEvent e) {
        Action action = e.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = e.getPlayer();
        if(action == Action.RIGHT_CLICK_BLOCK) {
            Block block = e.getClickedBlock();
            if(block != null) {
                checkBlock(player, block, e);
            }
        }

        ItemStack item = e.getItem();
        checkItem(player, item, e);
    }

    private void checkItem(Player player, ItemStack item, PlayerInteractEvent e) {
        if(item == null || isCrossbowReloading(item)) {
            return;
        }

        XMaterial material = getXMaterial(item);
        if (material == XMaterial.AIR) {
            return;
        }

        List<CooldownSettings> cooldownSettingsList = fetchCooldowns(CooldownType.INTERACT_ITEM);
        if(cooldownSettingsList.isEmpty()) {
            return;
        }

        CooldownData cooldownData = getCooldownData(player);
        List<CooldownSettings> allActiveCooldowns = cooldownData.getActiveCooldowns(CooldownType.INTERACT_ITEM);
        List<CooldownSettings> activeCooldowns = filter(allActiveCooldowns, material);

        CooldownSettings activeCooldown = checkActiveCooldowns(player, activeCooldowns);
        if(activeCooldown != null) {
            e.setUseItemInHand(Result.DENY);
            sendCooldownMessage(player, activeCooldown, material);

            CooldownPlugin plugin = getPlugin();
            BukkitScheduler scheduler = Bukkit.getScheduler();
            scheduler.runTask(plugin, player::updateInventory);
            return;
        }

        List<CooldownSettings> allValidCooldowns = fetchCooldowns(CooldownType.INTERACT_ITEM);
        List<CooldownSettings> validCooldowns = filter(allValidCooldowns, material);
        checkValidCooldowns(player, validCooldowns);
    }

    private void checkBlock(Player player, Block block, PlayerInteractEvent e) {
        XMaterial material = getXMaterial(block);
        if (material == XMaterial.AIR) {
            return;
        }

        List<CooldownSettings> cooldownSettingsList = fetchCooldowns(CooldownType.INTERACT_BLOCK);
        if(cooldownSettingsList.isEmpty()) {
            return;
        }

        CooldownData cooldownData = getCooldownData(player);
        List<CooldownSettings> allActiveCooldowns = cooldownData.getActiveCooldowns(CooldownType.INTERACT_BLOCK);
        List<CooldownSettings> activeCooldowns = filter(allActiveCooldowns, material);

        CooldownSettings activeCooldown = checkActiveCooldowns(player, activeCooldowns);
        if(activeCooldown != null) {
            e.setUseInteractedBlock(Result.DENY);
            sendCooldownMessage(player, activeCooldown, material);

            CooldownPlugin plugin = getPlugin();
            BukkitScheduler scheduler = Bukkit.getScheduler();
            scheduler.runTask(plugin, player::updateInventory);
            return;
        }

        List<CooldownSettings> allValidCooldowns = fetchCooldowns(CooldownType.INTERACT_BLOCK);
        List<CooldownSettings> validCooldowns = filter(allValidCooldowns, material);
        checkValidCooldowns(player, validCooldowns);
    }

    private boolean isCrossbowReloading(ItemStack item) {
        int minorVersion = VersionUtility.getMinorVersion();
        if (minorVersion < 14) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta instanceof CrossbowMeta) {
            CrossbowMeta crossbow = (CrossbowMeta) meta;
            return !crossbow.hasChargedProjectiles();
        }

        return false;
    }
}
