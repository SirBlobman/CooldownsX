package com.github.sirblobman.cooldowns.command;

import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.sirblobman.api.command.Command;
import com.github.sirblobman.cooldowns.api.ICooldownsX;

public final class CommandCooldownsX extends Command {
    public CommandCooldownsX(@NotNull ICooldownsX plugin) {
        super(plugin.getPlugin(), "cooldownsx");
        setPermissionName("cooldownsx.command.cooldownsx");
    }

    @Override
    protected @NotNull List<String> onTabComplete(@NotNull CommandSender sender, String @NotNull [] args) {
        return Collections.emptyList();
    }

    @Override
    protected boolean execute(@NotNull CommandSender sender, String @NotNull [] args) {
        try {
            JavaPlugin plugin = getPlugin();
            plugin.reloadConfig();

            sendMessage(sender, "command.reload-success");
        } catch (Exception ex) {
            sendMessage(sender, "command.reload-failure");
        }

        return true;
    }
}
