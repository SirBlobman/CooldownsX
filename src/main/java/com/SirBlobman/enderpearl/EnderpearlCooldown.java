package com.SirBlobman.enderpearl;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import com.SirBlobman.enderpearl.config.ConfigSettings;
import com.SirBlobman.enderpearl.hook.HookPlaceholdersClip;
import com.SirBlobman.enderpearl.hook.HookPlaceholdersMVdW;
import com.SirBlobman.enderpearl.listener.ListenEnderpearl;
import com.SirBlobman.enderpearl.utility.ECooldownUtil;

import java.io.File;
import java.util.logging.Logger;

public class EnderpearlCooldown extends JavaPlugin {
    public static Logger LOG;
    public static EnderpearlCooldown INSTANCE;
    public static File FOLDER;
    
    @Override
    public void onEnable() {
        INSTANCE = this;
        FOLDER = getDataFolder();
        LOG = getLogger();
        
        ConfigSettings.load();
        
        Bukkit.getPluginManager().registerEvents(new ListenEnderpearl(), this);
        Bukkit.getScheduler().runTaskTimer(this, new ECooldownUtil(), 0L, 1L);
        
        if(Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            HookPlaceholdersClip hook = new HookPlaceholdersClip();
            hook.register();
        }
        
        if(Bukkit.getPluginManager().isPluginEnabled("MVdWPlaceholderAPI")) {
            HookPlaceholdersMVdW hook = new HookPlaceholdersMVdW(this);
            hook.register();
        }
    }
}