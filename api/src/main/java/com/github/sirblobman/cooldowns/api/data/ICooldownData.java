package com.github.sirblobman.cooldowns.api.data;

import java.util.Set;
import java.util.UUID;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import com.github.sirblobman.api.configuration.PlayerDataManager;
import com.github.sirblobman.cooldowns.api.configuration.CooldownType;
import com.github.sirblobman.cooldowns.api.configuration.ICooldownSettings;

/**
 * Interface for CooldownsX player data.
 *
 * @author SirBlobman
 */
public interface ICooldownData {
    /**
     * @return The player id that owns this data.
     */
    UUID getPlayerId();

    /**
     * @return The OfflinePlayer instance based on the player id.
     */
    OfflinePlayer getOfflinePlayer();

    /**
     * @return The Player instance based on the player id.
     */
    Player getPlayer();

    /**
     * @return A set of all cooldowns that are currently active.
     */
    Set<ICooldownSettings> getActiveCooldowns();

    /**
     * @param cooldownType A type to use as a filter.
     * @return A filtered set of active cooldowns based on the type.
     */
    Set<ICooldownSettings> getActiveCooldowns(CooldownType cooldownType);

    /**
     * @param settings The cooldown settings object.
     * @return The active cooldown expire time for the specified cooldown,
     * or {@code 0L} if the cooldown is not active.
     */
    long getCooldownExpireTime(ICooldownSettings settings);

    /**
     * Set an expire time for the specified cooldown settings.
     *
     * @param settings   The cooldown settings object.
     * @param expireTime The expire time for the cooldown.
     * @see System#currentTimeMillis()
     */
    void setCooldown(ICooldownSettings settings, long expireTime);

    /**
     * Remove an active cooldown.
     *
     * @param settings The cooldown settings object.
     */
    void removeCooldown(ICooldownSettings settings);

    /**
     * @param settings The cooldown settings object.
     * @return The amount of times the action has been executed for the cooldown.
     * For example, if the cooldown type is {@link CooldownType#CONSUME_ITEM},
     * then it will be the amount of items eaten.
     */
    int getActionCount(ICooldownSettings settings);

    /**
     * @param settings The cooldown settings object.
     * @param count    The amount of times to set.
     * @see #getActionCount(ICooldownSettings)
     */
    void setActionCount(ICooldownSettings settings, int count);

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
