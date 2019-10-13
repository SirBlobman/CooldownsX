package com.SirBlobman.enderpearl.cooldown.hook.placeholder;

import com.SirBlobman.enderpearl.cooldown.EnderpearlCooldown;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;

public class PlaceholderHookClip extends PlaceholderExpansion {
    private final PlaceholderHook hook;
    public PlaceholderHookClip(EnderpearlCooldown plugin) {
        this.hook = new PlaceholderHook(plugin);
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
        return "3.0";
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
    public String onRequest(OfflinePlayer offline, String id) {
        if(!offline.isOnline()) return null;

        Player player = offline.getPlayer();
        return this.hook.getPlaceholder(player, id);
    }
}