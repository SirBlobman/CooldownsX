package com.SirBlobman.enderpearl.cooldown.task;

import com.SirBlobman.api.nms.NMS_Handler;
import com.SirBlobman.enderpearl.cooldown.EnderpearlCooldown;
import com.SirBlobman.enderpearl.cooldown.api.ECooldownAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.text.DecimalFormat;
import java.util.List;
import java.util.stream.Collectors;

public class EnderpearlCooldownTask extends BukkitRunnable {
    private final EnderpearlCooldown plugin;
    public EnderpearlCooldownTask(EnderpearlCooldown plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        NMS_Handler nmsHandler = NMS_Handler.getHandler();
        List<Player> playerList = Bukkit.getOnlinePlayers().stream().filter(ECooldownAPI::isInCooldown).collect(Collectors.toList());

        for(Player player : playerList) {
            int timeLeft = ECooldownAPI.getTimeLeftSeconds(player);
            if(timeLeft <= 0) {
                ECooldownAPI.removeFromCooldown(player);
                String message = this.plugin.getConfigMessage("action bar.end timer");
                nmsHandler.sendActionBar(player, message);
                continue;
            }

            long timeLeftMillis = ECooldownAPI.getTimeLeftMillis(player);

            String timeLeftNormal = Integer.toString(timeLeft);
            String timeLeftDecimal = getDecimalTimeLeft(timeLeftMillis);

            String message = this.plugin.getConfigMessage("action bar.timer")
                    .replace("{time_left}", timeLeftNormal)
                    .replace("{time_left_decimal}", timeLeftDecimal);
            nmsHandler.sendActionBar(player, message);
        }
    }

    private String getDecimalTimeLeft(long millis) {
        double seconds = ((double) millis / 1000.0D);
        DecimalFormat format = new DecimalFormat("0.0");
        return format.format(seconds);
    }
}