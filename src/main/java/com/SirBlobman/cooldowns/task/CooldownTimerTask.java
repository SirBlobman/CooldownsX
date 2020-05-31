package com.SirBlobman.cooldowns.task;

import java.text.DecimalFormat;
import java.util.Collection;
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
import org.bukkit.scheduler.BukkitRunnable;

public final class CooldownTimerTask extends BukkitRunnable {
    private final CooldownPlugin plugin;
    public CooldownTimerTask(CooldownPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin must not be null!");
    }
    
    @Override
    public void run() {
        Collection<? extends Player> onlinePlayerList = Bukkit.getOnlinePlayers();
        onlinePlayerList.forEach(this::checkCooldown);
    }
    
    private void checkCooldown(Player player) {
        CooldownManager cooldownManager = this.plugin.getCooldownManager();
        CooldownInfo cooldownInfo = cooldownManager.getCooldownInfo(player);
    
        MultiVersionHandler<?> multiVersionHandler = this.plugin.getMultiVersionHandler();
        AbstractNMS nmsHandler = multiVersionHandler.getInterface();
        ItemHandler itemHandler = nmsHandler.getItemHandler();
        PlayerHandler playerHandler = nmsHandler.getPlayerHandler();
        
        boolean isGoldenAppleExpired = cooldownInfo.isGoldenAppleTimerExpired();
        if(isGoldenAppleExpired && !cooldownInfo.isGoldenAppleTimerUnset()) {
            cooldownInfo.setGoldenAppleExpireTime(-1L);
            sendCooldownExpireMessage(player);
    
            Material goldenApple = itemHandler.matchMaterial("GOLDEN_APPLE");
            if(goldenApple != null) playerHandler.sendCooldownPacket(player, goldenApple, 0);
    
            Material enchantedGoldenApple = itemHandler.matchMaterial("ENCHANTED_GOLDEN_APPLE");
            if(enchantedGoldenApple != null) playerHandler.sendCooldownPacket(player, enchantedGoldenApple, 0);
        }
        
        boolean isEnderPearlExpired = cooldownInfo.isEnderPearlTimerExpired();
        if(isEnderPearlExpired && !cooldownInfo.isEnderPearlTimerUnset()) {
            cooldownInfo.setEnderPearlExpireTime(-1L);
            sendCooldownExpireMessage(player);
            playerHandler.sendCooldownPacket(player, Material.ENDER_PEARL, 0);
        }
        
        if(isGoldenAppleExpired && isEnderPearlExpired) return;
        if(!isGoldenAppleExpired && !isEnderPearlExpired) {
            sendBothMessage(player);
            return;
        }
        
        if(!isGoldenAppleExpired) {
            sendGoldenAppleMessage(player);
            return;
        }
        
        sendEnderPearlMessage(player);
    }
    
    private void sendGoldenAppleMessage(Player player) {
        CooldownManager cooldownManager = this.plugin.getCooldownManager();
        CooldownInfo cooldownInfo = cooldownManager.getCooldownInfo(player);
        long goldenAppleExpireMillis = cooldownInfo.getGoldenAppleExpireTime();
    
        ConfigManager<?> configManager = this.plugin.getConfigManager();
        String message = configManager.getConfigMessage("language.yml", "action-bar.golden-apple-timer", true);
        if(message.isEmpty()) return;
    
        String goldenAppleTimeLeft = formatTimeLeft(goldenAppleExpireMillis);
        String replace = message.replace("{time_left}", goldenAppleTimeLeft);
        sendActionBar(player, replace);
    }
    
    private void sendEnderPearlMessage(Player player) {
        CooldownManager cooldownManager = this.plugin.getCooldownManager();
        CooldownInfo cooldownInfo = cooldownManager.getCooldownInfo(player);
        long enderPearlExpireMillis = cooldownInfo.getEnderPearlExpireTime();
    
        ConfigManager<?> configManager = this.plugin.getConfigManager();
        String message = configManager.getConfigMessage("language.yml", "action-bar.ender-pearl-timer", true);
        if(message.isEmpty()) return;
    
        String enderPearlTimeLeft = formatTimeLeft(enderPearlExpireMillis);
        String replace = message.replace("{time_left}", enderPearlTimeLeft);
        sendActionBar(player, replace);
    }
    
    private void sendCooldownExpireMessage(Player player) {
        ConfigManager<?> configManager = this.plugin.getConfigManager();
        String message = configManager.getConfigMessage("language.yml", "action-bar.cooldown-end", true);
        if(message.isEmpty()) return;
        
        sendActionBar(player, message);
    }
    
    private void sendBothMessage(Player player) {
        CooldownManager cooldownManager = this.plugin.getCooldownManager();
        CooldownInfo cooldownInfo = cooldownManager.getCooldownInfo(player);
        long goldenAppleExpireMillis = cooldownInfo.getGoldenAppleExpireTime();
        long enderPearlExpireMillis = cooldownInfo.getEnderPearlExpireTime();
    
        ConfigManager<?> configManager = this.plugin.getConfigManager();
        String message = configManager.getConfigMessage("language.yml", "action-bar.both-timer", true);
        if(message.isEmpty()) return;
    
        String goldenAppleTimeLeft = formatTimeLeft(goldenAppleExpireMillis);
        String enderPearlTimeLeft = formatTimeLeft(enderPearlExpireMillis);
        String replace = message.replace("{time_left_e}", enderPearlTimeLeft).replace("{time_left_g}", goldenAppleTimeLeft);
        sendActionBar(player, replace);
    }
    
    private void sendActionBar(Player player, String message) {
        MultiVersionHandler<?> multiVersionHandler = this.plugin.getMultiVersionHandler();
        AbstractNMS nmsHandler = multiVersionHandler.getInterface();
        PlayerHandler playerHandler = nmsHandler.getPlayerHandler();
        playerHandler.sendActionBar(player, message);
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
}