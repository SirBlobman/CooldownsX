package com.SirBlobman.enderpearl.cooldown.listener;

import java.text.DecimalFormat;
import java.util.List;
import java.util.UUID;

import com.SirBlobman.api.utility.ItemUtil;
import com.SirBlobman.enderpearl.cooldown.EnderpearlCooldown;
import com.SirBlobman.enderpearl.cooldown.utility.EnderpearlCooldownManager;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitScheduler;

public class ListenerEnderPearlCooldown implements Listener {
    private final EnderpearlCooldown plugin;
    public ListenerEnderPearlCooldown(EnderpearlCooldown plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void beforeLaunch(PlayerInteractEvent e) {
        Result useHandItem = e.useItemInHand();
        if(useHandItem == Result.DENY) return;

        Action action = e.getAction();
        if(action != Action.RIGHT_CLICK_BLOCK && action != Action.RIGHT_CLICK_AIR) return;

        ItemStack item = e.getItem();
        if(ItemUtil.isAir(item)) return;

        Material itemType = item.getType();
        if(itemType != Material.ENDER_PEARL) return;

        Block block = e.getClickedBlock();
        if(block != null) {
            Material blockType = block.getType();
            if(isBlockIgnored(blockType)) return;
        }

        Player player = e.getPlayer();
        checkCooldown(player, e);
    }

    private String getDecimalTimeLeft(long millis) {
        double seconds = ((double) millis / 1_000.0D);

        DecimalFormat format = new DecimalFormat("0.0");
        return format.format(seconds);
    }

    private boolean isBlockIgnored(Material type) {
        if(type == null) return false;
        String typeName = type.name();

        FileConfiguration config = this.plugin.getConfig();
        if(config == null) return false;

        List<String> ignoredBlockTypeList = config.getStringList("ignored-block-types");
        return ignoredBlockTypeList.contains(typeName);
    }

    private boolean isWorldIgnored(Player player) {
        if(player == null) return false;

        FileConfiguration config = this.plugin.getConfig();
        if(config == null) return false;

        World world = player.getWorld();
        String worldName = world.getName();

        List<String> ignoredWorldNameList = config.getStringList("disabled-world-list");
        return ignoredWorldNameList.contains(worldName);
    }

    private void checkCooldown(Player player, Cancellable e) {
        if(isWorldIgnored(player)) return;
        if(EnderpearlCooldownManager.canBypass(player)) return;
        UUID uuid = player.getUniqueId();

        if(EnderpearlCooldownManager.isInCooldown(player)) {
            e.setCancelled(true);
            sendCooldownMessage(player);

            updateInventory(player);
            return;
        }

        EnderpearlCooldownManager.addCooldown(player);
    }

    private void sendCooldownMessage(Player player) {
        if(player == null) return;

        String message = this.plugin.getConfigMessage("messages.in-cooldown");
        if(message == null || message.isEmpty()) return;

        long millisLeft = EnderpearlCooldownManager.getTimeLeftMillis(player);
        long secondsLeft = EnderpearlCooldownManager.getTimeLeftSeconds(player);

        String timeLeft = Long.toString(secondsLeft);
        String timeLeftDecimal = getDecimalTimeLeft(millisLeft);
        message = message.replace("{time_left}", timeLeft).replace("{time_left_decimal}", timeLeftDecimal);

        player.sendMessage(message);
    }

    private void updateInventory(Player player) {
        Runnable task = player::updateInventory;
        BukkitScheduler scheduler = Bukkit.getScheduler();
        scheduler.runTaskLater(this.plugin, task, 2L);
    }
}