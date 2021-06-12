package com.github.sirblobman.cooldowns.listener;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.github.sirblobman.api.configuration.ConfigurationManager;
import com.github.sirblobman.api.core.listener.PluginListener;
import com.github.sirblobman.api.utility.MessageUtility;
import com.github.sirblobman.api.xseries.XMaterial;
import com.github.sirblobman.cooldowns.CooldownPlugin;
import com.github.sirblobman.cooldowns.manager.CooldownManager;
import com.github.sirblobman.cooldowns.manager.UndyingManager;
import com.github.sirblobman.cooldowns.object.CooldownData;

public abstract class CooldownListener extends PluginListener<CooldownPlugin> {
    public CooldownListener(CooldownPlugin plugin) {
        super(plugin);
    }

    protected final ConfigurationManager getConfigurationManager() {
        CooldownPlugin plugin = getPlugin();
        return plugin.getConfigurationManager();
    }

    protected final CooldownManager getCooldownManager() {
        CooldownPlugin plugin = getPlugin();
        return plugin.getCooldownManager();
    }

    protected final UndyingManager getUndyingManager() {
        CooldownPlugin plugin = getPlugin();
        return plugin.getUndyingManager();
    }

    protected final boolean checkCooldown(Player player, XMaterial material) {
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

    private String getTimeLeft(Player player, XMaterial material) {
        CooldownManager cooldownManager = getCooldownManager();
        CooldownData cooldownData = cooldownManager.getData(player);
        double expireMillis = cooldownData.getCooldownExpireTime(material);
        double systemMillis = System.currentTimeMillis();
        double millisLeft = (expireMillis - systemMillis);

        long timeLeftSeconds = (long) Math.ceil(millisLeft / 1_000.0D);
        return Long.toString(timeLeftSeconds);
    }

    private void sendCooldownMessage(Player player, XMaterial material) {
        ConfigurationManager configurationManager = getConfigurationManager();
        YamlConfiguration configuration = configurationManager.get("config.yml");
        String messageFormat = configuration.getString("cooldown-message");
        if(messageFormat == null) return;

        CooldownPlugin plugin = getPlugin();
        String timeLeft = getTimeLeft(player, material);
        String materialName = plugin.getMaterialName(material);

        String message = MessageUtility.color(messageFormat).replace("{time_left}", timeLeft)
                .replace("{material}", materialName);
        player.sendMessage(message);
    }
}
