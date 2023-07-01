package com.github.sirblobman.cooldowns.api.configuration;

import org.jetbrains.annotations.Nullable;

/**
 * Interface for CooldownsX action bar settings.
 *
 * @author SirBlobman
 */
public interface ActionBar extends Comparable<ActionBar> {
    /**
     * @return {@code true} if the action bar should be sent, otherwise {@code false}.
     */
    boolean isEnabled();

    /**
     * Action bars are ordered by priority.
     * Higher priority means it will be selected first.
     *
     * @return The priority for this action bar.
     */
    int getPriority();

    /**
     * @return The message format for this action bar.
     */
    @Nullable String getMessageFormat();
}
