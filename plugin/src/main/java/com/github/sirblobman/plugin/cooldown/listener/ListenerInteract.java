package com.github.sirblobman.plugin.cooldown.listener;

import java.util.Set;

import org.jetbrains.annotations.NotNull;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.github.sirblobman.api.utility.ItemUtility;
import com.github.sirblobman.plugin.cooldown.api.CooldownsX;
import com.github.sirblobman.plugin.cooldown.api.configuration.Cooldown;
import com.github.sirblobman.plugin.cooldown.api.configuration.CooldownType;
import com.github.sirblobman.plugin.cooldown.api.data.PlayerCooldown;
import com.github.sirblobman.plugin.cooldown.api.listener.CooldownListener;
import com.github.sirblobman.plugin.cooldown.modern.ModernHelper;
import com.github.sirblobman.api.shaded.xseries.XMaterial;

public final class ListenerInteract extends CooldownListener {
    public ListenerInteract(@NotNull CooldownsX plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onInteract(PlayerInteractEvent e) {
        Action action = e.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = e.getPlayer();
        if (action == Action.RIGHT_CLICK_BLOCK) {
            Block block = e.getClickedBlock();
            if (block != null) {
                checkBlock(player, block, e);
            }
        }

        ItemStack item = e.getItem();
        checkItem(player, item, e);
    }

    private void checkItem(@NotNull Player player, @NotNull ItemStack item, @NotNull PlayerInteractEvent e) {
        if (ItemUtility.isAir(item)) {
            return;
        }

        if (ModernHelper.isCrossbowReloading(item)) {
            return;
        }

        XMaterial material = getXMaterial(item);
        if (material == XMaterial.AIR) {
            return;
        }

        Set<Cooldown> cooldownSettingsList = fetchCooldowns(CooldownType.INTERACT_ITEM);
        if (cooldownSettingsList.isEmpty()) {
            return;
        }

        PlayerCooldown cooldownData = getCooldownData(player);
        Set<Cooldown> allActiveCooldowns = cooldownData.getActiveCooldowns(CooldownType.INTERACT_ITEM);
        Set<Cooldown> activeCooldowns = filter(allActiveCooldowns, material);

        Cooldown activeCooldown = checkActiveCooldowns(player, activeCooldowns);
        if (activeCooldown != null) {
            e.setUseItemInHand(Result.DENY);
            sendCooldownMessage(player, activeCooldown, material);
            updateInventoryLater(player);
            return;
        }

        Set<Cooldown> allValidCooldowns = fetchCooldowns(CooldownType.INTERACT_ITEM);
        Set<Cooldown> validCooldowns = filter(allValidCooldowns, material);
        checkValidCooldowns(player, validCooldowns);
    }

    private void checkBlock(@NotNull Player player, @NotNull Block block, @NotNull PlayerInteractEvent e) {
        XMaterial material = getXMaterial(block);
        if (material == XMaterial.AIR) {
            return;
        }

        Set<Cooldown> cooldownSettingsList = fetchCooldowns(CooldownType.INTERACT_BLOCK);
        if (cooldownSettingsList.isEmpty()) {
            return;
        }

        PlayerCooldown cooldownData = getCooldownData(player);
        Set<Cooldown> allActiveCooldowns = cooldownData.getActiveCooldowns(CooldownType.INTERACT_BLOCK);
        Set<Cooldown> activeCooldowns = filter(allActiveCooldowns, material);

        Cooldown activeCooldown = checkActiveCooldowns(player, activeCooldowns);
        if (activeCooldown != null) {
            e.setUseInteractedBlock(Result.DENY);
            sendCooldownMessage(player, activeCooldown, material);
            updateInventoryLater(player);
            return;
        }

        Set<Cooldown> allValidCooldowns = fetchCooldowns(CooldownType.INTERACT_BLOCK);
        Set<Cooldown> validCooldowns = filter(allValidCooldowns, material);
        checkValidCooldowns(player, validCooldowns);
    }
}
