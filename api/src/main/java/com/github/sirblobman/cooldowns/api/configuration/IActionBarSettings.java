package com.github.sirblobman.cooldowns.api.configuration;

public interface IActionBarSettings extends Comparable<IActionBarSettings> {
    /**
     * @return {@code true} if the action bar should be sent, otherwise {@code false}.
     */
    boolean isEnabled();

    int getPriority();

    String getMessageFormat();
}
