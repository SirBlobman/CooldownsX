package com.github.sirblobman.cooldowns.placeholder;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;

import com.github.sirblobman.api.utility.Validate;
import com.github.sirblobman.api.xseries.XMaterial;
import com.github.sirblobman.cooldowns.CooldownPlugin;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class HookPlaceholderAPI extends PlaceholderExpansion {
    private final CooldownPlugin plugin;

    public HookPlaceholderAPI(CooldownPlugin plugin) {
        this.plugin = Validate.notNull(plugin, "plugin must not be null!");
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public boolean persist() {
        return true;
    }

    @NotNull
    @Override
    public String getIdentifier() {
        String pluginName = this.plugin.getName();
        return pluginName.toLowerCase(Locale.US);
    }

    @NotNull
    @Override
    public String getAuthor() {
        PluginDescriptionFile description = this.plugin.getDescription();
        List<String> authorList = description.getAuthors();
        return String.join(", ", authorList);
    }

    @NotNull
    @Override
    public String getVersion() {
        PluginDescriptionFile description = this.plugin.getDescription();
        return description.getVersion();
    }

    @Nullable
    @Override
    public String onPlaceholderRequest(Player player, @NotNull String placeholder) {
        if (player == null) {
            return null;
        }

        if (placeholder.startsWith("time_left_decimal_")) {
            String materialName = placeholder.substring("time_left_decimal_".length());
            Optional<XMaterial> optionalMaterial = XMaterial.matchXMaterial(materialName);
            if (optionalMaterial.isPresent()) {
                XMaterial material = optionalMaterial.get();
                return PlaceholderHelper.getTimeLeftSecondsDecimal(player, material);
            }
        } else if (placeholder.startsWith("time_left_")) {
            String materialName = placeholder.substring("time_left_".length());
            Optional<XMaterial> optionalMaterial = XMaterial.matchXMaterial(materialName);
            if (optionalMaterial.isPresent()) {
                XMaterial material = optionalMaterial.get();
                return PlaceholderHelper.getTimeLeftSeconds(player, material);
            }
        }

        return null;
    }
}
