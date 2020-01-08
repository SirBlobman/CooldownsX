package com.SirBlobman.enderpearl.cooldown.hook;

import com.SirBlobman.enderpearl.cooldown.EnderpearlCooldown;

import org.bukkit.entity.Player;

import be.maximvdw.placeholderapi.PlaceholderAPI;
import be.maximvdw.placeholderapi.PlaceholderReplaceEvent;
import be.maximvdw.placeholderapi.PlaceholderReplacer;

public class HookMVdWPlaceholderAPI implements PlaceholderHook, PlaceholderReplacer {
    private final EnderpearlCooldown plugin;
    public HookMVdWPlaceholderAPI(EnderpearlCooldown plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean register() {
        PlaceholderAPI.registerPlaceholder(this.plugin, "enderpearl_cooldown_time_left", this);
        PlaceholderAPI.registerPlaceholder(this.plugin, "enderpearl_cooldown_time_left_decimal", this);
        return true;
    }

    @Override
    public String onPlaceholderReplace(PlaceholderReplaceEvent e) {
        Player player = e.getPlayer();
        if(player == null) return null;

        String placeholder = e.getPlaceholder();
        if(!placeholder.startsWith("enderpearl_cooldown_")) return null;

        String id = placeholder.substring("enderpearl_cooldown_".length());
        return getPlaceholder(this.plugin, player, id);
    }
}