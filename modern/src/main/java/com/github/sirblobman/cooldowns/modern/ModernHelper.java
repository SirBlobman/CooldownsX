package com.github.sirblobman.cooldowns.modern;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CrossbowMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import com.github.sirblobman.api.utility.ItemUtility;
import com.github.sirblobman.api.utility.VersionUtility;
import com.github.sirblobman.api.shaded.xseries.XPotion;

public final class ModernHelper {
    public static boolean isCrossbowReloading(@Nullable ItemStack item) {
        if (ItemUtility.isAir(item)) {
            return false;
        }

        int minorVersion = VersionUtility.getMinorVersion();
        if (minorVersion < 14) {
            return false;
        }

        ItemMeta itemMeta = item.getItemMeta();
        if (!(itemMeta instanceof CrossbowMeta)) {
            return false;
        }

        CrossbowMeta crossbowMeta = (CrossbowMeta) itemMeta;
        return !crossbowMeta.hasChargedProjectiles();
    }

    public static @NotNull List<XPotion> getPotionEffects(@Nullable ItemStack item) {
        if (ItemUtility.isAir(item)) {
            return Collections.emptyList();
        }

        ItemMeta itemMeta = item.getItemMeta();
        if (!(itemMeta instanceof PotionMeta)) {
            return Collections.emptyList();
        }

        List<XPotion> potionList = new ArrayList<>();
        PotionMeta potionMeta = (PotionMeta) itemMeta;

        PotionData basePotionData = potionMeta.getBasePotionData();
        PotionType basePotionType = basePotionData.getType();
        PotionEffectType baseEffectType = basePotionType.getEffectType();
        if (baseEffectType != null) {
            XPotion xpotion = XPotion.matchXPotion(baseEffectType);
            potionList.add(xpotion);
        }

        List<PotionEffect> customEffectList = potionMeta.getCustomEffects();
        for (PotionEffect customEffect : customEffectList) {
            PotionEffectType customEffectType = customEffect.getType();
            XPotion xpotion = XPotion.matchXPotion(customEffectType);
            potionList.add(xpotion);
        }

        return potionList;
    }
}
