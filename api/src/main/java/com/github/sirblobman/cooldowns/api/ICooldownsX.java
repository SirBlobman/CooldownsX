package com.github.sirblobman.cooldowns.api;

import org.bukkit.plugin.java.JavaPlugin;

import com.github.sirblobman.api.configuration.ConfigurationManager;
import com.github.sirblobman.api.language.LanguageManager;
import com.github.sirblobman.api.nms.MultiVersionHandler;
import com.github.sirblobman.api.xseries.XMaterial;
import com.github.sirblobman.api.xseries.XPotion;
import com.github.sirblobman.cooldowns.api.dictionary.IDictionary;
import com.github.sirblobman.cooldowns.api.manager.ICooldownManager;

/**
 * Interface for the CooldownsX plugin instance.
 *
 * @author SirBlobman
 */
public interface ICooldownsX {
    /**
     * @return The Bukkit plugin instance for CooldownsX.
     */
    JavaPlugin getPlugin();

    /**
     * @return The ConfigurationManager instance for CooldownsX.
     */
    ConfigurationManager getConfigurationManager();

    /**
     * @return The LanguageManager instance for CooldownsX
     */
    LanguageManager getLanguageManager();

    /**
     * @return The MultiVersionHandler instance for CooldownsX
     */
    MultiVersionHandler getMultiVersionHandler();

    /**
     * @return The cooldown manager instance.
     */
    ICooldownManager getCooldownManager();

    /**
     * @return The dictionary instance to map XMaterial keys to strings.
     */
    IDictionary<XMaterial> getMaterialDictionary();

    /**
     * @return The dictionary instance to map XPotion keys to strings.
     */
    IDictionary<XPotion> getPotionDictionary();

    /**
     * @return {@code true} if the plugin has debug-mode enabled, otherwise {@code false}.
     */
    boolean isDebugMode();

    /**
     * Send a debug message to the server log.
     * This method will not do anything when {@link #isDebugMode()} returns {@code false}.
     *
     * @param message The message to print to the log.
     */
    void printDebug(String message);
}
