package com.github.sirblobman.cooldowns.listener;

import java.util.Set;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.github.sirblobman.api.utility.ItemUtility;
import com.github.sirblobman.api.shaded.xseries.XMaterial;
import com.github.sirblobman.cooldowns.CooldownPlugin;
import com.github.sirblobman.cooldowns.api.configuration.CooldownType;
import com.github.sirblobman.cooldowns.api.configuration.ICooldownSettings;
import com.github.sirblobman.cooldowns.api.data.ICooldownData;
import com.github.sirblobman.cooldowns.api.listener.CooldownListener;
import com.github.sirblobman.cooldowns.modern.ModernHelper;

public final class ListenerInteract extends CooldownListener {
    public ListenerInteract(CooldownPlugin plugin) {
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

    private void checkItem(Player player, ItemStack item, PlayerInteractEvent e) {
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

        Set<ICooldownSettings> cooldownSettingsList = fetchCooldowns(CooldownType.INTERACT_ITEM);
        if (cooldownSettingsList.isEmpty()) {
            return;
        }

        ICooldownData cooldownData = getCooldownData(player);
        Set<ICooldownSettings> allActiveCooldowns = cooldownData.getActiveCooldowns(CooldownType.INTERACT_ITEM);
        Set<ICooldownSettings> activeCooldowns = filter(allActiveCooldowns, material);

        ICooldownSettings activeCooldown = checkActiveCooldowns(player, activeCooldowns);
        if (activeCooldown != null) {
            e.setUseItemInHand(Result.DENY);
            sendCooldownMessage(player, activeCooldown, material);
            updateInventoryLater(player);
            return;
        }

        Set<ICooldownSettings> allValidCooldowns = fetchCooldowns(CooldownType.INTERACT_ITEM);
        Set<ICooldownSettings> validCooldowns = filter(allValidCooldowns, material);
        checkValidCooldowns(player, validCooldowns);
    }

    private void checkBlock(Player player, Block block, PlayerInteractEvent e) {
        XMaterial material = getXMaterial(block);
        if (material == XMaterial.AIR) {
            return;
        }

        Set<ICooldownSettings> cooldownSettingsList = fetchCooldowns(CooldownType.INTERACT_BLOCK);
        if (cooldownSettingsList.isEmpty()) {
            return;
        }

        ICooldownData cooldownData = getCooldownData(player);
        Set<ICooldownSettings> allActiveCooldowns = cooldownData.getActiveCooldowns(CooldownType.INTERACT_BLOCK);
        Set<ICooldownSettings> activeCooldowns = filter(allActiveCooldowns, material);

        ICooldownSettings activeCooldown = checkActiveCooldowns(player, activeCooldowns);
        if (activeCooldown != null) {
            e.setUseInteractedBlock(Result.DENY);
            sendCooldownMessage(player, activeCooldown, material);
            updateInventoryLater(player);
            return;
        }

        Set<ICooldownSettings> allValidCooldowns = fetchCooldowns(CooldownType.INTERACT_BLOCK);
        Set<ICooldownSettings> validCooldowns = filter(allValidCooldowns, material);
        checkValidCooldowns(player, validCooldowns);
    }
}
