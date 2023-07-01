package com.github.sirblobman.cooldowns.listener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.NotNull;

import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;

import com.github.sirblobman.cooldowns.api.CooldownsX;
import com.github.sirblobman.cooldowns.api.configuration.Cooldown;
import com.github.sirblobman.cooldowns.api.configuration.CooldownType;
import com.github.sirblobman.cooldowns.api.data.PlayerCooldown;
import com.github.sirblobman.cooldowns.api.listener.CooldownListener;
import com.github.sirblobman.api.shaded.xseries.XPotion;

public final class ListenerPotionThrow extends CooldownListener {
    public ListenerPotionThrow(@NotNull CooldownsX plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onProjectileLaunch(ProjectileLaunchEvent e) {
        printDebug("Detected ProjectileLaunchEvent...");

        Projectile projectile = e.getEntity();
        if (!(projectile instanceof ThrownPotion)) {
            printDebug("Projectile was not a thrown potion, ignoring event.");
            return;
        }

        ThrownPotion thrownPotion = (ThrownPotion) projectile;
        List<XPotion> potionList = getPotionEffects(thrownPotion);
        if(potionList.isEmpty()) {
            printDebug("Thrown potion does not have any effects, ignoring event.");
            return;
        }

        ProjectileSource shooter = thrownPotion.getShooter();
        if(!(shooter instanceof Player)) {
            printDebug("Potion thrower is not player, ignoring event.");
            return;
        }

        Player player = (Player) shooter;
        checkPotionThrow(player, potionList, e);
    }

    private List<XPotion> getPotionEffects(@NotNull ThrownPotion thrownPotion) {
        List<XPotion> potionList = new ArrayList<>();

        Collection<PotionEffect> thrownPotionEffects = thrownPotion.getEffects();
        for (PotionEffect thrownPotionEffect : thrownPotionEffects) {
            PotionEffectType thrownPotionEffectType = thrownPotionEffect.getType();
            XPotion xpotion = XPotion.matchXPotion(thrownPotionEffectType);
            potionList.add(xpotion);
        }

        return potionList;
    }

    private void checkPotionThrow(@NotNull Player player, @NotNull List<XPotion> potionList, @NotNull Cancellable e) {
        printDebug("Checking potion throw for player '" + player.getName() + "'...");
        printDebug("Potions to check: " + potionList);

        Set<Cooldown> allPotionCooldowns = fetchCooldowns(CooldownType.POTION_THROW);
        if (allPotionCooldowns.isEmpty()) {
            printDebug("No cooldowns available for type POTION_THROW, ignoring event.");
            return;
        }

        PlayerCooldown cooldownData = getCooldownData(player);
        Set<Cooldown> activePotionCooldowns = cooldownData.getActiveCooldowns(CooldownType.POTION_THROW);
        Set<Cooldown> filteredActiveCooldowns = filter(activePotionCooldowns, potionList);

        Cooldown activeCooldown = checkActiveCooldowns(player, filteredActiveCooldowns);
        if (activeCooldown != null) {
            String cooldownId = activeCooldown.getId();
            printDebug("Found matching and active potion throw cooldown: '" + cooldownId + "'.");

            e.setCancelled(true);
            XPotion firstPotion = potionList.get(0);
            sendCooldownMessage(player, activeCooldown, firstPotion);
            printDebug("Cancelled event and triggered message for player.");

            updateInventoryLater(player);
            printDebug("Triggered player inventory update for one tick later.");
            return;
        }

        printDebug("No active potion throw cooldowns match the current list.");
        printDebug("Checking new cooldowns...");

        Set<Cooldown> validCooldowns = filter(allPotionCooldowns, potionList);
        checkValidCooldowns(player, validCooldowns);
    }
}
