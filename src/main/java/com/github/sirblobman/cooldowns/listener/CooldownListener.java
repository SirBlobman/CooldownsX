package com.github.sirblobman.cooldowns.listener;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.github.sirblobman.api.language.LanguageManager;
import com.github.sirblobman.api.language.Replacer;
import com.github.sirblobman.api.plugin.listener.PluginListener;
import com.github.sirblobman.api.xseries.XMaterial;
import com.github.sirblobman.cooldowns.CooldownPlugin;
import com.github.sirblobman.cooldowns.manager.CooldownManager;
import com.github.sirblobman.cooldowns.manager.MaterialDictionary;
import com.github.sirblobman.cooldowns.object.CooldownData;

import org.jetbrains.annotations.NotNull;

public abstract class CooldownListener extends PluginListener<CooldownPlugin> {
    public CooldownListener(CooldownPlugin plugin) {
        super(plugin);
    }

    protected final CooldownManager getCooldownManager() {
        CooldownPlugin plugin = getPlugin();
        return plugin.getCooldownManager();
    }

    protected final MaterialDictionary getMaterialDictionary() {
        CooldownPlugin plugin = getPlugin();
        return plugin.getMaterialDictionary();
    }

    protected final boolean checkCooldown(Player player, XMaterial material) {
        CooldownManager cooldownManager = getCooldownManager();
        CooldownData cooldownData = cooldownManager.getData(player);
        CooldownPlugin plugin = getPlugin();

        if (cooldownData.hasCooldown(plugin, material)) {
            sendCooldownMessage(player, material);
            return true;
        }

        if (cooldownManager.hasCooldown(material)) {
            long systemMillis = System.currentTimeMillis();
            long expireMillis = (systemMillis + cooldownManager.getCooldown(material));
            cooldownData.setCooldown(plugin, material, expireMillis);
        }

        return false;
    }

    @NotNull
    protected final XMaterial getXMaterial(ItemStack stack) {
        if (stack == null) {
            return XMaterial.AIR;
        }

        try {
            return XMaterial.matchXMaterial(stack);
        } catch (IllegalArgumentException ex) {
            try {
                Material bukkitMaterial = stack.getType();
                return XMaterial.matchXMaterial(bukkitMaterial);
            } catch (IllegalArgumentException ex2) {
                return XMaterial.AIR;
            }
        }
    }

    private String getTimeLeft(Player player, XMaterial material) {
        CooldownManager cooldownManager = getCooldownManager();
        CooldownData cooldownData = cooldownManager.getData(player);
        double expireMillis = cooldownData.getCooldownExpireTime(material);
        double systemMillis = System.currentTimeMillis();
        double millisLeft = (expireMillis - systemMillis);

        long timeLeftSeconds = (long) Math.ceil(millisLeft / 1_000.0D);
        return Long.toString(timeLeftSeconds);
    }

    private void sendCooldownMessage(Player player, XMaterial material) {
        CooldownPlugin plugin = getPlugin();
        String timeLeft = getTimeLeft(player, material);

        MaterialDictionary materialDictionary = getMaterialDictionary();
        String materialName = materialDictionary.getMaterialName(material);

        LanguageManager languageManager = plugin.getLanguageManager();
        Replacer replacer = message -> message.replace("{time_left}", timeLeft)
                .replace("{material}", materialName);
        languageManager.sendMessage(player, "cooldown", replacer);
    }
}
