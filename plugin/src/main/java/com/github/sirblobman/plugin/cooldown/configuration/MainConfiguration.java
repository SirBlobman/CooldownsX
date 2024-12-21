package com.github.sirblobman.plugin.cooldown.configuration;

import org.jetbrains.annotations.NotNull;

import org.bukkit.configuration.ConfigurationSection;

import com.github.sirblobman.api.configuration.IConfigurable;

public final class MainConfiguration implements IConfigurable {
    private boolean debugMode;
    private boolean useActionBar;
    private boolean saveAmountsUsed;

    private final PotionTriggers potionTriggers;

    public MainConfiguration() {
        this.potionTriggers = new PotionTriggers();
    }

    @Override
    public void load(@NotNull ConfigurationSection config) {
        setDebugMode(config.getBoolean("debug-mode", false));
        setSaveAmountsUsed(config.getBoolean("save-amounts-used", true));
        setUseActionBar(config.getBoolean("use-action-bar",true));

        ConfigurationSection potionTriggersSection = getOrCreateSection(config, "potion-triggers");
        this.potionTriggers.load(potionTriggersSection);
    }

    public boolean isDebugMode() {
        return this.debugMode;
    }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    public boolean isUseActionBar() {
        return this.useActionBar;
    }

    public void setUseActionBar(boolean useActionBar) {
        this.useActionBar = useActionBar;
    }

    public boolean isSaveAmountsUsed() {
        return this.saveAmountsUsed;
    }

    public void setSaveAmountsUsed(boolean saveAmountsUsed) {
        this.saveAmountsUsed = saveAmountsUsed;
    }

    public @NotNull PotionTriggers getPotionTriggers() {
        return this.potionTriggers;
    }
}
