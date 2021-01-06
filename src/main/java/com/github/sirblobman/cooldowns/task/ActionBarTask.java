package com.github.sirblobman.cooldowns.task;

import java.util.Collection;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.SirBlobman.api.configuration.ConfigurationManager;
import com.SirBlobman.api.nms.MultiVersionHandler;
import com.SirBlobman.api.nms.PlayerHandler;
import com.SirBlobman.api.utility.MessageUtility;
import com.SirBlobman.api.utility.Validate;
import com.github.sirblobman.cooldowns.CooldownPlugin;
import com.github.sirblobman.cooldowns.manager.CooldownManager;
import com.github.sirblobman.cooldowns.object.CooldownData;

public final class ActionBarTask extends BukkitRunnable {
    private final CooldownPlugin plugin;
    public ActionBarTask(CooldownPlugin plugin) {
        this.plugin = Validate.notNull(plugin, "plugin must not be null!");
    }

    public void start() {
        runTaskTimerAsynchronously(this.plugin, 0L, 1L);
    }

    @Override
    public void run() {
        Collection<? extends Player> onlinePlayerCollection = Bukkit.getOnlinePlayers();
        onlinePlayerCollection.stream().filter(this::shouldShow).forEach(this::sendActionBar);
    }

    private YamlConfiguration getConfiguration() {
        ConfigurationManager configurationManager = this.plugin.getConfigurationManager();
        return configurationManager.get("config.yml");
    }

    private String getFormat() {
        YamlConfiguration configuration = getConfiguration();
        return configuration.getString("action-bar-format");
    }

    private boolean shouldShow(Player player) {
        String format = getFormat();
        if(format == null) return false;

        CooldownManager cooldownManager = this.plugin.getCooldownManager();
        CooldownData cooldownData = cooldownManager.getData(player);
        Set<Material> materialSet = cooldownData.getActiveCooldowns(plugin);

        boolean shouldShow = false;
        for(Material material : materialSet) {
            String materialName = material.name();
            String placeholder = ("{" + materialName + "}");
            if(format.contains(placeholder)) {
                shouldShow = true;
                break;
            }
        }

        return shouldShow;
    }

    private String getTimeLeft(Player player, Material material) {
        CooldownManager cooldownManager = this.plugin.getCooldownManager();
        CooldownData cooldownData = cooldownManager.getData(player);
        double expireMillis = cooldownData.getCooldownExpireTime(material);
        double systemMillis = System.currentTimeMillis();
        double millisLeft = (expireMillis - systemMillis);

        long timeLeftSeconds = (long) Math.ceil(millisLeft / 1_000.0D);
        return Long.toString(timeLeftSeconds);
    }

    private void sendActionBar(Player player) {
        String format = getFormat();
        if(format == null) return;

        CooldownManager cooldownManager = this.plugin.getCooldownManager();
        CooldownData cooldownData = cooldownManager.getData(player);
        Set<Material> materialSet = cooldownData.getActiveCooldowns(plugin);

        String finalMessage = MessageUtility.color(format);
        for(Material material : materialSet) {
            String materialName = material.name();
            String placeholder = ("{" + materialName + "}");
            String replacement = getTimeLeft(player, material);
            finalMessage = finalMessage.replace(placeholder, replacement);
        }

        MultiVersionHandler multiVersionHandler = this.plugin.getMultiVersionHandler();
        PlayerHandler playerHandler = multiVersionHandler.getPlayerHandler();
        playerHandler.sendActionBar(player, finalMessage);
    }
}
