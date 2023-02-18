package com.github.sirblobman.cooldowns.placeholder;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;

import com.github.sirblobman.api.language.LanguageManager;
import com.github.sirblobman.api.utility.Validate;
import com.github.sirblobman.cooldowns.CooldownPlugin;
import com.github.sirblobman.cooldowns.api.data.ICooldownData;
import com.github.sirblobman.cooldowns.configuration.CooldownSettings;
import com.github.sirblobman.cooldowns.manager.CooldownManager;

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
        CooldownPlugin plugin = getCooldownPlugin();
        String pluginName = plugin.getName();
        return pluginName.toLowerCase(Locale.US);
    }

    @NotNull
    @Override
    public String getAuthor() {
        CooldownPlugin plugin = getCooldownPlugin();
        PluginDescriptionFile description = plugin.getDescription();
        List<String> authorList = description.getAuthors();
        return String.join(", ", authorList);
    }

    @NotNull
    @Override
    public String getVersion() {
        CooldownPlugin plugin = getCooldownPlugin();
        PluginDescriptionFile description = plugin.getDescription();
        return description.getVersion();
    }

    @Nullable
    @Override
    public String onPlaceholderRequest(Player player, @NotNull String placeholder) {
        if (player == null) {
            return null;
        }

        if (!placeholder.startsWith("time_left_")) {
            return null;
        }

        String subPlaceholder = placeholder.substring("time_left_".length());
        if (subPlaceholder.startsWith("decimal_")) {
            String id = subPlaceholder.substring("decimal_".length());
            return getTimeLeftDecimal(player, id);
        }

        return getTimeLeftInteger(player, subPlaceholder);
    }

    @NotNull
    private CooldownPlugin getCooldownPlugin() {
        return this.plugin;
    }

    @NotNull
    private LanguageManager getLanguageManager() {
        CooldownPlugin plugin = getCooldownPlugin();
        return plugin.getLanguageManager();
    }

    @NotNull
    private CooldownManager getCooldownManager() {
        CooldownPlugin plugin = getCooldownPlugin();
        return plugin.getCooldownManager();
    }

    @NotNull
    private ICooldownData getCooldownData(Player player) {
        CooldownManager cooldownManager = getCooldownManager();
        return cooldownManager.getData(player);
    }

    @Nullable
    private CooldownSettings getCooldownSettings(String id) {
        CooldownManager cooldownManager = getCooldownManager();
        return cooldownManager.getCooldownSettings(id);
    }

    @Nullable
    private String getTimeLeftDecimal(Player player, String id) {
        CooldownSettings settings = getCooldownSettings(id);
        if (settings == null) {
            return null;
        }

        ICooldownData cooldownData = getCooldownData(player);
        double expireTimeMillis = cooldownData.getCooldownExpireTime(settings);
        double systemTimeMillis = System.currentTimeMillis();
        double timeLeftMillis = Math.max(0.0D, expireTimeMillis - systemTimeMillis);
        double timeLeftSeconds = (timeLeftMillis / 1_000.0D);

        LanguageManager languageManager = getLanguageManager();
        DecimalFormat decimalFormat = languageManager.getDecimalFormat(player);
        return decimalFormat.format(timeLeftSeconds);
    }

    @Nullable
    private String getTimeLeftInteger(Player player, String id) {
        CooldownSettings settings = getCooldownSettings(id);
        if (settings == null) {
            return null;
        }

        ICooldownData cooldownData = getCooldownData(player);
        long expireTimeMillis = cooldownData.getCooldownExpireTime(settings);
        long systemTimeMillis = System.currentTimeMillis();
        long timeLeftMillis = Math.max(0L, expireTimeMillis - systemTimeMillis);
        long timeLeftSeconds = TimeUnit.MILLISECONDS.toSeconds(timeLeftMillis);
        return Long.toString(timeLeftSeconds);
    }
}
