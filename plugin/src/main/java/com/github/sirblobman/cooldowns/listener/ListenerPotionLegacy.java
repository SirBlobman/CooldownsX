package com.github.sirblobman.cooldowns.listener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.github.sirblobman.api.utility.ItemUtility;
import com.github.sirblobman.api.utility.VersionUtility;
import com.github.sirblobman.cooldowns.api.CooldownsX;
import com.github.sirblobman.cooldowns.api.configuration.CooldownType;
import com.github.sirblobman.cooldowns.api.configuration.Cooldown;
import com.github.sirblobman.cooldowns.api.data.PlayerCooldown;
import com.github.sirblobman.cooldowns.api.listener.CooldownListener;
import com.github.sirblobman.cooldowns.modern.ModernHelper;
import com.github.sirblobman.cooldowns.object.FakeCancellable;
import com.github.sirblobman.api.shaded.xseries.XMaterial;
import com.github.sirblobman.api.shaded.xseries.XPotion;

public final class ListenerPotionLegacy extends CooldownListener {
    private static final Set<XMaterial> POTION_MATERIAL_SET;

    static {
        POTION_MATERIAL_SET = EnumSet.of(XMaterial.POTION, XMaterial.SPLASH_POTION, XMaterial.LINGERING_POTION);
    }

    public ListenerPotionLegacy(@NotNull CooldownsX plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPotionSplash(PotionSplashEvent e) {
        printDebug("Detected PotionSplashEvent...");

        ThrownPotion potionEntity = e.getPotion();
        List<XPotion> potionList = getPotionEffects(potionEntity);
        if(potionList.isEmpty()) {
            printDebug("Thrown potion does not have any effects, ignoring event.");
            return;
        }

        printDebug("Thrown Potion Effects: " + potionList);

        Collection<LivingEntity> affectedEntityCollection = e.getAffectedEntities();
        for (LivingEntity affectedEntity : affectedEntityCollection) {
            if (!(affectedEntity instanceof Player)) {
                continue;
            }

            Player affectedPlayer = (Player) affectedEntity;
            printDebug("Checking affected player '" + affectedPlayer.getName() + "'...");

            FakeCancellable fakeCancellable = new FakeCancellable();
            checkPotion(affectedPlayer, potionList, fakeCancellable);

            if (fakeCancellable.isCancelled()) {
                printDebug("Potion effect was cancelled, setting splash intensity to zero.");
                e.setIntensity(affectedEntity, 0.0D);
            }
        }
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
        checkPotion(player, potionList, e);
    }

    private boolean isNotPotion(@NotNull ItemStack item) {
        if (ItemUtility.isAir(item)) {
            return true;
        }

        XMaterial material = XMaterial.matchXMaterial(item);
        return !POTION_MATERIAL_SET.contains(material);
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

    private @NotNull List<XPotion> getCustomEffects(@Nullable ItemStack item) {
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

    private @NotNull List<XPotion> getLegacyPotionEffects(@Nullable ItemStack item) {
        if (ItemUtility.isAir(item)) {
            return Collections.emptyList();
        }

        Potion basePotion = Potion.fromItemStack(item);
        if (basePotion == null) {
            return Collections.emptyList();
        }

        List<XPotion> potionList = new ArrayList<>();
        Collection<PotionEffect> baseEffectList = basePotion.getEffects();
        for (PotionEffect baseEffect : baseEffectList) {
            PotionEffectType baseEffectType = baseEffect.getType();
            XPotion xpotion = XPotion.matchXPotion(baseEffectType);
            potionList.add(xpotion);
        }

        return potionList;
    }

    private @NotNull List<XPotion> getPotionEffects(@Nullable ItemStack item) {
        if (ItemUtility.isAir(item)) {
            return Collections.emptyList();
        }

        int minorVersion = VersionUtility.getMinorVersion();
        List<XPotion> potionList;
        if (minorVersion < 9) {
            List<XPotion> legacyPotionList = getLegacyPotionEffects(item);
            potionList = new ArrayList<>(legacyPotionList);
        } else {
            List<XPotion> modernPotionList = ModernHelper.getPotionEffects(item);
            potionList = new ArrayList<>(modernPotionList);
        }

        List<XPotion> customPotionList = getCustomEffects(item);
        potionList.addAll(customPotionList);
        return potionList;
    }

    private void checkPotion(@NotNull Player player, @NotNull List<XPotion> potionList, @NotNull Cancellable e) {
        printDebug("Checking consume potion for player '" + player.getName() + "'...");
        printDebug("Potions to check: " + potionList);

        Set<Cooldown> allPotionCooldowns = fetchCooldowns(CooldownType.POTION);
        if (allPotionCooldowns.isEmpty()) {
            printDebug("No cooldowns available for type POTION, ignoring event.");
            return;
        }

        PlayerCooldown cooldownData = getCooldownData(player);
        Set<Cooldown> activePotionCooldowns = cooldownData.getActiveCooldowns(CooldownType.POTION);
        Set<Cooldown> filteredActiveCooldowns = filter(activePotionCooldowns, potionList);

        Cooldown activeCooldown = checkActiveCooldowns(player, filteredActiveCooldowns);
        if (activeCooldown != null) {
            String cooldownId = activeCooldown.getId();
            printDebug("Found matching and active potion cooldown: '" + cooldownId + "'.");

            e.setCancelled(true);
            XPotion firstPotion = potionList.get(0);
            sendCooldownMessage(player, activeCooldown, firstPotion);
            printDebug("Cancelled event and triggered message for player.");

            updateInventoryLater(player);
            printDebug("Triggered player inventory update for one tick later.");
            return;
        }

        printDebug("No active potion cooldowns match the current list.");
        printDebug("Checking new cooldowns...");

        Set<Cooldown> validCooldowns = filter(allPotionCooldowns, potionList);
        checkValidCooldowns(player, validCooldowns);
    }
}
