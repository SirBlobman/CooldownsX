package com.github.sirblobman.plugin.cooldown.listener;

import org.jetbrains.annotations.NotNull;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerQuitEvent;

import com.github.sirblobman.api.configuration.ConfigurationManager;
import com.github.sirblobman.plugin.cooldown.CooldownPlugin;
import com.github.sirblobman.plugin.cooldown.api.CooldownsX;
import com.github.sirblobman.plugin.cooldown.api.data.PlayerCooldown;
import com.github.sirblobman.plugin.cooldown.api.listener.CooldownListener;
import com.github.sirblobman.plugin.cooldown.configuration.MainConfiguration;

public final class ListenerQuit extends CooldownListener {
    private final CooldownPlugin plugin;

    public ListenerQuit(@NotNull CooldownPlugin plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    private @NotNull CooldownPlugin getCooldownPlugin() {
        return this.plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onQuit(PlayerQuitEvent e) {
        CooldownPlugin plugin = getCooldownPlugin();
        MainConfiguration configuration = plugin.getConfiguration();
        if (!configuration.isSaveAmountsUsed()) {
            return;
        }

        Player player = e.getPlayer();
        PlayerCooldown cooldownData = getCooldownData(player);
        cooldownData.saveActionCounts();
    }
}
