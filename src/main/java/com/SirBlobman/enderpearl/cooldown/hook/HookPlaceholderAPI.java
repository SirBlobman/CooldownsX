package com.SirBlobman.enderpearl.cooldown.hook;

import com.SirBlobman.enderpearl.cooldown.EnderpearlCooldown;

import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;

public class HookPlaceholderAPI extends PlaceholderExpansion implements PlaceholderHook {
    private final EnderpearlCooldown plugin;
    public HookPlaceholderAPI(EnderpearlCooldown plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getIdentifier() {
        return "enderpearlcooldown";
    }

    @Override
    public String getAuthor() {
        return "SirBlobman";
    }

    @Override
    public String getVersion() {
        PluginDescriptionFile description = this.plugin.getDescription();
        return description.getVersion();
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean register() {
        return super.register();
    }

    @Override
    public String onPlaceholderRequest(Player player, String id) {
        return getPlaceholder(this.plugin, player, id);
    }
}