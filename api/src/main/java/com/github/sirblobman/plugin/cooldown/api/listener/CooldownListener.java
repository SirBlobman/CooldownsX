package com.github.sirblobman.plugin.cooldown.api.listener;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.github.sirblobman.api.configuration.ConfigurationManager;
import com.github.sirblobman.api.folia.FoliaHelper;
import com.github.sirblobman.api.folia.scheduler.TaskScheduler;
import com.github.sirblobman.api.language.LanguageManager;
import com.github.sirblobman.api.language.replacer.DoubleReplacer;
import com.github.sirblobman.api.language.replacer.LongReplacer;
import com.github.sirblobman.api.language.replacer.Replacer;
import com.github.sirblobman.api.language.replacer.StringReplacer;
import com.github.sirblobman.api.plugin.ConfigurablePlugin;
import com.github.sirblobman.api.plugin.listener.PluginListener;
import com.github.sirblobman.plugin.cooldown.api.CooldownsX;
import com.github.sirblobman.plugin.cooldown.api.configuration.Cooldown;
import com.github.sirblobman.plugin.cooldown.api.configuration.CooldownType;
import com.github.sirblobman.plugin.cooldown.api.configuration.EnumDictionary;
import com.github.sirblobman.plugin.cooldown.api.data.PlayerCooldown;
import com.github.sirblobman.plugin.cooldown.api.data.PlayerCooldownManager;
import com.github.sirblobman.plugin.cooldown.api.task.PacketCooldownTask;
import com.github.sirblobman.plugin.cooldown.api.task.UpdateInventoryTask;
import com.github.sirblobman.api.shaded.adventure.text.Component;
import com.github.sirblobman.api.shaded.adventure.text.minimessage.MiniMessage;
import com.github.sirblobman.api.shaded.xseries.XMaterial;
import com.github.sirblobman.api.shaded.xseries.XPotion;

/**
 * A custom abstract listener class for all CooldownsX listeners.
 * This class includes useful methods for filtering cooldowns.
 *
 * @author SirBlobman
 */
public abstract class CooldownListener extends PluginListener<ConfigurablePlugin> {
    private final CooldownsX plugin;

    /**
     * @param plugin The CooldownsX plugin instance that this listener belongs to.
     */
    public CooldownListener(@NotNull CooldownsX plugin) {
        super(plugin.getPlugin());
        this.plugin = plugin;
    }

    /**
     * @return The CooldownsX plugin instance.
     */
    protected final @NotNull CooldownsX getCooldownsX() {
        return this.plugin;
    }

    /**
     * Convenience Method
     *
     * @return The configuration manager from the plugin.
     */
    protected final @NotNull ConfigurationManager getConfigurationManager() {
        CooldownsX plugin = getCooldownsX();
        return plugin.getConfigurationManager();
    }

    /**
     * Convenience Method
     *
     * @return The language manager from the plugin.
     */
    protected final @NotNull LanguageManager getLanguageManager() {
        CooldownsX plugin = getCooldownsX();
        return plugin.getLanguageManager();
    }

    /**
     * Convenience Method
     *
     * @return The cooldown manager from the plugin.
     */
    protected final @NotNull PlayerCooldownManager getCooldownManager() {
        CooldownsX plugin = getCooldownsX();
        return plugin.getCooldownManager();
    }

    /**
     * Convenience Method
     *
     * @return The material dictionary from the plugin.
     */
    protected final @NotNull EnumDictionary<XMaterial> getMaterialDictionary() {
        CooldownsX plugin = getCooldownsX();
        return plugin.getMaterialDictionary();
    }

    /**
     * Convenience Method
     *
     * @return The potion dictionary from the plugin.
     */
    protected final @NotNull EnumDictionary<XPotion> getPotionDictionary() {
        CooldownsX plugin = getCooldownsX();
        return plugin.getPotionDictionary();
    }

    protected final EnumDictionary<EntityType> getEntityDictionary() {
        CooldownsX plugin = getCooldownsX();
        return plugin.getEntityDictionary();
    }

    /**
     * Convenience Method
     *
     * @param player The player that owns the data.
     * @return The player data from the cooldown manager.
     */
    protected final @NotNull PlayerCooldown getCooldownData(@NotNull Player player) {
        PlayerCooldownManager cooldownManager = getCooldownManager();
        return cooldownManager.getData(player);
    }

