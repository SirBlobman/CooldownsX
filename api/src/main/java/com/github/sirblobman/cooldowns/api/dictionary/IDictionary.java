package com.github.sirblobman.cooldowns.api.dictionary;

/**
 * A dictionary maps enum values into strings to display them for players.
 *
 * @param <E> The enum class for this dictionary.
 * @author SirBlobman
 */
public interface IDictionary<E extends Enum<E>> {
    /**
     * @param key The enum value to use as a key.
     * @return The dictionary entry for the key.
     */
    String get(E key);

    /**
     * Set the definition for a key.
     *
     * @param key   The enum value to use as a key.
     * @param value The dictionary entry for the key.
     */
    void set(E key, String value);

    /**
     * Save the configuration file for this dictionary.
     */
    void saveConfiguration();

    /**
     * Reload the configuration file for this dictionary.
     */
    void reloadConfiguration();
}
