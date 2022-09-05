package com.github.sirblobman.cooldowns.listener;

import java.util.Set;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent.Action;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.github.sirblobman.api.xseries.XPotion;
import com.github.sirblobman.cooldowns.api.ICooldownsX;
import com.github.sirblobman.cooldowns.api.configuration.CooldownType;
import com.github.sirblobman.cooldowns.api.configuration.ICooldownSettings;
import com.github.sirblobman.cooldowns.api.data.ICooldownData;
import com.github.sirblobman.cooldowns.api.listener.CooldownListener;

public final class ListenerPotion extends CooldownListener {
    public ListenerPotion(ICooldownsX plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPotion(EntityPotionEffectEvent e) {
        printDebug("Detected EntityPotionEffectEvent...");

        Action action = e.getAction();
        if (action != Action.ADDED) {
            printDebug("Action type is not ADDED, ignoring.");
            return;
        }

        Entity entity = e.getEntity();
        if (!(entity instanceof Player)) {
            printDebug("Entity is not player, ignoring.");
            return;
        }

        Player player = (Player) entity;
        PotionEffect newEffect = e.getNewEffect();
        if (newEffect == null) {
            printDebug("New effect is null, ignoring.");
            return;
        }

        Set<ICooldownSettings> cooldownSettingsList = fetchCooldowns(CooldownType.POTION);
        if (cooldownSettingsList.isEmpty()) {
            printDebug("No POTION cooldowns available, ignoring.");
            return;
        }

        ICooldownData cooldownData = getCooldownData(player);
        PotionEffectType bukkitPotion = newEffect.getType();
        XPotion potion = XPotion.matchXPotion(bukkitPotion);

        Set<ICooldownSettings> allActiveCooldowns = cooldownData.getActiveCooldowns(CooldownType.POTION);
        Set<ICooldownSettings> activeCooldowns = filter(allActiveCooldowns, potion);
        ICooldownSettings activeCooldown = checkActiveCooldowns(player, activeCooldowns);

        if (activeCooldown != null) {
            printDebug("Found active cooldown '" + activeCooldown.getId() + "for potion " + potion + ".");
            e.setCancelled(true);
            sendCooldownMessage(player, activeCooldown, potion);
            printDebug("Cancelled event and sent message to player.");
            updateInventoryLater(player);
            printDebug("Triggered player inventory update for one tick later.");
            return;
        } else {
            printDebug("No active cooldowns for potion " + potion + ".");
        }

        Set<ICooldownSettings> allValidCooldowns = fetchCooldowns(CooldownType.POTION);
        Set<ICooldownSettings> validCooldowns = filter(allValidCooldowns, potion);
        checkValidCooldowns(player, validCooldowns);
    }
}
