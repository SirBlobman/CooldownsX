package com.github.sirblobman.plugin.cooldown.api.configuration;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.NotNull;

import org.bukkit.configuration.ConfigurationSection;

import com.github.sirblobman.api.configuration.IConfigurable;

public final class MainConfiguration implements IConfigurable {
    private boolean debugMode;
    private boolean useActionBar;
    private boolean saveAmountsUsed;
    private final Set<String> potionTriggerSet;

    public MainConfiguration() {
        potionTriggerSet = new HashSet<>();
    }

    @Override
    public void load(@NotNull ConfigurationSection config) {
        setDebugMode(config.getBoolean("debug-mode", false));
        setSaveAmountsUsed(config.getBoolean("save-amounts-used", true));
        setUseActionBar(config.getBoolean("use-action-bar",true));

        List<String> potionTriggerSet = config.getStringList("potion-triggers");
        setPotionTriggers(potionTriggerSet);
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

    public void setPotionTriggers(Collection<String> potionTriggerSet) {
        this.potionTriggerSet.clear();
        this.potionTriggerSet.addAll(potionTriggerSet);
    }

    public @NotNull Set<String> getPotionTriggers() {
        return Collections.unmodifiableSet(this.potionTriggerSet);
    }

    public boolean isPotionTrigger(@NotNull Enum<?> enumValue) {
        String enumName = enumValue.name();
        Set<String> potionTriggerSet = getPotionTriggers();
        return potionTriggerSet.contains(enumName);
    }
}
