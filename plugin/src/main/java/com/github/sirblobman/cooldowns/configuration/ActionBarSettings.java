package com.github.sirblobman.cooldowns.configuration;

import com.github.sirblobman.cooldowns.api.configuration.IActionBarSettings;

import org.jetbrains.annotations.Nullable;

public final class ActionBarSettings implements IActionBarSettings {
    private boolean enabled;
    private int priority;
    private String messageFormat;

    public ActionBarSettings() {
        this.enabled = false;
        this.priority = 1;
        this.messageFormat = "";
    }

    @Override
    public boolean isEnabled() {
        String messageFormat = getMessageFormat();
        return (this.enabled && messageFormat != null && !messageFormat.isEmpty());
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public int getPriority() {
        return this.priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    @Nullable
    @Override
    public String getMessageFormat() {
        return this.messageFormat;
    }

    public void setMessageFormat(@Nullable String messageFormat) {
        this.messageFormat = messageFormat;
    }

    @Override
    public int compareTo(IActionBarSettings other) {
        int thisPriority = this.getPriority();
        int otherPriority = other.getPriority();
        return Integer.compare(thisPriority, otherPriority);
    }
}
