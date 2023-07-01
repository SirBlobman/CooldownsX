package com.github.sirblobman.plugin.cooldown.object;

import org.bukkit.event.Cancellable;

public final class FakeCancellable implements Cancellable {
    private boolean cancelled;

    public FakeCancellable() {
        this.cancelled = false;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}
