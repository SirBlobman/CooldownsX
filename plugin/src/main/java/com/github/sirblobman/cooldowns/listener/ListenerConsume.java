package com.github.sirblobman.cooldowns.listener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;

import com.github.sirblobman.api.utility.ItemUtility;
import com.github.sirblobman.api.utility.VersionUtility;
import com.github.sirblobman.api.xseries.XMaterial;
import com.github.sirblobman.api.xseries.XPotion;
import com.github.sirblobman.cooldowns.CooldownPlugin;
import com.github.sirblobman.cooldowns.api.configuration.CooldownType;
import com.github.sirblobman.cooldowns.api.configuration.ICooldownSettings;
import com.github.sirblobman.cooldowns.api.data.ICooldownData;
import com.github.sirblobman.cooldowns.api.listener.CooldownListener;
import com.github.sirblobman.cooldowns.modern.ModernHelper;
import com.github.sirblobman.cooldowns.object.FakeCancellable;

public final class ListenerConsume extends CooldownListener {
    private static final Set<XMaterial> POTION_MATERIAL_SET;

    static {
        POTION_MATERIAL_SET = EnumSet.of(XMaterial.POTION, XMaterial.SPLASH_POTION, XMaterial.LINGERING_POTION);
    }

    public ListenerConsume(CooldownPlugin plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPotionSplash(PotionSplashEvent e) {
        printDebug("Detected PotionSplashEvent...");

        ThrownPotion potionEntity = e.getPotion();
        List<XPotion> potionList = new ArrayList<>();

        Collection<PotionEffect> potionEntityEffectCollection = potionEntity.getEffects();
        for (PotionEffect potionEntityEffect : potionEntityEffectCollection) {
            PotionEffectType potionEffectType = potionEntityEffect.getType();
            XPotion xpotion = XPotion.matchXPotion(potionEffectType);
            potionList.add(xpotion);
        }

        printDebug("Potion List: " + potionList);
        Collection<LivingEntity> affectedEntityCollection = e.getAffectedEntities();
        for (LivingEntity affectedEntity : affectedEntityCollection) {
            if (!(affectedEntity instanceof Player)) {
                continue;
            }

            Player affectedPlayer = (Player) affectedEntity;
            printDebug("Checking affected player " + affectedPlayer.getName() + "...");

            FakeCancellable fakeCancellable = new FakeCancellable();
            checkConsumePotion(affectedPlayer, potionList, fakeCancellable);

            if (fakeCancellable.isCancelled()) {
                printDebug("Potion effect was cancelled, setting splash intensity to zero.");
                e.setIntensity(affectedEntity, 0.0D);
            }
        }
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
        if (POTION_MATERIAL_SET.contains(material)) {
            List<XPotion> potions = getPotionEffects(item);
            if (!potions.isEmpty()) {
                printDebug("Detected potions in consumed item: " + potions);
                checkConsumePotion(player, potions, e);
            }
        } else {
            printDebug("Detected item as non-potion (food/drink).");
            checkConsumeFood(player, material, e);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onInteract(ProjectileLaunchEvent e) {
        printDebug("Detected ProjectileLaunch...");

        Projectile projectile = e.getEntity();
        if(!(projectile instanceof ThrownPotion)) {
            printDebug("Projectile was not a potion, ignoring.");
            return;
        }

        ProjectileSource shooter = projectile.getShooter();
        if(!(shooter instanceof Player)) {
            printDebug("shooter was not a player, ignoring.");
            return;
        }

        ThrownPotion thrownPotion = (ThrownPotion) projectile;
        List<XPotion> potionList = new ArrayList<>();
        Collection<PotionEffect> potionEffectCollection = thrownPotion.getEffects();

        for (PotionEffect potionEffect : potionEffectCollection) {
            PotionEffectType potionEffectType = potionEffect.getType();
            XPotion xpotion = XPotion.matchXPotion(potionEffectType);
            potionList.add(xpotion);
        }

        Player player = (Player) shooter;
        printDebug("Player: " + player.getName());

        printDebug("Detected potions in projectile: " + potionList);
        checkPotion(player, potionList, e);
    }

    private void checkConsumeFood(Player player, XMaterial material, PlayerItemConsumeEvent e) {
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
            updateInventoryLater(player);
            printDebug("Triggered player inventory update for one tick later.");
            return;
        } else {
            printDebug("No active cooldowns found.");
        }

        Set<ICooldownSettings> allValidCooldowns = fetchCooldowns(CooldownType.CONSUME_ITEM);
        Set<ICooldownSettings> validCooldowns = filter(allValidCooldowns, material);
        checkValidCooldowns(player, validCooldowns);
    }

    private void checkConsumePotion(Player player, List<XPotion> potions, Cancellable e) {
        printDebug("Checking consume potion for player " + player.getName() + "...");
        printDebug("Potions to check: " + potions);

        Set<ICooldownSettings> cooldownSettingsList = fetchCooldowns(CooldownType.POTION);
        if (cooldownSettingsList.isEmpty()) {
            printDebug("No cooldowns available with type POTION, ignoring.");
            return;
        }

        ICooldownData cooldownData = getCooldownData(player);
        Set<ICooldownSettings> allActiveCooldowns = cooldownData.getActiveCooldowns(CooldownType.POTION);
        Set<ICooldownSettings> activeCooldowns = filter(allActiveCooldowns, potions);

        ICooldownSettings activeCooldown = checkActiveCooldowns(player, activeCooldowns);
        if (activeCooldown != null) {
            printDebug("Found active cooldown '" + activeCooldown.getId() + "for potions.");
            e.setCancelled(true);
            sendCooldownMessage(player, activeCooldown, potions.get(0));
            printDebug("Cancelled event and sent message to player.");
            updateInventoryLater(player);
            printDebug("Triggered player inventory update for one tick later.");
            return;
        } else {
            printDebug("No active cooldowns found.");
        }

        Set<ICooldownSettings> allValidCooldowns = fetchCooldowns(CooldownType.POTION);
        Set<ICooldownSettings> validCooldowns = filter(allValidCooldowns, potions);
        checkValidCooldowns(player, validCooldowns);
    }

    private void checkPotion(Player player, List<XPotion> potions, Cancellable e) {
        printDebug("Checking interact potion for player " + player.getName() + "...");
        printDebug("Potions to check: " + potions);

        Set<ICooldownSettings> cooldownSettingsList = fetchCooldowns(CooldownType.POTION);
        if (cooldownSettingsList.isEmpty()) {
            printDebug("No cooldowns available with type POTION, ignoring.");
            return;
        }

        ICooldownData cooldownData = getCooldownData(player);
        Set<ICooldownSettings> allActiveCooldowns = cooldownData.getActiveCooldowns(CooldownType.POTION);
        Set<ICooldownSettings> activeCooldowns = filter(allActiveCooldowns, potions);

        ICooldownSettings activeCooldown = checkActiveCooldowns(player, activeCooldowns);
        if (activeCooldown != null) {
            printDebug("Found active cooldown '" + activeCooldown.getId() + "for potions.");
            e.setCancelled(true);
            sendCooldownMessage(player, activeCooldown, potions.get(0));
            printDebug("Cancelled event and sent message to player.");
            updateInventoryLater(player);
            printDebug("Triggered player inventory update for one tick later.");
            return;
        } else {
            printDebug("No active cooldowns found.");
        }

        Set<ICooldownSettings> allValidCooldowns = fetchCooldowns(CooldownType.POTION);
        Set<ICooldownSettings> validCooldowns = filter(allValidCooldowns, potions);
        checkValidCooldowns(player, validCooldowns);
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

    private List<XPotion> getLegacyPotionEffects(ItemStack item) {
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

    private List<XPotion> getPotionEffects(ItemStack item) {
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
}
