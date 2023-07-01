package com.github.sirblobman.plugin.cooldown.dictionary;

import java.util.EnumMap;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

import org.bukkit.configuration.file.YamlConfiguration;

import com.github.sirblobman.api.configuration.ConfigurationManager;
import com.github.sirblobman.plugin.cooldown.api.CooldownsX;
import com.github.sirblobman.plugin.cooldown.api.configuration.EnumDictionary;

public abstract class Dictionary<E extends Enum<E>> implements EnumDictionary<E> {
    private final CooldownsX plugin;
    private final String fileName;
    private final Class<E> enumClass;
    private final Map<E, String> dictionary;

    public Dictionary(@NotNull CooldownsX plugin, @NotNull String fileName, @NotNull Class<E> enumClass) {
        this.plugin = plugin;
        this.fileName = fileName;
        this.enumClass = enumClass;
        this.dictionary = new EnumMap<>(this.enumClass);
    }

    protected final @NotNull CooldownsX getPlugin() {
        return this.plugin;
    }

    protected final @NotNull String getFileName() {
        return this.fileName;
    }

    protected final @NotNull Class<E> getEnumClass() {
        return this.enumClass;
    }

    protected final @NotNull ConfigurationManager getConfigurationManager() {
        CooldownsX plugin = getPlugin();
        return plugin.getConfigurationManager();
    }

    protected final @NotNull YamlConfiguration getConfiguration() {
        String fileName = getFileName();
        ConfigurationManager configurationManager = getConfigurationManager();
        return configurationManager.get(fileName);
    }

    @Override
    public @NotNull String get(@NotNull E key) {
        String defaultName = key.toString();
        return this.dictionary.getOrDefault(key, defaultName);
    }

    @Override
    public void set(@NotNull E key, @NotNull String value) {
        this.dictionary.put(key, value);
    }

    @Override
    public void saveConfiguration() {
        YamlConfiguration configuration = getConfiguration();
        Class<E> enumClass = getEnumClass();
        E[] keys = enumClass.getEnumConstants();

        for (E key : keys) {
            String keyName = key.name();
            String keyValue = get(key);
            configuration.set(keyName, keyValue);
        }

        String fileName = getFileName();
        ConfigurationManager configurationManager = getConfigurationManager();
        configurationManager.save(fileName);
    }

    @Override
    public void reloadConfiguration() {
        this.dictionary.clear();

        YamlConfiguration configuration = getConfiguration();
        Class<E> enumClass = getEnumClass();
        E[] keys = enumClass.getEnumConstants();

        for (E key : keys) {
            String keyName = key.name();
            if (!configuration.isString(keyName)) {
                continue;
            }

            String keyDefaultValue = key.toString();
            String keyValue = configuration.getString(keyName, keyDefaultValue);
            set(key, keyValue);
        }
    }
}
