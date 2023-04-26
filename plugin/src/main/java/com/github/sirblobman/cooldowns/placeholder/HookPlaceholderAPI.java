package com.github.sirblobman.cooldowns.placeholder;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.sirblobman.api.language.LanguageManager;
import com.github.sirblobman.cooldowns.api.ICooldownsX;
import com.github.sirblobman.cooldowns.api.configuration.ICooldownSettings;
import com.github.sirblobman.cooldowns.api.data.ICooldownData;
import com.github.sirblobman.cooldowns.api.manager.ICooldownManager;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;

public final class HookPlaceholderAPI extends PlaceholderExpansion {
    private final ICooldownsX plugin;

    public HookPlaceholderAPI(@NotNull ICooldownsX plugin) {
        this.plugin = plugin;
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
    public @NotNull String getIdentifier() {
        JavaPlugin plugin = getJavaPlugin();
        String pluginName = plugin.getName();
        return pluginName.toLowerCase(Locale.US);
    }

    @Override
    public @NotNull String getAuthor() {
        JavaPlugin plugin = getJavaPlugin();
        PluginDescriptionFile description = plugin.getDescription();
        List<String> authorList = description.getAuthors();
        return String.join(", ", authorList);
    }

    @Override
    public @NotNull String getVersion() {
        JavaPlugin plugin = getJavaPlugin();
        PluginDescriptionFile description = plugin.getDescription();
        return description.getVersion();
    }

    @Override
    public @Nullable String onPlaceholderRequest(@Nullable Player player, @NotNull String placeholder) {
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

    private @NotNull ICooldownsX getCooldownsX() {
        return this.plugin;
    }

    private @NotNull JavaPlugin getJavaPlugin() {
        ICooldownsX plugin = getCooldownsX();
        return plugin.getPlugin();
    }

    private @NotNull LanguageManager getLanguageManager() {
        ICooldownsX plugin = getCooldownsX();
        return plugin.getLanguageManager();
    }

    private @NotNull ICooldownManager getCooldownManager() {
        ICooldownsX plugin = getCooldownsX();
        return plugin.getCooldownManager();
    }

    private @NotNull ICooldownData getCooldownData(@NotNull Player player) {
        ICooldownManager manager = getCooldownManager();
        return manager.getData(player);
    }

    private @Nullable ICooldownSettings getCooldownSettings(@NotNull String id) {
        ICooldownManager manager = getCooldownManager();
        return manager.getCooldownSettings(id);
    }

    @Nullable
    private String getTimeLeftDecimal(@NotNull Player player, @NotNull String id) {
        ICooldownSettings settings = getCooldownSettings(id);
        if (settings == null) {
            return null;
        }

        ICooldownData data = getCooldownData(player);
        double expireTimeMillis = data.getCooldownExpireTime(settings);
        double systemTimeMillis = System.currentTimeMillis();
        double timeLeftMillis = Math.max(0.0D, expireTimeMillis - systemTimeMillis);
        double timeLeftSeconds = (timeLeftMillis / 1_000.0D);

        LanguageManager languageManager = getLanguageManager();
        DecimalFormat decimalFormat = languageManager.getDecimalFormat(player);
        return decimalFormat.format(timeLeftSeconds);
    }

    private @Nullable String getTimeLeftInteger(@NotNull Player player, @NotNull String id) {
        ICooldownSettings settings = getCooldownSettings(id);
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
