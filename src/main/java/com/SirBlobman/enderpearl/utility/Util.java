package com.SirBlobman.enderpearl.utility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.SirBlobman.enderpearl.EnderpearlCooldown;
import com.SirBlobman.enderpearl.config.ConfigSettings;

public class Util {
    public static String color(String o) {
        String c = ChatColor.translateAlternateColorCodes('&', o);
        return c;
    }
    
    public static List<String> colorLore(String... oo) {
        List<String> color = newList();
        Arrays.stream(oo).forEach(o -> {
            String c = color(o);
            color.add(c);
        });
        return color;
    }
    
    @SafeVarargs
    public static <L> List<L> newList(L... ll) {
        List<L> list = new ArrayList<>();
        list.addAll(Arrays.asList(ll));
        return list;
    }
    
    public static <K, V> Map<K, V> newMap() {
        Map<K, V> map = new HashMap<>();
        return map;
    }
    
    public static void log(String... ss) {
        Arrays.stream(ss).filter(s -> s != null && !s.isEmpty()).forEach(msg -> {
            EnderpearlCooldown.LOG.info(msg);
        });
    }
    
    public static void sendMessage(CommandSender sender, String message) {
        if(sender == null || message == null || message.isEmpty()) return;
        
        String color = color(message);
        sender.sendMessage(color);
    }
    
    public static void debug(String message) {
        if(ConfigSettings.getOption("debug", false)) {
            EnderpearlCooldown.LOG.info("[Debug] " + message);
        }
    }
}