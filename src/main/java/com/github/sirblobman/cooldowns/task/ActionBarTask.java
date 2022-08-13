package com.github.sirblobman.cooldowns.task;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.github.sirblobman.api.adventure.adventure.audience.Audience;
import com.github.sirblobman.api.adventure.adventure.platform.bukkit.BukkitAudiences;
import com.github.sirblobman.api.adventure.adventure.text.Component;
import com.github.sirblobman.api.adventure.adventure.text.minimessage.MiniMessage;
import com.github.sirblobman.api.language.LanguageManager;
import com.github.sirblobman.api.utility.Validate;
import com.github.sirblobman.api.xseries.XMaterial;
import com.github.sirblobman.cooldowns.CooldownPlugin;
import com.github.sirblobman.cooldowns.manager.CooldownManager;
import com.github.sirblobman.cooldowns.manager.MaterialDictionary;
import com.github.sirblobman.cooldowns.object.ActionBarSettings;
import com.github.sirblobman.cooldowns.object.CooldownData;
import com.github.sirblobman.cooldowns.object.CooldownSettings;

public final class ActionBarTask extends BukkitRunnable {
    private final CooldownPlugin plugin;

    public ActionBarTask(CooldownPlugin plugin) {
        this.plugin = Validate.notNull(plugin, "plugin must not be null!");
    }

    public void start() {
        CooldownPlugin plugin = getPlugin();
        runTaskTimerAsynchronously(plugin, 1L, 1L);
    }

    @Override
    public void run() {
        Collection<? extends Player> onlinePlayerCollection = Bukkit.getOnlinePlayers();
        for (Player player : onlinePlayerCollection) {
            checkActionBar(player);
        }
    }

    private CooldownPlugin getPlugin() {
        return this.plugin;
    }

    private CooldownManager getCooldownManager() {
        CooldownPlugin plugin = getPlugin();
        return plugin.getCooldownManager();
    }

    private void checkActionBar(Player player) {
        CooldownPlugin plugin = getPlugin();
        CooldownManager cooldownManager = getCooldownManager();
        CooldownData cooldownData = cooldownManager.getData(player);
        Set<XMaterial> activeCooldownSet = cooldownData.getActiveCooldowns(plugin);

        int highestPriority = Integer.MIN_VALUE;
        CooldownSettings highestSettings = null;

        for (XMaterial material : activeCooldownSet) {
            CooldownSettings cooldownSettings = cooldownManager.getCooldownSettings(material);
            if (cooldownSettings == null) {
                continue;
            }

            ActionBarSettings actionBarSettings = cooldownSettings.getActionBarSettings();
            if (actionBarSettings.isEnabled()) {
                int priority = actionBarSettings.getPriority();
                if (priority > highestPriority) {
                    highestPriority = priority;
                    highestSettings = cooldownSettings;
                }
            }
        }

        if (highestSettings != null) {
            sendActionBar(player, highestSettings);
        }
    }

    private void sendActionBar(Player player, CooldownSettings settings) {
        ActionBarSettings actionBarSettings = settings.getActionBarSettings();
        String messageFormat = actionBarSettings.getMessageFormat();
        if (messageFormat == null || messageFormat.isEmpty()) {
            return;
        }

        CooldownPlugin plugin = getPlugin();
        LanguageManager languageManager = plugin.getLanguageManager();
        BukkitAudiences audiences = languageManager.getAudiences();
        if (audiences == null) {
            return;
        }

        CooldownManager cooldownManager = getCooldownManager();
        CooldownData cooldownData = cooldownManager.getData(player);
        XMaterial material = settings.getMaterial();

        long expireMillis = cooldownData.getCooldownExpireTime(material);
        long systemMillis = System.currentTimeMillis();
        long subtractMillis = (expireMillis - systemMillis);
        long secondsLeft = TimeUnit.MILLISECONDS.toSeconds(subtractMillis);
        String secondsLeftString = Long.toString(secondsLeft);

        MaterialDictionary materialDictionary = plugin.getMaterialDictionary();
        String materialName = materialDictionary.getMaterialName(material);
        String message = messageFormat.replace("{time_left}", secondsLeftString)
                .replace("{material}", materialName);

        MiniMessage miniMessage = languageManager.getMiniMessage();
        Component component = miniMessage.deserialize(message);
        Audience audience = audiences.player(player);
        audience.sendActionBar(component);
    }
}
