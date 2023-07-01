package com.github.sirblobman.plugin.cooldown.api.task;

import org.jetbrains.annotations.NotNull;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import com.github.sirblobman.api.folia.details.EntityTaskDetails;
import com.github.sirblobman.api.nms.MultiVersionHandler;
import com.github.sirblobman.api.nms.PlayerHandler;
import com.github.sirblobman.plugin.cooldown.api.CooldownsX;

public final class PacketCooldownTask extends EntityTaskDetails<Player> {
    private final CooldownsX plugin;
    private final Material material;
    private final int ticks;

    public PacketCooldownTask(@NotNull CooldownsX plugin, @NotNull Player entity, @NotNull Material material,
                              int ticks) {
        super(plugin.getPlugin(), entity);
        setDelay(1L);

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
        PlayerHandler playerHandler = getPlayerHandler();
        playerHandler.sendCooldownPacket(player, material, ticks);
    }

    private @NotNull CooldownsX getCooldownsX() {
        return this.plugin;
    }

    private @NotNull MultiVersionHandler getMultiVersionHandler() {
        CooldownsX plugin = getCooldownsX();
        return plugin.getMultiVersionHandler();
    }

    private @NotNull PlayerHandler getPlayerHandler() {
        MultiVersionHandler multiVersionHandler = getMultiVersionHandler();
        return multiVersionHandler.getPlayerHandler();
    }

    private @NotNull Material getMaterial() {
        return this.material;
    }

    private int getTicks() {
        return this.ticks;
    }
}
