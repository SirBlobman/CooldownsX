package com.SirBlobman.enderpearl.cooldown.listener;

import com.SirBlobman.api.utility.ItemUtil;
import com.SirBlobman.enderpearl.cooldown.EnderpearlCooldown;
import com.SirBlobman.enderpearl.cooldown.api.ECooldownAPI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.text.DecimalFormat;
import java.util.List;

public class ListenEnderpearls implements Listener {
    private final EnderpearlCooldown plugin;
    public ListenEnderpearls(EnderpearlCooldown plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=false)
    public void beforeLaunch(PlayerInteractEvent e) {
        if(e.useItemInHand() == Event.Result.DENY) return;

        Action action = e.getAction();
        if(action == Action.PHYSICAL || action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) return;

        ItemStack item = e.getItem();
        if(ItemUtil.isAir(item)) return;

        Material type = item.getType();
        if(type != Material.ENDER_PEARL) return;

        Player player = e.getPlayer();
        if(ECooldownAPI.canBypass(player)) return;

        World world = player.getWorld();
        String worldName = world.getName();
        List<String> disabledWorldList = this.plugin.getConfig().getStringList("disabled worlds");
        if(disabledWorldList.contains(worldName)) return;

        Block block = e.getClickedBlock();
        if(block != null) {
            BlockState state = block.getState();
            if(state instanceof InventoryHolder) return;
            if(isBlacklistedFromEnderpearlAction(block.getType())) return;
        }

        if(!ECooldownAPI.isInCooldown(player)) {
            ECooldownAPI.addToCooldown(player);
            return;
        }

        e.setCancelled(true);
        int timeLeft = ECooldownAPI.getTimeLeftSeconds(player);
        long timeLeftMillis = ECooldownAPI.getTimeLeftMillis(player);

        String timeLeftNormal = Integer.toString(timeLeft);
        String timeLeftDecimal = getDecimalTimeLeft(timeLeftMillis);

        String message = this.plugin.getConfigMessage("in cooldown").replace("{time_left}", timeLeftNormal).replace("{time_left_decimal}", timeLeftDecimal);
        player.sendMessage(message);

        Bukkit.getScheduler().runTaskLater(this.plugin, () -> player.updateInventory(), 1L);
    }

    private String getDecimalTimeLeft(long millis) {
        double seconds = ((double) millis / 1000.0D);
        DecimalFormat format = new DecimalFormat("0.0");
        return format.format(seconds);
    }

    private boolean isBlacklistedFromEnderpearlAction(Material type) {
        String typeName = type.name();
        if(typeName.endsWith("BUTTON")) return true;
        if(typeName.equals("WORKBENCH") || typeName.equals("CRAFTING_TABLE")) return true;
        if(typeName.equals("ENDER_CHEST")) return true;

        return false;
    }
}