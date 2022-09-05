package com.github.sirblobman.cooldowns.api.manager;

import java.util.List;

import org.bukkit.OfflinePlayer;

import com.github.sirblobman.cooldowns.api.configuration.ICooldownSettings;
import com.github.sirblobman.cooldowns.api.data.ICooldownData;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ICooldownManager {
    @NotNull ICooldownData getData(OfflinePlayer player);
    @Nullable ICooldownSettings getCooldownSettings(String id);
    @NotNull List<ICooldownSettings> getAllCooldownSettings();
    void reloadConfig();
}
