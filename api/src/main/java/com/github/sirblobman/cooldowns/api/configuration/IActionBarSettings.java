package com.github.sirblobman.cooldowns.api.configuration;

public interface IActionBarSettings extends Comparable<IActionBarSettings>{
    boolean isEnabled();
    int getPriority();
    String getMessageFormat();
}
