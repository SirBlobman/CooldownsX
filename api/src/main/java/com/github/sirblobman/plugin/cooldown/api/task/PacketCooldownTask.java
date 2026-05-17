package com.github.sirblobman.plugin.cooldown.api.task;

import org.jetbrains.annotations.NotNull;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.github.sirblobman.api.folia.details.EntityTaskDetails;
import com.github.sirblobman.plugin.cooldown.api.CooldownsX;

public final class PacketCooldownTask extends EntityTaskDetails<Player> {
    private final CooldownsX plugin;
    private final Material material;
    private final int ticks;

    public PacketCooldownTask(@NotNull CooldownsX plugin, @NotNull Player entity, @NotNull Material material,
                              int ticks) {
        super(plugin.getPlugin(), entity);

        this.plugin = plugin;
        this.material = material;
        this.ticks = ticks;
    }

    @Override
    public void run() {
        Player player = getEntity();
        if (player == null) {
            return;
        }

        int ticks = getTicks();
        Material material = getMaterial();

        // Use ItemStack variant - Paper reads the item's use_cooldown component
        // (cooldown_group) and applies cooldown to the correct group.
        // For 1.21.2+ items like ender_pearl this routes through the new cooldown
        // group system; for older items it falls back to per-material cooldown.
        try {
            ItemStack stack = new ItemStack(material);
            player.setCooldown(stack, ticks);
        } catch (Throwable t) {
            // Fallback for older API
            player.setCooldown(material, ticks);
        }
    }

    private @NotNull CooldownsX getCooldownsX() {
        return this.plugin;
    }

    private @NotNull Material getMaterial() {
        return this.material;
    }

    private int getTicks() {
        return this.ticks;
    }
}