    /**
     * Convenience Method
     *
     * @param stack The item to use to match the XMaterial value.
     * @return The XMaterial value, or {@link XMaterial#AIR} if there is an error.
     */
    protected final @NotNull XMaterial getXMaterial(@Nullable ItemStack stack) {
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

    /**
     * Convenience Method
     *
     * @param block The block to use to match the XMaterial value.
     * @return The XMaterial value, or {@link XMaterial#AIR} if there is an error.
     */
    protected final @NotNull XMaterial getXMaterial(@Nullable Block block) {
        if (block == null) {
            return XMaterial.AIR;
        }

        try {
            Material bukkitMaterial = block.getType();
            return XMaterial.matchXMaterial(bukkitMaterial);
        } catch (IllegalArgumentException ex) {
            return XMaterial.AIR;
        }
    }

    /**
     * Send a cooldown packet to a player with a 1 tick delay.
     *
     * @param player   The player that will receive the packet.
     * @param material The material that will have a cooldown.
     * @param ticks    The amount of time (in ticks) for the cooldown.
     */
    protected final void sendPacket(@NotNull Player player, @NotNull XMaterial material, int ticks) {
        Material bukkitMaterial = material.parseMaterial();
        if (bukkitMaterial == null) {
            return;
        }

        CooldownsX plugin = getCooldownsX();
        PacketCooldownTask task = new PacketCooldownTask(plugin, player, bukkitMaterial, ticks);
        plugin.getFoliaHelper().getScheduler().scheduleEntityTask(task);
    }

    /**
     * @param cooldownType The type to use as a filter.
     * @return All available cooldowns matching the specified type.
     */
    protected final @NotNull Set<Cooldown> fetchCooldowns(@NotNull CooldownType cooldownType) {
        PlayerCooldownManager cooldownManager = getCooldownManager();
        List<Cooldown> cooldownSettingsList = cooldownManager.getAllCooldownSettings();
        if (cooldownSettingsList.isEmpty()) {
            return Collections.emptySet();
        }

        return cooldownSettingsList.parallelStream()
                .filter(settings -> settings.getCooldownType() == cooldownType)
                .collect(Collectors.toSet());
    }

    /**
     * @param original The set of cooldowns to filter.
     * @param material The material to use as a filter.
     * @return A new {@link Set} of cooldowns filtered by the specified material.
     */
    protected final @NotNull Set<Cooldown> filter(@NotNull Set<Cooldown> original,
                                                  @NotNull XMaterial material) {
        if (original.isEmpty()) {
            return Collections.emptySet();
        }

        return original.parallelStream()
                .filter(settings -> settings.hasMaterial(material))
                .collect(Collectors.toSet());
    }

    /**
     * @param original The set of cooldowns to filter.
     * @param entityType The entity type to use as a filter.
     * @return A new {@link Set} of cooldowns filtered by the specified material.
     */
    protected final @NotNull Set<Cooldown> filter(@NotNull Set<Cooldown> original,
                                                  @NotNull EntityType entityType) {
        if (original.isEmpty()) {
            return Collections.emptySet();
        }

        return original.parallelStream()
                .filter(settings -> settings.hasEntity(entityType))
                .collect(Collectors.toSet());
    }

    /**
     * @param original The set of cooldowns to filter.
     * @param potion   The potion to use as a filter.
     * @return A new {@link Set} of cooldowns filtered by the specified potion.
     */
    protected final @NotNull Set<Cooldown> filter(@NotNull Set<Cooldown> original,
                                                  @NotNull XPotion potion) {
        if (original.isEmpty()) {
            return Collections.emptySet();
        }

        Set<XPotion> potions = Collections.singleton(potion);
        return filter(original, potions);
    }

    /**
     * @param original The set of cooldowns to filter.
     * @param potions  The list of potions to use as a filter.
     * @return A new {@link Set} of cooldowns filtered by the specified list of potions.
     */
    protected final @NotNull Set<Cooldown> filter(@NotNull Set<Cooldown> original,
                                                  @NotNull Iterable<XPotion> potions) {
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


    /**
     * Check if any of the specified cooldowns are active on a player.
     *
     * @param player    The player object.
     * @param cooldowns The set of cooldowns to check.
     * @return Null if there is not an active cooldown, or the first cooldown that is valid.
     */
    protected final @Nullable Cooldown checkActiveCooldowns(@NotNull Player player, @NotNull Set<Cooldown> cooldowns) {
        World world = player.getWorld();
        PlayerCooldown cooldownData = getCooldownData(player);

        for (Cooldown cooldown : cooldowns) {
            String id = cooldown.getId();
            printDebug("Checking cooldown with id '" + id + "'...");
            if (cooldown.isDisabled(world)) {
                printDebug("World is disabled, skipping.");
                continue;
            }

            if (cooldown.canBypass(player)) {
                printDebug("Player has bypass permission, skipping.");
                continue;
            }

            if (cooldown.checkCombatMode(player)) {
                printDebug("Player has combat mode mismatch, skipping.");
                continue;
            }

            long systemMillis = System.currentTimeMillis();
            long expireMillis = cooldownData.getCooldownExpireTime(cooldown);
            if (systemMillis >= expireMillis) {
                printDebug("Cooldown is already expired, skipping.");
                continue;
            }

            printDebug("Found active cooldown.");
            return cooldown;
        }

        printDebug("Failed to match any active cooldowns.");
        return null;
    }

    /**
     * Check and apply any of the specified cooldowns that are valid for the specified player.
     *
     * @param player    The player object.
     * @param cooldowns The set of cooldowns to check.
     */
    protected final void checkValidCooldowns(@NotNull Player player, @NotNull Set<Cooldown> cooldowns) {
        World world = player.getWorld();
        PlayerCooldown cooldownData = getCooldownData(player);
        String playerName = player.getName();
        printDebug("Checking if any cooldowns can be activated for player '" + playerName + "'.");

        for (Cooldown cooldown : cooldowns) {
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
                    printDebug("Cooldown has 'reset' enabled, setting amount to zero.");
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
                int ticks = (int) (cooldownSeconds * 20L);
                List<XMaterial> materialList = optionalMaterialList.get();
                for (XMaterial material : materialList) {
                    printDebug("Sent cooldown packet for material " + material);
                    sendPacket(player, material, ticks);
                }
            }
        }
    }

    /**
     * Send a cooldown message to a player for a material-based cooldown.
     *
     * @param player   The player that will receive the message.
     * @param settings The cooldown that contains the message format.
     * @param material The material for the cooldown.
     */
    protected final void sendCooldownMessage(@NotNull Player player, @NotNull Cooldown settings,
                                             @NotNull XMaterial material) {
        EnumDictionary<XMaterial> materialDictionary = getMaterialDictionary();
        String materialName = materialDictionary.get(material);
        Replacer replacer = new StringReplacer("{material}", materialName);
        sendCooldownMessage(player, settings, replacer);
    }

    /**
     * Send a cooldown message to a player for a potion-based cooldown.
     *
     * @param player   The player that will receive the message.
     * @param settings The cooldown that contains the message format.
     * @param potion   The potion for the cooldown.
     */
    protected final void sendCooldownMessage(@NotNull Player player, @NotNull Cooldown settings,
                                             @NotNull XPotion potion) {
        EnumDictionary<XPotion> potionDictionary = getPotionDictionary();
        String potionName = potionDictionary.get(potion);
        Replacer replacer = new StringReplacer("{potion}", potionName);
        sendCooldownMessage(player, settings, replacer);
    }

    /**
     * Send a cooldown message to a player for a potion-based cooldown.
     *
     * @param player   The player that will receive the message.
     * @param settings The cooldown that contains the message format.
     * @param entityType The entity type for the cooldown.
     */
    protected final void sendCooldownMessage(@NotNull Player player, @NotNull Cooldown settings,
                                             @NotNull EntityType entityType) {
        EnumDictionary<EntityType> entityDictionary = getEntityDictionary();
        String potionName = entityDictionary.get(entityType);
        Replacer replacer = new StringReplacer("{entity}", potionName);
        sendCooldownMessage(player, settings, replacer);
    }

    /**
     * Send a cooldown with a replaced message to a player.
     *
     * @param player   The player that will receive the message.
     * @param settings The cooldown that contains the message format.
     * @param replacer The replacer for placeholders in the message.
     */
    private void sendCooldownMessage(@NotNull Player player, @NotNull Cooldown settings,
                                     @NotNull Replacer replacer) {
        String messageFormat = settings.getMessageFormat();
        if (messageFormat == null || messageFormat.isEmpty()) {
            return;
        }

        PlayerCooldown cooldownData = getCooldownData(player);
        double expireTimeMillis = cooldownData.getCooldownExpireTime(settings);
        double systemTimeMillis = System.currentTimeMillis();
        double timeLeftMillis = Math.max(0.0D, expireTimeMillis - systemTimeMillis);
        double timeLeftSeconds = (timeLeftMillis / 1_000.0D);
        long timeLeftSecondsInteger = Math.round(timeLeftSeconds);


        LanguageManager languageManager = getLanguageManager();
        DecimalFormat decimalFormat = languageManager.getDecimalFormat(player);
        Replacer timeLeftIntegerReplacer = new LongReplacer("{time_left}", timeLeftSecondsInteger);
        Replacer timeLeftDecimalReplacer = new DoubleReplacer("{time_left_decimal}", timeLeftSeconds,
                decimalFormat);

        MiniMessage miniMessage = languageManager.getMiniMessage();
        Component message = miniMessage.deserialize(messageFormat)
                .replaceText(timeLeftIntegerReplacer.asReplacementConfig())
                .replaceText(timeLeftDecimalReplacer.asReplacementConfig())
                .replaceText(replacer.asReplacementConfig());
        languageManager.sendMessage(player, message);
    }

    /**
     * Print out a debugging message to the console
     *
     * @param message The message to print.
     * @see CooldownsX#isDebugMode()
     */
    protected final void printDebug(@NotNull String message) {
        Class<?> thisClass = getClass();
        String className = thisClass.getSimpleName();
        String fullMessage = String.format(Locale.US, "[%s] %s", className, message);

        CooldownsX plugin = getCooldownsX();
        plugin.printDebug(fullMessage);
    }

    /**
     * Update an inventory for a player. Runs one tick later.
     * @param player The player that needs their inventory closed.
     */
    protected final void updateInventoryLater(@NotNull Player player) {
        CooldownsX cooldownsX = getCooldownsX();
        FoliaHelper foliaHelper = cooldownsX.getFoliaHelper();
        TaskScheduler scheduler = foliaHelper.getScheduler();

        ConfigurablePlugin plugin = cooldownsX.getPlugin();
        UpdateInventoryTask task = new UpdateInventoryTask(plugin, player);
        scheduler.scheduleEntityTask(task);
    }
}
