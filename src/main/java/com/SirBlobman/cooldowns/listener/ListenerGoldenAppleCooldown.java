package com.SirBlobman.cooldowns.listener;

import java.text.DecimalFormat;
import java.util.Objects;

import com.SirBlobman.api.configuration.ConfigManager;
import com.SirBlobman.api.nms.AbstractNMS;
import com.SirBlobman.api.nms.ItemHandler;
import com.SirBlobman.api.nms.MultiVersionHandler;
import com.SirBlobman.api.nms.PlayerHandler;
import com.SirBlobman.cooldowns.CooldownPlugin;
import com.SirBlobman.cooldowns.manager.CooldownManager;
import com.SirBlobman.cooldowns.object.CooldownInfo;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.scheduler.BukkitScheduler;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;

public class ListenerGoldenAppleCooldown implements Listener {
    private final CooldownPlugin plugin;
    public ListenerGoldenAppleCooldown(CooldownPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin must not be null!");
    }
    
    @EventHandler(priority=EventPriority.NORMAL, ignoreCancelled=true)
    public void onEat(PlayerItemConsumeEvent e) {
        ItemStack item = e.getItem();
        if(!isGoldenApple(item)) return;
    
        Player player = e.getPlayer();
        if(hasBypassPermission(player)) return;
        
        CooldownManager cooldownManager = this.plugin.getCooldownManager();
        CooldownInfo cooldownInfo = cooldownManager.getCooldownInfo(player);
        if(cooldownInfo.isGoldenAppleTimerUnset() || cooldownInfo.isGoldenAppleTimerExpired()) {
            addCooldown(player);
            return;
        }
    
        e.setCancelled(true);
        sendCooldownMessage(player);
    
        BukkitScheduler scheduler = Bukkit.getScheduler();
        scheduler.runTaskLater(this.plugin, player::updateInventory, 1L);
    }
    
    private boolean isGoldenApple(ItemStack item) {
        Material material = item.getType();
        String materialName = material.name();
        return (materialName.equals("GOLDEN_APPLE") || materialName.equals("ENCHANTED_GOLDEN_APPLE"));
    }
    
    private void addCooldown(Player player) {
        ConfigManager<?> configManager = this.plugin.getConfigManager();
        YamlConfiguration config = configManager.getConfig("golden_apple.yml");
        
        long cooldownMillis = config.getLong("cooldown", 10_000L);
        long systemMillis = System.currentTimeMillis();
        long expireMillis = (systemMillis + cooldownMillis);
        
        CooldownManager cooldownManager = this.plugin.getCooldownManager();
        CooldownInfo cooldownInfo = cooldownManager.getCooldownInfo(player);
        cooldownInfo.setGoldenAppleExpireTime(expireMillis);
    
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
        ItemHandler itemHandler = nmsHandler.getItemHandler();
        PlayerHandler playerHandler = nmsHandler.getPlayerHandler();
    
        Material goldenApple = itemHandler.matchMaterial("GOLDEN_APPLE");
        if(goldenApple != null) playerHandler.sendCooldownPacket(player, goldenApple, timeLeftTicks);
    
        Material enchantedGoldenApple = itemHandler.matchMaterial("ENCHANTED_GOLDEN_APPLE");
        if(enchantedGoldenApple != null) playerHandler.sendCooldownPacket(player, enchantedGoldenApple, timeLeftTicks);
    }
    
    private void sendCooldownMessage(Player player) {
        ConfigManager<?> configManager = this.plugin.getConfigManager();
        String message = configManager.getConfigMessage("language.yml", "prevent-usage.golden-apple", true);
        if(message.isEmpty()) return;
        
        CooldownManager cooldownManager = this.plugin.getCooldownManager();
        CooldownInfo cooldownInfo = cooldownManager.getCooldownInfo(player);
        long expireTime = cooldownInfo.getGoldenAppleExpireTime();
        
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
    
    private boolean hasBypassPermission(Player player) {
        ConfigManager<?> configManager = this.plugin.getConfigManager();
        YamlConfiguration config = configManager.getConfig("golden_apple.yml");
        String bypassPermission = config.getString("bypass-permission");
        if(bypassPermission == null || bypassPermission.isEmpty()) return false;
        
        Permission permission = new Permission(bypassPermission, "Golden Apple Cooldown Bypass", PermissionDefault.FALSE);
        return player.hasPermission(permission);
    }
}