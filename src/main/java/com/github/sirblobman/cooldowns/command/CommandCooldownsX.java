package com.github.sirblobman.cooldowns.command;

import java.util.Collections;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.github.sirblobman.api.command.Command;
import com.github.sirblobman.cooldowns.CooldownPlugin;

public final class CommandCooldownsX extends Command {
    private final CooldownPlugin plugin;
    public CommandCooldownsX(CooldownPlugin plugin) {
        super(plugin, "cooldownsx");
        this.plugin = plugin;
    }

    @Override
    protected List<String> onTabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }

    @Override
    protected boolean execute(CommandSender sender, String[] args) {
        this.plugin.onReload();
        sender.sendMessage(ChatColor.GREEN + "Successfully reloaded the configuration files for CooldownsX.");
        return true;
    }
}
