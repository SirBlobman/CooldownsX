package com.github.sirblobman.cooldowns.command;

import java.util.Collections;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.sirblobman.api.command.Command;
import com.github.sirblobman.cooldowns.CooldownPlugin;

public final class CommandCooldownsX extends Command {
    public CommandCooldownsX(CooldownPlugin plugin) {
        super(plugin, "cooldownsx");
        setPermissionName("cooldownsx.command.cooldownsx");
    }

    @Override
    protected List<String> onTabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }

    @Override
    protected boolean execute(CommandSender sender, String[] args) {
        try {
            JavaPlugin plugin = getPlugin();
            plugin.reloadConfig();

            sendMessage(sender, "command.reload-success", null);
        } catch (Exception ex) {
            sendMessage(sender, "command.reload-failure", null);
        }

        return true;
    }
}
