package com.github.sirblobman.cooldowns.task;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.github.sirblobman.api.adventure.adventure.text.Component;
import com.github.sirblobman.api.adventure.adventure.text.minimessage.MiniMessage;
import com.github.sirblobman.api.language.LanguageManager;
import com.github.sirblobman.cooldowns.CooldownPlugin;
import com.github.sirblobman.cooldowns.api.configuration.IActionBarSettings;
import com.github.sirblobman.cooldowns.api.configuration.ICooldownSettings;
import com.github.sirblobman.cooldowns.api.data.ICooldownData;

public final class ActionBarTask extends CooldownTask {
    public ActionBarTask(CooldownPlugin plugin) {
        super(plugin);
    }

    @Override
    public void run() {
        Collection<? extends Player> onlinePlayerCollection = Bukkit.getOnlinePlayers();
        for (Player player : onlinePlayerCollection) {
            printDebug("Checking action bar for player '" + player.getName() + "'.");
            checkActionBar(player);
        }
    }

    private void checkActionBar(Player player) {
        ICooldownData cooldownData = getCooldownData(player);
        Set<ICooldownSettings> activeCooldowns = cooldownData.getActiveCooldowns();
        if (activeCooldowns.isEmpty()) {
            printDebug("Player does not have any active cooldowns.");
            return;
        }

        List<ICooldownSettings> cooldownSettingsList = activeCooldowns.parallelStream()
                .filter(settings -> settings.getActionBarSettings().isEnabled())
                .sorted(Comparator.comparing(ICooldownSettings::getActionBarSettings).reversed())
                .collect(Collectors.toList());
        if (cooldownSettingsList.isEmpty()) {
            printDebug("Player active cooldowns don't have any action bars enabled.");
            return;
        }

        ICooldownSettings cooldownSettings = cooldownSettingsList.get(0);
        sendActionBar(player, cooldownSettings);
        printDebug("Sent action bar to player.");
    }

    private void sendActionBar(Player player, ICooldownSettings settings) {
        IActionBarSettings actionBarSettings = settings.getActionBarSettings();
        String messageFormat = actionBarSettings.getMessageFormat();
        if (messageFormat == null || messageFormat.isEmpty()) {
            return;
        }

        ICooldownData cooldownData = getCooldownData(player);
        double expireTimeMillis = cooldownData.getCooldownExpireTime(settings);
        double systemTimeMillis = System.currentTimeMillis();
        double timeLeftMillis = Math.max(0.0D, expireTimeMillis - systemTimeMillis);
        double timeLeftSeconds = (timeLeftMillis / 1_000.0D);
        long timeLeftSecondsInteger = Math.round(timeLeftSeconds);

        LanguageManager languageManager = getLanguageManager();
        MiniMessage miniMessage = languageManager.getMiniMessage();
        String timeLeftInteger = Long.toString(timeLeftSecondsInteger);
        String timeLeftDecimal = languageManager.formatDecimal(player, timeLeftSeconds);

        String messageString = messageFormat.replace("{time_left}", timeLeftInteger)
                .replace("{time_left_decimal}", timeLeftDecimal);
        Component message = miniMessage.deserialize(messageString);
        languageManager.sendActionBar(player, message);
    }
}
