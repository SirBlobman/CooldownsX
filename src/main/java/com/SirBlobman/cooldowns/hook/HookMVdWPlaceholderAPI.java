package com.SirBlobman.cooldowns.hook;

import java.util.Objects;

import com.SirBlobman.cooldowns.CooldownPlugin;

import org.bukkit.entity.Player;

import be.maximvdw.placeholderapi.PlaceholderAPI;
import be.maximvdw.placeholderapi.PlaceholderReplaceEvent;
import be.maximvdw.placeholderapi.PlaceholderReplacer;

public class HookMVdWPlaceholderAPI implements PlaceholderHook, PlaceholderReplacer {
    private final CooldownPlugin plugin;
    public HookMVdWPlaceholderAPI(CooldownPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin must not be null!");
    }
    
    public void register() {
        PlaceholderAPI.registerPlaceholder(this.plugin, "cooldownsx_ender_pearl", this);
        PlaceholderAPI.registerPlaceholder(this.plugin, "cooldownsx_golden_apple", this);
    }
    
    @Override
    public CooldownPlugin getCooldownPlugin() {
        return this.plugin;
    }
    
    @Override
    public String onPlaceholderReplace(PlaceholderReplaceEvent e) {
        Player player = e.getPlayer();
        if(player == null) return null;
        
        String placeholder = e.getPlaceholder();
        if(placeholder == null || !placeholder.startsWith("cooldownsx_")) return null;
        
        String id = placeholder.substring("cooldownsx_".length());
        return getPlaceholder(player, id);
    }
}