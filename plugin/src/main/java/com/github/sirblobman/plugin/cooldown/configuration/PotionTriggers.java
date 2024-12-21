package com.github.sirblobman.plugin.cooldown.configuration;

import org.jetbrains.annotations.NotNull;

import org.bukkit.configuration.ConfigurationSection;

import com.github.sirblobman.api.configuration.IConfigurable;

public class PotionTriggers implements IConfigurable {
    private boolean beacons;
    private boolean tippedArrows;

    @Override
    public void load(@NotNull ConfigurationSection config) {
        setBeacons(config.getBoolean("beacons", true));
        setTippedArrows(config.getBoolean("tipped-arrows", true));
    }

    public boolean getBeacons() {
        return this.beacons;
    }

    public void setBeacons(boolean beacons) {
        this.beacons = beacons;
    }

    public boolean getTippedArrows() {
        return this.tippedArrows;
    }

    public void setTippedArrows(boolean tippedArrows) {
        this.tippedArrows = tippedArrows;
    }
}
