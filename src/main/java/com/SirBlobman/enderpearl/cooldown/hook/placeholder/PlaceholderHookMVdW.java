package com.SirBlobman.enderpearl.cooldown.hook.placeholder;

import be.maximvdw.placeholderapi.PlaceholderAPI;
import be.maximvdw.placeholderapi.PlaceholderReplaceEvent;
import be.maximvdw.placeholderapi.PlaceholderReplacer;
import com.SirBlobman.enderpearl.cooldown.EnderpearlCooldown;
import org.bukkit.entity.Player;

public class PlaceholderHookMVdW extends PlaceholderHook implements PlaceholderReplacer {
    public PlaceholderHookMVdW(EnderpearlCooldown plugin) {super(plugin);}

    public void register() {
        EnderpearlCooldown plugin = getPlugin();
        PlaceholderAPI.registerPlaceholder(plugin, "enderpearl_cooldown_time_left", this);
        PlaceholderAPI.registerPlaceholder(plugin, "enderpearl_cooldown_time_left_decimal", this);
    }

    @Override
    public String onPlaceholderReplace(PlaceholderReplaceEvent e) {
        String placeholder = e.getPlaceholder();
        if(!placeholder.startsWith("enderpearl_cooldown_")) return null;

        Player player = e.getPlayer();
        if(player == null) return null;

        String id = placeholder.substring("enderpearl_cooldown_".length());
        return getPlaceholder(player, id);
    }
}