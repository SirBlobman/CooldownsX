package com.github.sirblobman.plugin.cooldown.api;

import org.jetbrains.annotations.NotNull;

import org.bukkit.entity.EntityType;

import com.github.sirblobman.api.configuration.ConfigurationManager;
import com.github.sirblobman.api.configuration.PlayerDataManager;
import com.github.sirblobman.api.language.LanguageManager;
import com.github.sirblobman.api.plugin.ConfigurablePlugin;
import com.github.sirblobman.api.plugin.IMultiVersionPlugin;
import com.github.sirblobman.plugin.cooldown.api.configuration.EnumDictionary;
import com.github.sirblobman.plugin.cooldown.api.configuration.MainConfiguration;
import com.github.sirblobman.plugin.cooldown.api.data.PlayerCooldownManager;
import com.github.sirblobman.api.shaded.xseries.XMaterial;
import com.github.sirblobman.api.shaded.xseries.XPotion;

/**
 * Interface for the CooldownsX plugin instance.
 *
 * @author SirBlobman
 */
public interface CooldownsX extends IMultiVersionPlugin {
    /**
     * @return The Bukkit plugin instance for CooldownsX.
     */
    @NotNull ConfigurablePlugin getPlugin();

    /**
     * @return The ConfigurationManager instance for CooldownsX.
     */
    @NotNull ConfigurationManager getConfigurationManager();

    /**
     * @return The MainConfiguration instance for CooldownsX
     */
    @NotNull MainConfiguration getConfiguration();

    /**
     * @return The LanguageManager instance for CooldownsX
     */
    @NotNull LanguageManager getLanguageManager();

    /**
     * @return The PlayerDataManager instance for CooldownsX
     */
    @NotNull PlayerDataManager getPlayerDataManager();

    /**
     * @return The cooldown manager instance.
     */
    @NotNull PlayerCooldownManager getCooldownManager();

    /**
     * @return The dictionary instance to map XMaterial keys to strings.
     */
    @NotNull EnumDictionary<XMaterial> getMaterialDictionary();

    /**
     * @return The dictionary instance to map XPotion keys to strings.
     */
    @NotNull EnumDictionary<XPotion> getPotionDictionary();

    /**
     * @return The dictionary instance to map EntityType keys to strings.
     */
    @NotNull EnumDictionary<EntityType> getEntityDictionary();

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
