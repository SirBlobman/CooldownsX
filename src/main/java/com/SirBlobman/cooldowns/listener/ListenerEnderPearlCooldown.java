package com.SirBlobman.cooldowns.listener;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Objects;

import com.SirBlobman.api.configuration.ConfigManager;
import com.SirBlobman.api.item.ItemUtil;
import com.SirBlobman.api.nms.AbstractNMS;
import com.SirBlobman.api.nms.MultiVersionHandler;
import com.SirBlobman.api.nms.PlayerHandler;
import com.SirBlobman.cooldowns.CooldownPlugin;
import com.SirBlobman.cooldowns.manager.CooldownManager;
import com.SirBlobman.cooldowns.object.CooldownInfo;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.scheduler.BukkitScheduler;

import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class ListenerEnderPearlCooldown implements Listener {
    private final CooldownPlugin plugin;
    public ListenerEnderPearlCooldown(CooldownPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin must not be null!");
    }
    
    @EventHandler(priority=EventPriority.NORMAL)
    public void onInteract(PlayerInteractEvent e) {
        Action action = e.getAction();
        if(action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;
        
        Player player = e.getPlayer();
        if(hasBypassPermission(player)) return;
        
        Result result = e.useItemInHand();
        if(result == Result.DENY) return;
        
        Block block = e.getClickedBlock();
        if(isIgnored(block) && !player.isSneaking()) return;
        
        ItemStack item = e.getItem();
        if(ItemUtil.isAir(item)) return;
        
        Material itemType = item.getType();
        if(itemType != Material.ENDER_PEARL) return;
        
        CooldownManager cooldownManager = this.plugin.getCooldownManager();
        CooldownInfo cooldownInfo = cooldownManager.getCooldownInfo(player);
        if(cooldownInfo.isEnderPearlTimerUnset() || cooldownInfo.isEnderPearlTimerExpired()) {
            addCooldown(player);
            return;
        }
        
        e.setUseItemInHand(Result.DENY);
        sendCooldownMessage(player);
    }
    
    private void addCooldown(Player player) {
        ConfigManager<?> configManager = this.plugin.getConfigManager();
        YamlConfiguration config = configManager.getConfig("ender_pearl.yml");
        
        long cooldownMillis = config.getLong("cooldown", 10_000L);
        long systemMillis = System.currentTimeMillis();
        long expireMillis = (systemMillis + cooldownMillis);
        
        CooldownManager cooldownManager = this.plugin.getCooldownManager();
        CooldownInfo cooldownInfo = cooldownManager.getCooldownInfo(player);
        cooldownInfo.setEnderPearlExpireTime(expireMillis);
    
        Runnable task = () -> sendCooldownPacket(player, expireMillis);
        BukkitScheduler scheduler = Bukkit.getScheduler();
        scheduler.runTaskLater(this.plugin, task, 1L);
    }
    
    private void sendCooldownPacket(Player player, long expireTime) {
        long systemTime = System.currentTimeMillis();
        long timeLeftMillis = (expireTime - systemTime);
        int timeLeftTicks = (int) (timeLeftMillis / 50L);
    
        MultiVersionHandler<?> multiVersionHandler = this.plugin.getMultiVersionHandler();
        AbstractNMS nmsHandler = multiVersionHandler.getInterface();
        PlayerHandler playerHandler = nmsHandler.getPlayerHandler();
        playerHandler.sendCooldownPacket(player, Material.ENDER_PEARL, timeLeftTicks);
    }
    
    private void sendCooldownMessage(Player player) {
        ConfigManager<?> configManager = this.plugin.getConfigManager();
        String message = configManager.getConfigMessage("language.yml", "prevent-usage.ender-pearl", true);
        if(message.isEmpty()) return;
        
        CooldownManager cooldownManager = this.plugin.getCooldownManager();
        CooldownInfo cooldownInfo = cooldownManager.getCooldownInfo(player);
        long expireTime = cooldownInfo.getEnderPearlExpireTime();
        
        String timeLeft = formatTimeLeft(expireTime);
        String replaced = message.replace("{time_left}", timeLeft);
        player.sendMessage(replaced);
    }
    
    private String formatTimeLeft(long expireTime) {
        long systemTime = System.currentTimeMillis();
        double timeLeftMillis = (expireTime - systemTime);
        double timeLeftSeconds = (timeLeftMillis / 1_000.0D);
        if(timeLeftSeconds <= 0.0D) timeLeftSeconds = 0.0D;
        
        YamlConfiguration config = this.plugin.getConfig();
        String decimalFormatString = config.getString("decimal-format", "0.000");
        if(decimalFormatString == null) decimalFormatString = "0.000";
        
        DecimalFormat decimalFormat = new DecimalFormat(decimalFormatString);
        return decimalFormat.format(timeLeftSeconds);
    }
    
    private boolean isIgnored(Block block) {
        if(block == null) return false;
    
        ConfigManager<?> configManager = this.plugin.getConfigManager();
        YamlConfiguration config = configManager.getConfig("ender_pearl.yml");
        List<String> ignoredBlockNameList = config.getStringList("ignored-block-list");
        
        Material material = block.getType();
        String materialName = material.name();
        return ignoredBlockNameList.contains(materialName);
    }
    
    private boolean hasBypassPermission(Player player) {
        ConfigManager<?> configManager = this.plugin.getConfigManager();
        YamlConfiguration config = configManager.getConfig("ender_pearl.yml");
        String bypassPermission = config.getString("bypass-permission");
        if(bypassPermission == null || bypassPermission.isEmpty()) return false;
        
        Permission permission = new Permission(bypassPermission, "Enderpearl Cooldown Bypass", PermissionDefault.FALSE);
        return player.hasPermission(permission);
    }
}