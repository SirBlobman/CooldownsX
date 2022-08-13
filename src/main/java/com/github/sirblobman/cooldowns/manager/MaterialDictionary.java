package com.github.sirblobman.cooldowns.manager;

import java.util.EnumMap;
import java.util.Map;

import org.bukkit.configuration.file.YamlConfiguration;

import com.github.sirblobman.api.configuration.ConfigurationManager;
import com.github.sirblobman.api.utility.Validate;
import com.github.sirblobman.api.xseries.XMaterial;
import com.github.sirblobman.cooldowns.CooldownPlugin;

import org.jetbrains.annotations.NotNull;

public final class MaterialDictionary {
    private final CooldownPlugin plugin;
    private final Map<XMaterial, String> dictionaryMap;

    public MaterialDictionary(CooldownPlugin plugin) {
        this.plugin = Validate.notNull(plugin, "plugin must not be null!");
        this.dictionaryMap = new EnumMap<>(XMaterial.class);
    }

    @NotNull
    public String getMaterialName(XMaterial material) {
        if (material == null) {
            return getMaterialName(XMaterial.AIR);
        }

        String defaultName = material.toString();
        return this.dictionaryMap.getOrDefault(material, defaultName);
    }

    public void setMaterialName(XMaterial material, String name) {
        Validate.notNull(material, "material must not be null!");
        Validate.notEmpty(name, "name must not be empty!");
        this.dictionaryMap.put(material, name);

        ConfigurationManager configurationManager = getConfigurationManager();
        YamlConfiguration configuration = configurationManager.get("material.yml");
        configuration.set(material.name(), name);
        configurationManager.save("material.yml");
    }

    public void reloadConfig() {
        XMaterial[] materialArray = XMaterial.values();
        ConfigurationManager configurationManager = getConfigurationManager();
        YamlConfiguration configuration = configurationManager.get("material.yml");

        for (XMaterial material : materialArray) {
            String materialName = material.name();
            String defaultName = material.toString();
            if (!configuration.isSet(materialName)) {
                configuration.set(materialName, defaultName);
            }

            String dictionaryName = configuration.getString(materialName, defaultName);
            this.dictionaryMap.put(material, dictionaryName);
        }

        configurationManager.save("material.yml");
    }

    private CooldownPlugin getPlugin() {
        return this.plugin;
    }

    private ConfigurationManager getConfigurationManager() {
        CooldownPlugin plugin = getPlugin();
        return plugin.getConfigurationManager();
    }
}
