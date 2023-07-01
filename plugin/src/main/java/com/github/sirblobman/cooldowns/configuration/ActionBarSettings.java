package com.github.sirblobman.cooldowns.configuration;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.github.sirblobman.cooldowns.api.configuration.ActionBar;

public final class ActionBarSettings implements ActionBar {
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

    @Override
    public @Nullable String getMessageFormat() {
        return this.messageFormat;
    }

    public void setMessageFormat(@Nullable String messageFormat) {
        this.messageFormat = messageFormat;
    }

    @Override
    public int compareTo(@NotNull ActionBar other) {
        int thisPriority = this.getPriority();
        int otherPriority = other.getPriority();
        return Integer.compare(thisPriority, otherPriority);
    }
}
