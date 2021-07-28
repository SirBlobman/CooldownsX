package com.github.sirblobman.cooldowns.object;

public final class ActionBarSettings {
    private final boolean isEnabled;
    private final int priority;
    private final String messageFormat;

    public ActionBarSettings(boolean enabled, int priority, String messageFormat) {
        this.isEnabled = enabled;
        this.priority = priority;
        this.messageFormat = messageFormat;
    }

    public static ActionBarSettings getDefaultActionBarSettings() {
        return new ActionBarSettings(false, 0, null);
    }

    public boolean isEnabled() {
        return (this.isEnabled && this.messageFormat != null && !this.messageFormat.isEmpty());
    }

    public int getPriority() {
        return this.priority;
    }

    public String getMessageFormat() {
        return this.messageFormat;
    }
}
