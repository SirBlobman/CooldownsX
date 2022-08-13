package com.github.sirblobman.cooldowns.object;

import org.jetbrains.annotations.Nullable;

public final class ActionBarSettings {
    private boolean enabled;
    private int priority;
    private String messageFormat;

    public ActionBarSettings() {
        this.enabled = false;
        this.priority = 1;
        this.messageFormat = "";
    }

    public boolean isEnabled() {
        String messageFormat = getMessageFormat();
        return (this.enabled && messageFormat != null && !messageFormat.isEmpty());
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getPriority() {
        return this.priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    @Nullable
    public String getMessageFormat() {
        return this.messageFormat;
    }

    public void setMessageFormat(@Nullable String messageFormat) {
        this.messageFormat = messageFormat;
    }
}
