package com.SirBlobman.cooldowns.hook;

import java.util.List;
import java.util.Objects;

import com.SirBlobman.cooldowns.CooldownPlugin;

import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;

public class HookPlaceholderAPI extends PlaceholderExpansion implements PlaceholderHook {
    private final CooldownPlugin plugin;
    public HookPlaceholderAPI(CooldownPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin must not be null!");
    }
    
    @Override
    public CooldownPlugin getCooldownPlugin() {
        return this.plugin;
    }
    
    @Override
    public boolean persist() {
        return true;
    }
    
    @Override
    public boolean canRegister() {
        return true;
    }
    
    @Override
    public String getIdentifier() {
        return "cooldownsx";
    }
    
    @Override
    public String getAuthor() {
        PluginDescriptionFile description = this.plugin.getDescription();
        List<String> authorList = description.getAuthors();
        return String.join(", ", authorList);
    }
    
    @Override
    public String getVersion() {
        PluginDescriptionFile description = this.plugin.getDescription();
        return description.getVersion();
    }
    
    @Override
    public String onPlaceholderRequest(Player player, String id) {
        if(player == null || id == null) return null;
        return getPlaceholder(player, id);
    }
}