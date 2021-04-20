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

    public void register() {
        XMaterial[] materialArray = XMaterial.values();
        for(XMaterial material : materialArray) {
            String materialName = material.name();
            String timeLeftInteger = ("cooldownsx_time_left_" + materialName);
            String timeLeftDecimal = ("cooldownsx_time_left_decimal_" + materialName);
            PlaceholderAPI.registerPlaceholder(this.plugin, timeLeftInteger, this);
            PlaceholderAPI.registerPlaceholder(this.plugin, timeLeftDecimal, this);
        }

        PlaceholderAPI.registerPlaceholder(this.plugin, "cooldownsx_undying_time_left", this);
        PlaceholderAPI.registerPlaceholder(this.plugin, "cooldownsx_undying_time_left_decimal", this);
    }

    @Override
    public String onPlaceholderReplace(PlaceholderReplaceEvent e) {
        Player player = e.getPlayer();
        if(player == null) return null;

        String placeholder = e.getPlaceholder();
        if(placeholder.startsWith("cooldownsx_time_left_decimal_")) {
            String materialName = placeholder.substring("cooldownsx_time_left_decimal_".length());
            Optional<XMaterial> optionalMaterial = XMaterial.matchXMaterial(materialName);
            if(optionalMaterial.isPresent()) {
                XMaterial material = optionalMaterial.get();
                return PlaceholderHelper.getTimeLeftDecimal(player, material);
            }

            return null;
        }

        if(placeholder.startsWith("cooldownsx_time_left_")) {
            String materialName = placeholder.substring("cooldownsx_time_left_".length());
            Optional<XMaterial> optionalMaterial = XMaterial.matchXMaterial(materialName);
            if(optionalMaterial.isPresent()) {
                XMaterial material = optionalMaterial.get();
                return PlaceholderHelper.getTimeLeftInteger(player, material);
            }

            return null;
        }

        switch(placeholder) {
            case "cooldownsx_undying_time_left_decimal": return PlaceholderHelper.getUndyingTimeLeftDecimal(player);
            case "cooldownsx_undying_time_left": return PlaceholderHelper.getUndyingTimeLeftInteger(player);
            default: break;
        }

        return null;
    }
}