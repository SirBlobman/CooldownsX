package com.github.sirblobman.cooldowns.api;

import org.bukkit.plugin.java.JavaPlugin;

import com.github.sirblobman.api.configuration.ConfigurationManager;
import com.github.sirblobman.api.language.LanguageManager;
import com.github.sirblobman.api.nms.MultiVersionHandler;
import com.github.sirblobman.api.xseries.XMaterial;
import com.github.sirblobman.api.xseries.XPotion;
import com.github.sirblobman.cooldowns.api.dictionary.IDictionary;
import com.github.sirblobman.cooldowns.api.manager.ICooldownManager;

public interface ICooldownsX {
    JavaPlugin getPlugin();

    ConfigurationManager getConfigurationManager();
    LanguageManager getLanguageManager();
    MultiVersionHandler getMultiVersionHandler();

    ICooldownManager getCooldownManager();
    IDictionary<XMaterial> getMaterialDictionary();
    IDictionary<XPotion> getPotionDictionary();

    boolean isDebugMode();
    void printDebug(String message);
}
