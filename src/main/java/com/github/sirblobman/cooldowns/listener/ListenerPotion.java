package com.github.sirblobman.cooldowns.listener;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent.Action;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitScheduler;

import com.github.sirblobman.api.xseries.XPotion;
import com.github.sirblobman.cooldowns.CooldownPlugin;
import com.github.sirblobman.cooldowns.configuration.CooldownSettings;
import com.github.sirblobman.cooldowns.object.CooldownData;
import com.github.sirblobman.cooldowns.object.CooldownType;

public final class ListenerPotion extends CooldownListener {
    public ListenerPotion(CooldownPlugin plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPotion(EntityPotionEffectEvent e) {
        Action action = e.getAction();
        if(action != Action.ADDED) {
            return;
        }

        Entity entity = e.getEntity();
        if(!(entity instanceof Player)) {
            return;
        }

        Player player = (Player) entity;
        PotionEffect newEffect = e.getNewEffect();
        if(newEffect == null) {
            return;
        }

        PotionEffectType bukkitPotion = newEffect.getType();
        XPotion potion = XPotion.matchXPotion(bukkitPotion);

        List<CooldownSettings> cooldownSettingsList = fetchCooldowns(CooldownType.POTION);
        if(cooldownSettingsList.isEmpty()) {
            return;
        }

        CooldownData cooldownData = getCooldownData(player);
        List<CooldownSettings> allActiveCooldowns = cooldownData.getActiveCooldowns(CooldownType.POTION);
        List<CooldownSettings> activeCooldowns = filter(allActiveCooldowns, potion);

        CooldownSettings activeCooldown = checkActiveCooldowns(player, activeCooldowns);
        if(activeCooldown != null) {
            e.setCancelled(true);
            sendCooldownMessage(player, activeCooldown, potion);

            CooldownPlugin plugin = getPlugin();
            BukkitScheduler scheduler = Bukkit.getScheduler();
            scheduler.runTask(plugin, player::updateInventory);
            return;
        }

        List<CooldownSettings> allValidCooldowns = fetchCooldowns(CooldownType.INTERACT_BLOCK);
        List<CooldownSettings> validCooldowns = filter(allValidCooldowns, potion);
        checkValidCooldowns(player, validCooldowns);
    }
}
