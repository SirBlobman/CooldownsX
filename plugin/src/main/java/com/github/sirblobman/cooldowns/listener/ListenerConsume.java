package com.github.sirblobman.cooldowns.listener;

import java.util.Set;

import org.jetbrains.annotations.NotNull;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

import com.github.sirblobman.api.utility.ItemUtility;
import com.github.sirblobman.cooldowns.api.CooldownsX;
import com.github.sirblobman.cooldowns.api.configuration.Cooldown;
import com.github.sirblobman.cooldowns.api.configuration.CooldownType;
import com.github.sirblobman.cooldowns.api.data.PlayerCooldown;
import com.github.sirblobman.cooldowns.api.listener.CooldownListener;
import com.github.sirblobman.api.shaded.xseries.XMaterial;

public final class ListenerConsume extends CooldownListener {
    public ListenerConsume(@NotNull CooldownsX plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onConsume(PlayerItemConsumeEvent e) {
        printDebug("Detected PlayerItemConsumeEvent...");

        ItemStack item = e.getItem();
        if (ItemUtility.isAir(item)) {
            printDebug("item is air/null, ignoring.");
            return;
        }

        Player player = e.getPlayer();
        printDebug("Player: " + player.getName());

        XMaterial material = XMaterial.matchXMaterial(item);
        checkFood(player, material, e);
    }

    private void checkFood(@NotNull Player player, @NotNull XMaterial material, @NotNull PlayerItemConsumeEvent e) {
        printDebug("Checking consume food for player " + player.getName() + "...");
        Set<Cooldown> cooldownSettingsList = fetchCooldowns(CooldownType.CONSUME_ITEM);
        if (cooldownSettingsList.isEmpty()) {
            return;
        }

        PlayerCooldown cooldownData = getCooldownData(player);
        Set<Cooldown> allActiveCooldowns = cooldownData.getActiveCooldowns(CooldownType.CONSUME_ITEM);
        Set<Cooldown> activeCooldowns = filter(allActiveCooldowns, material);

        Cooldown activeCooldown = checkActiveCooldowns(player, activeCooldowns);
        if (activeCooldown != null) {
            printDebug("Found active cooldown '" + activeCooldown.getId() + "for consume.");
            e.setCancelled(true);
            sendCooldownMessage(player, activeCooldown, material);
            printDebug("Cancelled event and sent message to player.");
            updateInventoryLater(player);
            printDebug("Triggered player inventory update for one tick later.");
            return;
        } else {
            printDebug("No active cooldowns found.");
        }

        Set<Cooldown> allValidCooldowns = fetchCooldowns(CooldownType.CONSUME_ITEM);
        Set<Cooldown> validCooldowns = filter(allValidCooldowns, material);
        checkValidCooldowns(player, validCooldowns);
    }
}
