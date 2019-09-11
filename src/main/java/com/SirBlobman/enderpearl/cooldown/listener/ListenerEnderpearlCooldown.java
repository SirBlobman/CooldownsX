package com.SirBlobman.enderpearl.cooldown.listener;

import com.SirBlobman.api.utility.ItemUtil;
import com.SirBlobman.api.utility.Util;
import com.SirBlobman.enderpearl.cooldown.EnderpearlCooldown;
import com.SirBlobman.enderpearl.cooldown.api.ECooldownAPI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;

import java.text.DecimalFormat;
import java.util.List;
import java.util.UUID;

public class ListenerEnderpearlCooldown implements Listener {
    private final EnderpearlCooldown plugin;
    public ListenerEnderpearlCooldown(EnderpearlCooldown plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void beforeLaunch(PlayerInteractEvent e) {
        if(e.useItemInHand() == Event.Result.DENY) return;

        Action action = e.getAction();
        if(action != Action.RIGHT_CLICK_BLOCK && action != Action.RIGHT_CLICK_AIR) return;

        ItemStack item = e.getItem();
        if(ItemUtil.isAir(item)) return;

        Material type = item.getType();
        if(type != Material.ENDER_PEARL) return;

        Block block = e.getClickedBlock();
        if(block != null) {
            Material blockType = block.getType();
            if(isBlacklistedFromEnderpearlAction(blockType)) return;
        }

        Player player = e.getPlayer();
        checkCooldown(player, e);
    }

    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
    public void onLaunch(ProjectileLaunchEvent e) {
        Projectile projectile = e.getEntity();
        if(!(projectile instanceof EnderPearl)) return;

        EnderPearl enderpearl = (EnderPearl) projectile;
        ProjectileSource shooter = enderpearl.getShooter();
        if(!(shooter instanceof Player)) return;

        Player player = (Player) shooter;
        checkCooldown(player, e);

        if(e.isCancelled()) player.getInventory().addItem(new ItemStack(Material.ENDER_PEARL, 1));
    }

    private final List<UUID> allowUse = Util.newList();
    private void checkCooldown(Player player, Cancellable e) {
        World world = player.getWorld();
        String worldName = world.getName();
        List<String> disabledWorldList = this.plugin.getConfig().getStringList("disabled worlds");
        if(disabledWorldList.contains(worldName)) return;
        if(ECooldownAPI.canBypass(player)) return;

        UUID uuid = player.getUniqueId();
        if(!ECooldownAPI.isInCooldown(player)) {
            ECooldownAPI.addToCooldown(player);
            allowUse.add(uuid);
            return;
        }

        if(allowUse.contains(uuid)) {
            allowUse.remove(uuid);
            return;
        }

        e.setCancelled(true);
        Bukkit.getScheduler().runTaskLater(this.plugin, player::updateInventory, 1L);

        int timeLeftSeconds = ECooldownAPI.getTimeLeftSeconds(player);
        long timeLeftMillis = ECooldownAPI.getTimeLeftMillis(player);

        String normalString = Integer.toString(timeLeftSeconds);
        String decimalString = getDecimalTimeLeft(timeLeftMillis);

        String message = this.plugin.getConfigMessage("in cooldown")
                .replace("{time_left}", normalString)
                .replace("{time_left_decimal}", decimalString);
        player.sendMessage(message);
    }

    private String getDecimalTimeLeft(long millis) {
        double seconds = ((double) millis / 1000.0D);
        DecimalFormat format = new DecimalFormat("0.0");
        return format.format(seconds);
    }

    private boolean isBlacklistedFromEnderpearlAction(Material type) {
        if(type == null) return false;

        FileConfiguration config = this.plugin.getConfig();
        List<String> blockBlacklist = config.getStringList("ignore interaction blocks");

        String typeName = type.name();
        return blockBlacklist.contains(typeName);
    }
}