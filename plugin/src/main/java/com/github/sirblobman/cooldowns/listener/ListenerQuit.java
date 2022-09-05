package com.github.sirblobman.cooldowns.listener;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerQuitEvent;

import com.github.sirblobman.api.configuration.ConfigurationManager;
import com.github.sirblobman.cooldowns.api.ICooldownsX;
import com.github.sirblobman.cooldowns.api.data.ICooldownData;
import com.github.sirblobman.cooldowns.api.listener.CooldownListener;

public final class ListenerQuit extends CooldownListener {
    public ListenerQuit(ICooldownsX plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onQuit(PlayerQuitEvent e) {
        ConfigurationManager configurationManager = getConfigurationManager();
        YamlConfiguration configuration = configurationManager.get("config.yml");
        if(!configuration.getBoolean("save-amounts-used", true)) {
            return;
        }

        Player player = e.getPlayer();
        ICooldownData cooldownData = getCooldownData(player);
        cooldownData.saveActionCounts();
    }
}
