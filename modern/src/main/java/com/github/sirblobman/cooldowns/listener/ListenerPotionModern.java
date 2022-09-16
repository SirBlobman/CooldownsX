package com.github.sirblobman.cooldowns.listener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent.Action;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.github.sirblobman.api.utility.ItemUtility;
import com.github.sirblobman.api.xseries.XMaterial;
import com.github.sirblobman.api.xseries.XPotion;
import com.github.sirblobman.cooldowns.api.ICooldownsX;
import com.github.sirblobman.cooldowns.api.configuration.CooldownType;
import com.github.sirblobman.cooldowns.api.configuration.ICooldownSettings;
import com.github.sirblobman.cooldowns.api.data.ICooldownData;
import com.github.sirblobman.cooldowns.api.listener.CooldownListener;
import com.github.sirblobman.cooldowns.modern.ModernHelper;

public final class ListenerPotionModern extends CooldownListener {
    private static final Set<XMaterial> POTION_MATERIAL_SET;

    static {
        POTION_MATERIAL_SET = EnumSet.of(XMaterial.POTION, XMaterial.SPLASH_POTION, XMaterial.LINGERING_POTION);
    }

    public ListenerPotionModern(ICooldownsX plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPotionConsume(PlayerItemConsumeEvent e) {
        printDebug("Detected PlayerItemConsumeEvent...");

        ItemStack item = e.getItem();
        if (isNotPotion(item)) {
            printDebug("Consumed item was not a potion, ignoring event.");
            return;
        }

        List<XPotion> potionList = getPotionEffects(item);
        if(potionList.isEmpty()) {
            printDebug("Consumed potion does not have any effects, ignoring event.");
            return;
        }

        Player player = e.getPlayer();
        printDebug("Player: " + player.getName());

        ICooldownData cooldownData = getCooldownData(player);
        Set<ICooldownSettings> allActiveCooldowns = cooldownData.getActiveCooldowns(CooldownType.POTION);
        Set<ICooldownSettings> activeCooldowns = filter(allActiveCooldowns, potionList);
        ICooldownSettings activeCooldown = checkActiveCooldowns(player, activeCooldowns);

        if (activeCooldown != null) {
            printDebug("Found active cooldown '" + activeCooldown.getId() + "for potion s" + potionList + ".");
            e.setCancelled(true);
            sendCooldownMessage(player, activeCooldown, potionList.get(0));
            printDebug("Cancelled event and sent message to player.");
            updateInventoryLater(player);
            printDebug("Triggered player inventory update for one tick later.");
        } else {
            printDebug("No active cooldowns for potion " + potionList + ".");
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityPotionEffect(EntityPotionEffectEvent e) {
        printDebug("Detected EntityPotionEffectEvent...");

        Action action = e.getAction();
        if (action != Action.ADDED && action != Action.CHANGED) {
            printDebug("Action type is not ADDED or CHANGED, ignoring.");
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

    private boolean isNotPotion(ItemStack item) {
        if (ItemUtility.isAir(item)) {
            return true;
        }

        XMaterial material = XMaterial.matchXMaterial(item);
        return !POTION_MATERIAL_SET.contains(material);
    }

    private List<XPotion> getCustomEffects(ItemStack item) {
        if (ItemUtility.isAir(item)) {
            return Collections.emptyList();
        }

        ItemMeta itemMeta = item.getItemMeta();
        if (!(itemMeta instanceof PotionMeta)) {
            return Collections.emptyList();
        }

        PotionMeta potionMeta = (PotionMeta) itemMeta;
        if (!potionMeta.hasCustomEffects()) {
            return Collections.emptyList();
        }

        List<XPotion> potionList = new ArrayList<>();
        List<PotionEffect> customEffectList = potionMeta.getCustomEffects();
        for (PotionEffect customEffect : customEffectList) {
            PotionEffectType customEffectType = customEffect.getType();
            XPotion xpotion = XPotion.matchXPotion(customEffectType);
            potionList.add(xpotion);
        }

        return potionList;
    }

    private List<XPotion> getPotionEffects(ItemStack item) {
        if (ItemUtility.isAir(item)) {
            return Collections.emptyList();
        }

        List<XPotion> potionList = ModernHelper.getPotionEffects(item);
        List<XPotion> customPotionList = getCustomEffects(item);
        potionList.addAll(customPotionList);
        return potionList;
    }
}
