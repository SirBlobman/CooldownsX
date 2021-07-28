package com.github.sirblobman.cooldowns.placeholder;

import java.util.Optional;

import org.bukkit.entity.Player;

import com.github.sirblobman.api.utility.Validate;
import com.github.sirblobman.api.xseries.XMaterial;
import com.github.sirblobman.cooldowns.CooldownPlugin;

import be.maximvdw.placeholderapi.PlaceholderAPI;
import be.maximvdw.placeholderapi.PlaceholderReplaceEvent;
import be.maximvdw.placeholderapi.PlaceholderReplacer;

public final class HookMVdWPlaceholderAPI implements PlaceholderReplacer {
    private final CooldownPlugin plugin;

    public HookMVdWPlaceholderAPI(CooldownPlugin plugin) {
        this.plugin = Validate.notNull(plugin, "plugin must not be null!");
    }

    public final CooldownPlugin getPlugin() {
        return this.plugin;
    }

    public void register() {
        CooldownPlugin plugin = getPlugin();
        PlaceholderAPI.registerPlaceholder(plugin, "cooldownsx_*", this);
    }

    @Override
    public String onPlaceholderReplace(PlaceholderReplaceEvent e) {
        Player player = e.getPlayer();
        if(player == null) return null;

        String id = e.getPlaceholder();
        if(!id.startsWith("cooldownsx_")) return null;

        String placeholder = id.substring("cooldownsx_".length());
        if(placeholder.startsWith("time_left_decimal_")) {
            String materialName = placeholder.substring("time_left_decimal_".length());
            Optional<XMaterial> optionalMaterial = XMaterial.matchXMaterial(materialName);
            if(optionalMaterial.isPresent()) {
                XMaterial material = optionalMaterial.get();
                return PlaceholderHelper.getTimeLeftDecimal(player, material);
            }

            return null;
        }

        if(placeholder.startsWith("time_left_")) {
            String materialName = placeholder.substring("time_left_".length());
            Optional<XMaterial> optionalMaterial = XMaterial.matchXMaterial(materialName);
            if(optionalMaterial.isPresent()) {
                XMaterial material = optionalMaterial.get();
                return PlaceholderHelper.getTimeLeftInteger(player, material);
            }

            return null;
        }

        switch(placeholder) {
            case "undying_time_left_decimal": return PlaceholderHelper.getUndyingTimeLeftDecimal(player);
            case "undying_time_left": return PlaceholderHelper.getUndyingTimeLeftInteger(player);
            default: break;
        }

        return null;
    }
}
