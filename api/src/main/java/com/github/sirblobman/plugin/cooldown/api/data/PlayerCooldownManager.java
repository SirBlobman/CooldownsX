package com.github.sirblobman.plugin.cooldown.api.data;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.bukkit.OfflinePlayer;

import com.github.sirblobman.plugin.cooldown.api.configuration.Cooldown;

/**
 * Interface for the CooldownsX Cooldown Manager instance.
 * The cooldown manager stores player data and cooldown settings by id.
 *
 * @author SirBlobman
 */
public interface PlayerCooldownManager {
    /**
     * Get or create the cooldown data for a player.
     *
     * @param player The player that will own the data.
     * @return New or existing cooldown data for the specified player.
     */
    @NotNull PlayerCooldown getData(@NotNull OfflinePlayer player);

    /**
     * @param id The id of the settings in the configuration file.
     * @return A cooldown settings object if one matches the id, otherwise {@code null}.
     */
    @Nullable Cooldown getCooldownSettings(@NotNull String id);

    /**
     * @return A list of all cooldown settings that are currently loaded.
     */
    @NotNull List<Cooldown> getAllCooldownSettings();

    /**
     * Reloads all cooldowns from the configuration file.
     * Player data will not be reloaded.
     */
    void reloadConfig();
}
