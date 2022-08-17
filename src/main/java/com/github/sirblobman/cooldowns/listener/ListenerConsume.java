package com.github.sirblobman.cooldowns.listener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
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
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.scheduler.BukkitScheduler;

import com.github.sirblobman.api.utility.VersionUtility;
import com.github.sirblobman.api.xseries.XMaterial;
import com.github.sirblobman.api.xseries.XPotion;
import com.github.sirblobman.cooldowns.CooldownPlugin;
import com.github.sirblobman.cooldowns.configuration.CooldownSettings;
import com.github.sirblobman.cooldowns.object.CooldownData;
import com.github.sirblobman.cooldowns.object.CooldownType;
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
        ThrownPotion potion = e.getPotion();
        Collection<PotionEffect> effects = potion.getEffects();
        List<XPotion> potions = new ArrayList<>();

        for (PotionEffect effect : effects) {
            PotionEffectType bukkitType = effect.getType();
            potions.add(XPotion.matchXPotion(bukkitType));
        }

        Collection<LivingEntity> entityList = e.getAffectedEntities();
        for (LivingEntity livingEntity : entityList) {
            if(livingEntity instanceof Player) {
                Player player = (Player) livingEntity;
                FakeCancellable fakeCancellable = new FakeCancellable();
                checkConsumePotion(player, potions, fakeCancellable);
                if(fakeCancellable.isCancelled()) {
                    e.setIntensity(player, 0.0D);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onConsume(PlayerItemConsumeEvent e) {
        ItemStack item = e.getItem();
        Player player = e.getPlayer();
        XMaterial material = getXMaterial(item);
        if (material == XMaterial.AIR) {
            return;
        }

        checkConsumeFood(player, material, e);

        if(POTION_MATERIAL_SET.contains(material)) {
            List<XPotion> potions = getPotionEffects(item);
            if(!potions.isEmpty()) {
                checkConsumePotion(player, potions, e);
            }
        }
    }

    private void checkConsumeFood(Player player, XMaterial material, PlayerItemConsumeEvent e) {
        List<CooldownSettings> cooldownSettingsList = fetchCooldowns(CooldownType.CONSUME_ITEM);
        if(cooldownSettingsList.isEmpty()) {
            return;
        }

        CooldownData cooldownData = getCooldownData(player);
        List<CooldownSettings> allActiveCooldowns = cooldownData.getActiveCooldowns(CooldownType.CONSUME_ITEM);
        List<CooldownSettings> activeCooldowns = filter(allActiveCooldowns, material);

        CooldownSettings activeCooldown = checkActiveCooldowns(player, activeCooldowns);
        if(activeCooldown != null) {
            e.setCancelled(true);
            sendCooldownMessage(player, activeCooldown, material);

            CooldownPlugin plugin = getPlugin();
            BukkitScheduler scheduler = Bukkit.getScheduler();
            scheduler.runTask(plugin, player::updateInventory);
            return;
        }

        List<CooldownSettings> allValidCooldowns = fetchCooldowns(CooldownType.CONSUME_ITEM);
        List<CooldownSettings> validCooldowns = filter(allValidCooldowns, material);
        checkValidCooldowns(player, validCooldowns);
    }

    private void checkConsumePotion(Player player, List<XPotion> potions, Cancellable e) {
        List<CooldownSettings> cooldownSettingsList = fetchCooldowns(CooldownType.POTION);
        if(cooldownSettingsList.isEmpty()) {
            return;
        }

        CooldownData cooldownData = getCooldownData(player);
        List<CooldownSettings> allActiveCooldowns = cooldownData.getActiveCooldowns(CooldownType.POTION);
        List<CooldownSettings> activeCooldowns = filter(allActiveCooldowns, potions);

        CooldownSettings activeCooldown = checkActiveCooldowns(player, activeCooldowns);
        if(activeCooldown != null) {
            e.setCancelled(true);
            sendCooldownMessage(player, activeCooldown, potions.get(0));

            CooldownPlugin plugin = getPlugin();
            BukkitScheduler scheduler = Bukkit.getScheduler();
            scheduler.runTask(plugin, player::updateInventory);
            return;
        }

        List<CooldownSettings> allValidCooldowns = fetchCooldowns(CooldownType.POTION);
        List<CooldownSettings> validCooldowns = filter(allValidCooldowns, potions);
        checkValidCooldowns(player, validCooldowns);
    }

    @SuppressWarnings("deprecation")
    private List<XPotion> getPotionEffects(ItemStack item) {
        if(item == null) {
            return Collections.emptyList();
        }

        List<XPotion> potionList = new ArrayList<>();

        int minorVersion = VersionUtility.getMinorVersion();
        if(minorVersion < 9) {
            Potion potion = Potion.fromItemStack(item);
            Collection<PotionEffect> effectList = potion.getEffects();
            for (PotionEffect potionEffect : effectList) {
                PotionEffectType bukkitType = potionEffect.getType();
                potionList.add(XPotion.matchXPotion(bukkitType));
            }
        }

        ItemMeta itemMeta = item.getItemMeta();
        if(itemMeta instanceof PotionMeta) {
            PotionMeta potionMeta = (PotionMeta) itemMeta;
            List<PotionEffect> effectList = potionMeta.getCustomEffects();
            for (PotionEffect potionEffect : effectList) {
                PotionEffectType bukkitType = potionEffect.getType();
                potionList.add(XPotion.matchXPotion(bukkitType));
            }

            if(minorVersion >= 9) {
                PotionData potionData = potionMeta.getBasePotionData();
                PotionType potionType = potionData.getType();
                PotionEffectType effectType = potionType.getEffectType();
                if(effectType != null) {
                    potionList.add(XPotion.matchXPotion(effectType));
                }
            }
        }

        return potionList;
    }
}
