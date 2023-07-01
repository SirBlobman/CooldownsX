package com.github.sirblobman.plugin.cooldown.task;

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.github.sirblobman.api.language.LanguageManager;
import com.github.sirblobman.plugin.cooldown.api.CooldownsX;
import com.github.sirblobman.plugin.cooldown.api.configuration.ActionBar;
import com.github.sirblobman.plugin.cooldown.api.configuration.Cooldown;
import com.github.sirblobman.plugin.cooldown.api.data.PlayerCooldown;
import com.github.sirblobman.api.shaded.adventure.text.Component;
import com.github.sirblobman.api.shaded.adventure.text.minimessage.MiniMessage;

public final class ActionBarTask extends CooldownTask {
    public ActionBarTask(@NotNull CooldownsX plugin) {
        super(plugin);
    }

    @Override
    public void run() {
        Collection<? extends Player> onlinePlayerCollection = Bukkit.getOnlinePlayers();
        for (Player player : onlinePlayerCollection) {
            checkActionBar(player);
        }
    }

    private void checkActionBar(@NotNull Player player) {
        PlayerCooldown cooldownData = getCooldownData(player);
        Set<Cooldown> activeCooldowns = cooldownData.getActiveCooldowns();
        if (activeCooldowns.isEmpty()) {
            return;
        }

        List<Cooldown> cooldownSettingsList = activeCooldowns.parallelStream()
                .filter(settings -> settings.getActionBarSettings().isEnabled())
                .sorted(Comparator.comparing(Cooldown::getActionBarSettings).reversed())
                .collect(Collectors.toList());
        if (cooldownSettingsList.isEmpty()) {
            return;
        }

        Cooldown cooldownSettings = cooldownSettingsList.get(0);
        sendActionBar(player, cooldownSettings);
    }

    private void sendActionBar(@NotNull Player player, @NotNull Cooldown settings) {
        ActionBar actionBarSettings = settings.getActionBarSettings();
        String messageFormat = actionBarSettings.getMessageFormat();
        if (messageFormat == null || messageFormat.isEmpty()) {
            return;
        }

        PlayerCooldown cooldownData = getCooldownData(player);
        double expireTimeMillis = cooldownData.getCooldownExpireTime(settings);
        double systemTimeMillis = System.currentTimeMillis();
        double timeLeftMillis = Math.max(0.0D, expireTimeMillis - systemTimeMillis);
        double timeLeftSeconds = (timeLeftMillis / 1_000.0D);
        long timeLeftSecondsInteger = Math.round(timeLeftSeconds);

        LanguageManager languageManager = getLanguageManager();
        MiniMessage miniMessage = languageManager.getMiniMessage();
        String timeLeftInteger = Long.toString(timeLeftSecondsInteger);

        DecimalFormat decimalFormat = languageManager.getDecimalFormat(player);
        String timeLeftDecimal = decimalFormat.format(timeLeftSeconds);

        String messageString = messageFormat.replace("{time_left}", timeLeftInteger)
                .replace("{time_left_decimal}", timeLeftDecimal);
        Component message = miniMessage.deserialize(messageString);
        languageManager.sendActionBar(player, message);
    }
}
