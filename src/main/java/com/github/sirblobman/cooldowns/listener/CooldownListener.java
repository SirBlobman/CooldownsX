package com.github.sirblobman.cooldowns.listener;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;

import com.github.sirblobman.api.configuration.ConfigurationManager;
import com.github.sirblobman.api.utility.MessageUtility;
import com.github.sirblobman.api.utility.Validate;
import com.github.sirblobman.cooldowns.CooldownPlugin;
import com.github.sirblobman.cooldowns.manager.CooldownManager;
import com.github.sirblobman.cooldowns.manager.UndyingManager;
import com.github.sirblobman.cooldowns.object.CooldownData;

public abstract class CooldownListener implements Listener {
    private final CooldownPlugin plugin;
    private boolean isRegistered;
    public CooldownListener(CooldownPlugin plugin) {
        this.plugin = Validate.notNull(plugin, "plugin must not be null!");
        this.isRegistered = false;
    }

    protected final CooldownPlugin getPlugin() {
        return this.plugin;
    }

    protected final CooldownManager getCooldownManager() {
        CooldownPlugin plugin = getPlugin();
        return plugin.getCooldownManager();
    }

    protected final UndyingManager getUndyingManager() {
        CooldownPlugin plugin = getPlugin();
        return plugin.getUndyingManager();
    }

    protected final boolean checkCooldown(Player player, Material material) {
        CooldownManager cooldownManager = getCooldownManager();
        CooldownData cooldownData = cooldownManager.getData(player);
        CooldownPlugin plugin = getPlugin();

        if(cooldownData.hasCooldown(plugin, material)) {
            sendCooldownMessage(player, material);
            return true;
        }

        if(cooldownManager.hasCooldown(material)) {
            long systemMillis = System.currentTimeMillis();
            long expireMillis = (systemMillis + cooldownManager.getCooldown(material));
            cooldownData.setCooldown(plugin, material, expireMillis);
        }

        return false;
    }

    public final void register() {
        if(this.isRegistered) return;
        CooldownPlugin plugin = getPlugin();
        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(this, plugin);
        this.isRegistered = true;
    }

    private String getTimeLeft(Player player, Material material) {
        CooldownManager cooldownManager = getCooldownManager();
        CooldownData cooldownData = cooldownManager.getData(player);
        double expireMillis = cooldownData.getCooldownExpireTime(material);
        double systemMillis = System.currentTimeMillis();
        double millisLeft = (expireMillis - systemMillis);

        long timeLeftSeconds = (long) Math.ceil(millisLeft / 1_000.0D);
        return Long.toString(timeLeftSeconds);
    }

    private void sendCooldownMessage(Player player, Material material) {
        ConfigurationManager configurationManager = this.plugin.getConfigurationManager();
        YamlConfiguration configuration = configurationManager.get("config.yml");
        String messageFormat = configuration.getString("cooldown-message");
        if(messageFormat == null) return;

        String timeLeft = getTimeLeft(player, material);
        String materialName = material.name();
        String message = MessageUtility.color(messageFormat).replace("{time_left}", timeLeft).replace("{material}", materialName);
        player.sendMessage(message);
    }
}