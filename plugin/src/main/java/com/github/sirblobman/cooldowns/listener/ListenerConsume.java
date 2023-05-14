package com.github.sirblobman.cooldowns.listener;

import java.util.Set;

import org.jetbrains.annotations.NotNull;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

import com.github.sirblobman.api.utility.ItemUtility;
import com.github.sirblobman.cooldowns.api.ICooldownsX;
import com.github.sirblobman.cooldowns.api.configuration.CooldownType;
import com.github.sirblobman.cooldowns.api.configuration.ICooldownSettings;
import com.github.sirblobman.cooldowns.api.data.ICooldownData;
import com.github.sirblobman.cooldowns.api.listener.CooldownListener;
import com.github.sirblobman.api.shaded.xseries.XMaterial;

public final class ListenerConsume extends CooldownListener {
    public ListenerConsume(@NotNull ICooldownsX plugin) {
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
        Set<ICooldownSettings> cooldownSettingsList = fetchCooldowns(CooldownType.CONSUME_ITEM);
        if (cooldownSettingsList.isEmpty()) {
            return;
        }

        ICooldownData cooldownData = getCooldownData(player);
        Set<ICooldownSettings> allActiveCooldowns = cooldownData.getActiveCooldowns(CooldownType.CONSUME_ITEM);
        Set<ICooldownSettings> activeCooldowns = filter(allActiveCooldowns, material);

        ICooldownSettings activeCooldown = checkActiveCooldowns(player, activeCooldowns);
        if (activeCooldown != null) {
            printDebug("Found active cooldown '" + activeCooldown.getId() + "for consume.");
            e.setCancelled(true);
            sendCooldownMessage(player, activeCooldown, material);
            printDebug("Cancelled event and sent message to player.");
            closeInventoryLater(player);
            printDebug("Triggered player inventory update for one tick later.");
            return;
        } else {
            printDebug("No active cooldowns found.");
        }

        Set<ICooldownSettings> allValidCooldowns = fetchCooldowns(CooldownType.CONSUME_ITEM);
        Set<ICooldownSettings> validCooldowns = filter(allValidCooldowns, material);
        checkValidCooldowns(player, validCooldowns);
    }
}
