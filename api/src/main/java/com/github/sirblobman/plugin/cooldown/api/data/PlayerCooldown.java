package com.github.sirblobman.plugin.cooldown.api.data;

import java.util.Set;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import com.github.sirblobman.api.configuration.PlayerDataManager;
import com.github.sirblobman.plugin.cooldown.api.configuration.Cooldown;
import com.github.sirblobman.plugin.cooldown.api.configuration.CooldownType;

/**
 * Interface for CooldownsX player data.
 *
 * @author SirBlobman
 */
public interface PlayerCooldown {
    /**
     * @return The player id that owns this data.
     */
    @NotNull UUID getPlayerId();

    /**
     * @return The OfflinePlayer instance based on the player id.
     */
    @NotNull OfflinePlayer getOfflinePlayer();

    /**
     * @return The Player instance based on the player id.
     */
    @Nullable Player getPlayer();

    /**
     * @return A set of all cooldowns that are currently active.
     */
    @NotNull Set<Cooldown> getActiveCooldowns();

    /**
     * @param cooldownType A type to use as a filter.
     * @return A filtered set of active cooldowns based on the type.
     */
    @NotNull Set<Cooldown> getActiveCooldowns(@NotNull CooldownType cooldownType);

    /**
     * @param settings The cooldown settings object.
     * @return The active cooldown expire time for the specified cooldown,
     * or {@code 0L} if the cooldown is not active.
     */
    long getCooldownExpireTime(@NotNull Cooldown settings);

    /**
     * Set an expire time for the specified cooldown settings.
     *
     * @param settings   The cooldown settings object.
     * @param expireTime The expire time for the cooldown.
     * @see System#currentTimeMillis()
     */
    void setCooldown(@NotNull Cooldown settings, long expireTime);

    /**
     * Remove an active cooldown.
     *
     * @param settings The cooldown settings object.
     */
    void removeCooldown(@NotNull Cooldown settings);

    /**
     * @param settings The cooldown settings object.
     * @return The amount of times the action has been executed for the cooldown.
     * For example, if the cooldown type is {@link CooldownType#CONSUME_ITEM},
     * then it will be the amount of items eaten.
     */
    int getActionCount(@NotNull Cooldown settings);

    /**
     * @param settings The cooldown settings object.
     * @param count    The amount of times to set.
     * @see #getActionCount(Cooldown)
     */
    void setActionCount(@NotNull Cooldown settings, int count);

    /**
     * Load all the action counts from the player data file.
     *
     * @see #getPlayerId()
     * @see PlayerDataManager
     */
    void loadActionCounts();

    /**
     * Save all the stored action counts to the player data file.
     *
     * @see #getPlayerId()
     * @see PlayerDataManager
     */
    void saveActionCounts();
}
