package com.github.sirblobman.cooldowns.api.listener;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import com.github.sirblobman.api.adventure.adventure.text.Component;
import com.github.sirblobman.api.adventure.adventure.text.minimessage.MiniMessage;
import com.github.sirblobman.api.configuration.ConfigurationManager;
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
import com.github.sirblobman.cooldowns.api.ICooldownsX;
import com.github.sirblobman.cooldowns.api.configuration.CooldownType;
import com.github.sirblobman.cooldowns.api.configuration.ICooldownSettings;
import com.github.sirblobman.cooldowns.api.data.ICooldownData;
import com.github.sirblobman.cooldowns.api.dictionary.IDictionary;
import com.github.sirblobman.cooldowns.api.manager.ICooldownManager;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class CooldownListener extends PluginListener<JavaPlugin> {
    private final ICooldownsX plugin;

    public CooldownListener(ICooldownsX plugin) {
        super(plugin.getPlugin());
        this.plugin = plugin;
    }

    protected final ICooldownsX getCooldownsX() {
        return this.plugin;
    }

    protected final ConfigurationManager getConfigurationManager() {
        ICooldownsX plugin = getCooldownsX();
        return plugin.getConfigurationManager();
    }

    protected final LanguageManager getLanguageManager() {
        ICooldownsX plugin = getCooldownsX();
        return plugin.getLanguageManager();
    }

    protected final ICooldownManager getCooldownManager() {
        ICooldownsX plugin = getCooldownsX();
        return plugin.getCooldownManager();
    }

    protected final IDictionary<XMaterial> getMaterialDictionary() {
        ICooldownsX plugin = getCooldownsX();
        return plugin.getMaterialDictionary();
    }

    protected final IDictionary<XPotion> getPotionDictionary() {
        ICooldownsX plugin = getCooldownsX();
        return plugin.getPotionDictionary();
    }

    protected final ICooldownData getCooldownData(Player player) {
        ICooldownManager cooldownManager = getCooldownManager();
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
        if (block == null) {
            return XMaterial.AIR;
        }

        Material bukkitMaterial = block.getType();
        int minorVersion = VersionUtility.getMinorVersion();
        if (minorVersion < 13) {
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

        JavaPlugin plugin = getPlugin();
        Runnable task = () -> sendPacket0(player, bukkitMaterial, ticksLeft);
        BukkitScheduler scheduler = Bukkit.getScheduler();
        scheduler.runTask(plugin, task);
    }

    private void sendPacket0(Player player, Material material, int ticks) {
        ICooldownsX plugin = getCooldownsX();
        MultiVersionHandler multiVersionHandler = plugin.getMultiVersionHandler();
        PlayerHandler playerHandler = multiVersionHandler.getPlayerHandler();
        playerHandler.sendCooldownPacket(player, material, ticks);
    }

    @NotNull
    protected final Set<ICooldownSettings> fetchCooldowns(CooldownType cooldownType) {
        ICooldownManager cooldownManager = getCooldownManager();
        List<ICooldownSettings> cooldownSettingsList = cooldownManager.getAllCooldownSettings();
        if (cooldownSettingsList.isEmpty()) {
            return Collections.emptySet();
        }

        return cooldownSettingsList.parallelStream()
                .filter(settings -> settings.getCooldownType() == cooldownType)
                .collect(Collectors.toSet());
    }

    protected final Set<ICooldownSettings> filter(Set<ICooldownSettings> original, XMaterial material) {
        if (original.isEmpty()) {
            return Collections.emptySet();
        }

        return original.parallelStream()
                .filter(settings -> settings.hasMaterial(material))
                .collect(Collectors.toSet());
    }

    protected final Set<ICooldownSettings> filter(Set<ICooldownSettings> original, XPotion potion) {
        if (original.isEmpty()) {
            return Collections.emptySet();
        }

        Set<XPotion> potions = Collections.singleton(potion);
        return filter(original, potions);
    }

    protected final Set<ICooldownSettings> filter(Set<ICooldownSettings> original, Iterable<XPotion> potions) {
        if (original.isEmpty()) {
            return Collections.emptySet();
        }

        return original.parallelStream().filter(settings -> {
            for (XPotion potion : potions) {
                if (settings.hasPotion(potion)) {
                    return true;
                }
            }

            return false;
        }).collect(Collectors.toSet());
    }


    // Return a non-null value to cancel the event.
    @Nullable
    protected final ICooldownSettings checkActiveCooldowns(Player player, Set<ICooldownSettings> cooldowns) {
        World world = player.getWorld();
        ICooldownData cooldownData = getCooldownData(player);

        for (ICooldownSettings cooldown : cooldowns) {
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

    protected final void checkValidCooldowns(Player player, Set<ICooldownSettings> cooldowns) {
        World world = player.getWorld();
        ICooldownData cooldownData = getCooldownData(player);
        String playerName = player.getName();
        printDebug("Checking if any cooldowns can be activated for player '" + playerName + "'.");

        for (ICooldownSettings cooldown : cooldowns) {
            String cooldownId = cooldown.getId();
            printDebug("Checking cooldown '" + cooldownId + "'.");

            if (cooldown.isDisabled(world)) {
                printDebug("World is disabled, skipping.");
                continue;
            }

            if (cooldown.canBypass(player)) {
                printDebug("Player has bypass, skipping.");
                continue;
            }

            int amount = cooldown.getAmount();
            if (amount > 1) {
                int used = cooldownData.getActionCount(cooldown);
                if (used < amount) {
                    printDebug("Player has not triggered the action enough times, skipping.");
                    cooldownData.setActionCount(cooldown, used + 1);
                    continue;
                }

                if (cooldown.isResetAmount()) {
                    cooldownData.setActionCount(cooldown, 0);
                }
            }

            long cooldownSeconds = cooldown.getCooldownSeconds(player);
            long cooldownMillis = TimeUnit.SECONDS.toMillis(cooldownSeconds);
            long systemMillis = System.currentTimeMillis();
            long expireMillis = (systemMillis + cooldownMillis);
            cooldownData.setCooldown(cooldown, expireMillis);
            printDebug("Added cooldown '" + cooldownId + "' to player '" + playerName
                    + "' until '" + expireMillis + "'.");

            Optional<List<XMaterial>> optionalMaterialList = cooldown.getMaterialList();
            if (optionalMaterialList.isPresent() && cooldown.isUsePacketCooldown()) {
                List<XMaterial> materialList = optionalMaterialList.get();
                for (XMaterial material : materialList) {
                    sendPacket(player, material, (int) (cooldownSeconds * 20L));
                }
            }
        }
    }

    protected final void sendCooldownMessage(Player player, ICooldownSettings settings, XMaterial material) {
        IDictionary<XMaterial> materialDictionary = getMaterialDictionary();
        String materialName = materialDictionary.get(material);

        Replacer replacer = new SimpleReplacer("{material}", materialName);
        sendCooldownMessage(player, settings, replacer);
    }

    protected final void sendCooldownMessage(Player player, ICooldownSettings settings, XPotion potion) {
        IDictionary<XPotion> potionDictionary = getPotionDictionary();
        String potionName = potionDictionary.get(potion);

        Replacer replacer = new SimpleReplacer("{potion}", potionName);
        sendCooldownMessage(player, settings, replacer);
    }

    private void sendCooldownMessage(Player player, ICooldownSettings settings, Replacer replacer) {
        String messageFormat = settings.getMessageFormat();
        if (messageFormat == null || messageFormat.isEmpty()) {
            return;
        }

        ICooldownData cooldownData = getCooldownData(player);
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

    protected final void printDebug(String message) {
        ICooldownsX plugin = getCooldownsX();
        if (!plugin.isDebugMode()) {
            return;
        }

        Class<?> thisClass = getClass();
        String className = thisClass.getSimpleName();

        String fullMessage = String.format(Locale.US, "[Debug] [%s] %s", className, message);
        Logger logger = getLogger();
        logger.info(fullMessage);
    }

    protected final void updateInventoryLater(Player player) {
        JavaPlugin plugin = getPlugin();
        BukkitScheduler scheduler = Bukkit.getScheduler();
        scheduler.runTaskLater(plugin, player::closeInventory, 1L);
    }
}
