package com.github.sirblobman.cooldowns.api;

import org.jetbrains.annotations.NotNull;

import com.github.sirblobman.api.configuration.ConfigurationManager;
import com.github.sirblobman.api.configuration.PlayerDataManager;
import com.github.sirblobman.api.folia.IFoliaPlugin;
import com.github.sirblobman.api.language.LanguageManager;
import com.github.sirblobman.api.nms.MultiVersionHandler;
import com.github.sirblobman.api.plugin.ConfigurablePlugin;
import com.github.sirblobman.cooldowns.api.dictionary.IDictionary;
import com.github.sirblobman.cooldowns.api.manager.ICooldownManager;
import com.github.sirblobman.api.shaded.xseries.XMaterial;
import com.github.sirblobman.api.shaded.xseries.XPotion;

/**
 * Interface for the CooldownsX plugin instance.
 *
 * @author SirBlobman
 */
public interface ICooldownsX extends IFoliaPlugin<ConfigurablePlugin> {
    /**
     * @return The Bukkit plugin instance for CooldownsX.
     */
    @NotNull ConfigurablePlugin getPlugin();

    /**
     * @return The ConfigurationManager instance for CooldownsX.
     */
    @NotNull ConfigurationManager getConfigurationManager();

    /**
     * @return The LanguageManager instance for CooldownsX
     */
    @NotNull LanguageManager getLanguageManager();

    /**
     * @return The PlayerDataManager instance for CooldownsX
     */
    @NotNull PlayerDataManager getPlayerDataManager();

    /**
     * @return The MultiVersionHandler instance for CooldownsX
     */
    @NotNull MultiVersionHandler getMultiVersionHandler();

    /**
     * @return The cooldown manager instance.
     */
    @NotNull ICooldownManager getCooldownManager();

    /**
     * @return The dictionary instance to map XMaterial keys to strings.
     */
    @NotNull IDictionary<XMaterial> getMaterialDictionary();

    /**
     * @return The dictionary instance to map XPotion keys to strings.
     */
    @NotNull IDictionary<XPotion> getPotionDictionary();

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
    void printDebug(@NotNull String message);
}
