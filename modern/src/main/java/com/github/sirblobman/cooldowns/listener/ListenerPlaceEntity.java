package com.github.sirblobman.cooldowns.listener;

import java.util.Set;

import org.jetbrains.annotations.NotNull;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import com.github.sirblobman.cooldowns.api.CooldownsX;
import com.github.sirblobman.cooldowns.api.configuration.Cooldown;
import com.github.sirblobman.cooldowns.api.configuration.CooldownType;
import com.github.sirblobman.cooldowns.api.data.PlayerCooldown;
import com.github.sirblobman.cooldowns.api.listener.CooldownListener;

public final class ListenerPlaceEntity extends CooldownListener {
    public ListenerPlaceEntity(@NotNull CooldownsX plugin) {
        super(plugin);
    }

    @SuppressWarnings("deprecation") // Draft API
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityPlace(org.bukkit.event.entity.EntityPlaceEvent e) {
        Player player = e.getPlayer();
        if (player == null) {
            return;
        }

        EntityType entityType = e.getEntityType();
        checkPlaceEntity(player, entityType, e);
    }

    private void checkPlaceEntity(@NotNull Player player, @NotNull EntityType entityType, @NotNull Cancellable e) {
        Set<Cooldown> cooldownSettingsList = fetchCooldowns(CooldownType.PLACE_ENTITY);
        if (cooldownSettingsList.isEmpty()) {
            return;
        }

        PlayerCooldown cooldownData = getCooldownData(player);
        Set<Cooldown> allActiveCooldowns = cooldownData.getActiveCooldowns(CooldownType.PLACE_ENTITY);
        Set<Cooldown> activeCooldownSet = filter(allActiveCooldowns, entityType);

        Cooldown activeCooldown = checkActiveCooldowns(player, activeCooldownSet);
        if (activeCooldown != null) {
            e.setCancelled(true);
            sendCooldownMessage(player, activeCooldown, entityType);
            updateInventoryLater(player);
            return;
        }

        Set<Cooldown> allValidCooldowns = fetchCooldowns(CooldownType.CONSUME_ITEM);
        Set<Cooldown> validCooldownSet = filter(allValidCooldowns, entityType);
        checkValidCooldowns(player, validCooldownSet);
    }
}
