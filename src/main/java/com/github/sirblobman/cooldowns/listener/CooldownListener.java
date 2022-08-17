package com.github.sirblobman.cooldowns.listener;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitScheduler;

import com.github.sirblobman.api.adventure.adventure.text.Component;
import com.github.sirblobman.api.adventure.adventure.text.minimessage.MiniMessage;
import com.github.sirblobman.api.language.LanguageManager;
import com.github.sirblobman.api.language.Replacer;
import com.github.sirblobman.api.language.SimpleReplacer;
import com.github.sirblobman.api.nms.MultiVersionHandler;
import com.github.sirblobman.api.nms.PlayerHandler;
import com.github.sirblobman.api.plugin.listener.PluginListener;
import com.github.sirblobman.api.utility.Validate;
import com.github.sirblobman.api.utility.VersionUtility;
import com.github.sirblobman.api.xseries.XMaterial;
import com.github.sirblobman.api.xseries.XPotion;
import com.github.sirblobman.cooldowns.CooldownPlugin;
import com.github.sirblobman.cooldowns.configuration.CooldownSettings;
import com.github.sirblobman.cooldowns.dictionary.MaterialDictionary;
import com.github.sirblobman.cooldowns.dictionary.PotionDictionary;
import com.github.sirblobman.cooldowns.manager.CooldownManager;
import com.github.sirblobman.cooldowns.object.CooldownData;
import com.github.sirblobman.cooldowns.object.CooldownType;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class CooldownListener extends PluginListener<CooldownPlugin> {
    public CooldownListener(CooldownPlugin plugin) {
        super(plugin);
    }

    protected final LanguageManager getLanguageManager() {
        CooldownPlugin plugin = getPlugin();
        return plugin.getLanguageManager();
    }

    protected final CooldownManager getCooldownManager() {
        CooldownPlugin plugin = getPlugin();
        return plugin.getCooldownManager();
    }

    protected final MaterialDictionary getMaterialDictionary() {
        CooldownPlugin plugin = getPlugin();
        return plugin.getMaterialDictionary();
    }

    protected final CooldownData getCooldownData(Player player) {
        CooldownManager cooldownManager = getCooldownManager();
        return cooldownManager.getData(player);
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

    @NotNull
    @SuppressWarnings("deprecation")
    protected final XMaterial getXMaterial(Block block) {
        if(block == null) {
            return XMaterial.AIR;
        }

        Material bukkitMaterial = block.getType();
        int minorVersion = VersionUtility.getMinorVersion();
        if(minorVersion < 13) {
            byte data = block.getData();
            int materialId = bukkitMaterial.getId();
            return XMaterial.matchXMaterial(materialId, data).orElse(XMaterial.AIR);
        } else {
            return XMaterial.matchXMaterial(bukkitMaterial);
        }
    }

    protected final void sendPacket(Player player, XMaterial material, int ticksLeft) {
        Validate.notNull(player, "player must not be null!");
        Validate.notNull(material, "material must not be null!");

        Material bukkitMaterial = material.parseMaterial();
        if (bukkitMaterial == null) {
            return;
        }

        CooldownPlugin plugin = getPlugin();
        Runnable task = () -> sendPacket0(player, bukkitMaterial, ticksLeft);
        BukkitScheduler scheduler = Bukkit.getScheduler();
        scheduler.runTask(plugin, task);
    }

    private void sendPacket0(Player player, Material material, int ticks) {
        CooldownPlugin plugin = getPlugin();
        MultiVersionHandler multiVersionHandler = plugin.getMultiVersionHandler();
        PlayerHandler playerHandler = multiVersionHandler.getPlayerHandler();
        playerHandler.sendCooldownPacket(player, material, ticks);
    }

    @NotNull
    protected final List<CooldownSettings> fetchCooldowns(CooldownType cooldownType) {
        CooldownManager cooldownManager = getCooldownManager();
        List<CooldownSettings> cooldownSettingsList = cooldownManager.getAllCooldownSettings();
        if(cooldownSettingsList.isEmpty()) {
            return Collections.emptyList();
        }

        return cooldownSettingsList.parallelStream()
                .filter(settings -> settings.getCooldownType() == cooldownType)
                .collect(Collectors.toList());
    }

    protected final List<CooldownSettings> filter(List<CooldownSettings> original, XMaterial material) {
        if(original.isEmpty()) {
            return Collections.emptyList();
        }

        return original.parallelStream()
                .filter(settings -> settings.hasMaterial(material))
                .collect(Collectors.toList());
    }

    protected final List<CooldownSettings> filter(List<CooldownSettings> original, XPotion potion) {
        if(original.isEmpty()) {
            return Collections.emptyList();
        }

        List<XPotion> potions = Collections.singletonList(potion);
        return filter(original, potions);
    }

    protected final List<CooldownSettings> filter(List<CooldownSettings> original, List<XPotion> potions) {
        if(original.isEmpty()) {
            return Collections.emptyList();
        }

        return original.parallelStream().filter(settings -> {
            for (XPotion potion : potions) {
                if(settings.hasPotion(potion)) {
                    return true;
                }
            }

            return false;
        }).collect(Collectors.toList());
    }


    // Return a non-null value to cancel the event.
    @Nullable
    protected final CooldownSettings checkActiveCooldowns(Player player, List<CooldownSettings> cooldowns) {
        World world = player.getWorld();
        CooldownData cooldownData = getCooldownData(player);

        for (CooldownSettings cooldown : cooldowns) {
            if (cooldown.isDisabled(world)) {
                continue;
            }

            if (cooldown.canBypass(player)) {
                continue;
            }

            long systemMillis = System.currentTimeMillis();
            long expireMillis = cooldownData.getCooldownExpireTime(cooldown);
            if (systemMillis >= expireMillis) {
                continue;
            }

            return cooldown;
        }

        return null;
    }

    protected final void checkValidCooldowns(Player player, List<CooldownSettings> cooldowns) {
        World world = player.getWorld();
        CooldownData cooldownData = getCooldownData(player);

        for (CooldownSettings cooldown : cooldowns) {
            if (cooldown.isDisabled(world)) {
                continue;
            }

            if (cooldown.canBypass(player)) {
                continue;
            }
            
            int amount = cooldown.getAmount();
            if(amount > 1) {
                int used = cooldownData.getActionCount(cooldown);
                if(used < amount) {
                    cooldownData.setActionCount(cooldown, used + 1);
                    continue;
                }
                
                if(cooldown.isResetAmount()) {
                    cooldownData.setActionCount(cooldown, 0);
                }
            }

            long cooldownSeconds = cooldown.getCooldownSeconds(player);
            long cooldownMillis = TimeUnit.SECONDS.toMillis(cooldownSeconds);
            long systemMillis = System.currentTimeMillis();
            long expireMillis = (systemMillis + cooldownMillis);
            cooldownData.setCooldown(cooldown, expireMillis);

            Optional<List<XMaterial>> optionalMaterialList = cooldown.getMaterialList();
            if (optionalMaterialList.isPresent() && cooldown.isUsePacketCooldown()) {
                List<XMaterial> materialList = optionalMaterialList.get();
                for (XMaterial material : materialList) {
                    sendPacket(player, material, (int) (cooldownSeconds * 20L));
                }
            }
        }
    }

    protected final void sendCooldownMessage(Player player, CooldownSettings settings, XMaterial material) {
        CooldownPlugin plugin = getPlugin();
        MaterialDictionary materialDictionary = plugin.getMaterialDictionary();
        String materialName = materialDictionary.get(material);

        Replacer replacer = new SimpleReplacer("{material}", materialName);
        sendCooldownMessage(player, settings, replacer);
    }

    protected final void sendCooldownMessage(Player player, CooldownSettings settings, XPotion potion) {
        CooldownPlugin plugin = getPlugin();
        PotionDictionary potionDictionary = plugin.getPotionDictionary();
        String potionName = potionDictionary.get(potion);

        Replacer replacer = new SimpleReplacer("{potion}", potionName);
        sendCooldownMessage(player, settings, replacer);
    }

    private void sendCooldownMessage(Player player, CooldownSettings settings, Replacer replacer) {
        String messageFormat = settings.getMessageFormat();
        if(messageFormat == null || messageFormat.isEmpty()) {
            return;
        }

        CooldownData cooldownData = getCooldownData(player);
        double expireTimeMillis = cooldownData.getCooldownExpireTime(settings);
        double systemTimeMillis = System.currentTimeMillis();
        double timeLeftMillis = Math.max(0.0D, expireTimeMillis - systemTimeMillis);
        double timeLeftSeconds = (timeLeftMillis / 1_000.0D);
        long timeLeftSecondsInteger = Math.round(timeLeftSeconds);


        LanguageManager languageManager = getLanguageManager();
        MiniMessage miniMessage = languageManager.getMiniMessage();
        String timeLeftInteger = Long.toString(timeLeftSecondsInteger);
        String timeLeftDecimal = languageManager.formatDecimal(player, timeLeftSeconds);
        String messageFormatReplaced = replacer.replace(messageFormat);

        String messageString = messageFormatReplaced.replace("{time_left}", timeLeftInteger)
                .replace("{time_left_decimal}", timeLeftDecimal);
        Component message = miniMessage.deserialize(messageString);
        languageManager.sendMessage(player, message);
    }
}
