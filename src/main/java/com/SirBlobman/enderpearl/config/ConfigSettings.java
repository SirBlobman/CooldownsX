package com.SirBlobman.enderpearl.config;

import org.bukkit.configuration.file.YamlConfiguration;

public class ConfigSettings extends Config {
	private static YamlConfiguration config = new YamlConfiguration();
	public static void save() {saveConfig(config, "config.yml");}
	public static YamlConfiguration load() {
		copyFromJar("config.yml");
		config = loadConfig("config.yml");
		return config;
	}
	
	@SuppressWarnings("unchecked")
    public static <O> O getOption(String path, O defaultValue) {
	    load();
	    if(config.isSet(path)) {
	        Object obj = config.get(path);
	        Class<?> valClass = defaultValue.getClass();
	        if(valClass.isInstance(obj)) {
	            O o = (O) obj;
	            return o;
	        }
	    }
	    
	    return defaultValue;
	}
}