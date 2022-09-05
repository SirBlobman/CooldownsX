package com.github.sirblobman.cooldowns.listener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

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
        ThrownPotion potionEntity = e.getPotion();
        List<XPotion> potionList = new ArrayList<>();

        Collection<PotionEffect> potionEntityEffectCollection = potionEntity.getEffects();
        for (PotionEffect potionEntityEffect : potionEntityEffectCollection) {
            PotionEffectType potionEffectType = potionEntityEffect.getType();
            XPotion xpotion = XPotion.matchXPotion(potionEffectType);
            potionList.add(xpotion);
        }

        Collection<LivingEntity> affectedEntityCollection = e.getAffectedEntities();
        for (LivingEntity affectedEntity : affectedEntityCollection) {
            if (!(affectedEntity instanceof Player)) {
                continue;
            }

            Player affectedPlayer = (Player) affectedEntity;
            FakeCancellable fakeCancellable = new FakeCancellable();
            checkConsumePotion(affectedPlayer, potionList, fakeCancellable);

            if (fakeCancellable.isCancelled()) {
                e.setIntensity(affectedEntity, 0.0D);
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onConsume(PlayerItemConsumeEvent e) {
        ItemStack item = e.getItem();
        if (ItemUtility.isAir(item)) {
            return;
        }

        Player player = e.getPlayer();
        XMaterial material = XMaterial.matchXMaterial(item);
        if (POTION_MATERIAL_SET.contains(material)) {
            List<XPotion> potions = getPotionEffects(item);
            if (!potions.isEmpty()) {
                checkConsumePotion(player, potions, e);
            }
        } else {
            checkConsumeFood(player, material, e);
        }
    }

    private void checkConsumeFood(Player player, XMaterial material, PlayerItemConsumeEvent e) {
        Set<ICooldownSettings> cooldownSettingsList = fetchCooldowns(CooldownType.CONSUME_ITEM);
        if (cooldownSettingsList.isEmpty()) {
            return;
        }

        ICooldownData cooldownData = getCooldownData(player);
        Set<ICooldownSettings> allActiveCooldowns = cooldownData.getActiveCooldowns(CooldownType.CONSUME_ITEM);
        Set<ICooldownSettings> activeCooldowns = filter(allActiveCooldowns, material);

        ICooldownSettings activeCooldown = checkActiveCooldowns(player, activeCooldowns);
        if (activeCooldown != null) {
            e.setCancelled(true);
            sendCooldownMessage(player, activeCooldown, material);
            updateInventoryLater(player);
            return;
        }

        Set<ICooldownSettings> allValidCooldowns = fetchCooldowns(CooldownType.CONSUME_ITEM);
        Set<ICooldownSettings> validCooldowns = filter(allValidCooldowns, material);
        checkValidCooldowns(player, validCooldowns);
    }

    private void checkConsumePotion(Player player, List<XPotion> potions, Cancellable e) {
        Set<ICooldownSettings> cooldownSettingsList = fetchCooldowns(CooldownType.POTION);
        if (cooldownSettingsList.isEmpty()) {
            return;
        }

        ICooldownData cooldownData = getCooldownData(player);
        Set<ICooldownSettings> allActiveCooldowns = cooldownData.getActiveCooldowns(CooldownType.POTION);
        Set<ICooldownSettings> activeCooldowns = filter(allActiveCooldowns, potions);

        ICooldownSettings activeCooldown = checkActiveCooldowns(player, activeCooldowns);
        if (activeCooldown != null) {
            e.setCancelled(true);
            sendCooldownMessage(player, activeCooldown, potions.get(0));
            updateInventoryLater(player);
            return;
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
