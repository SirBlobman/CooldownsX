package com.SirBlobman.enderpearl.config;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import com.SirBlobman.enderpearl.EnderpearlCooldown;

public class Config {
	private static final File FOLDER = EnderpearlCooldown.FOLDER;
	
	/**
	 * Loads a configuration file
	 * 
	 * @param fileName The name of the file which is inside of "/plugins/EnderpearlCooldown/"
	 * @return {@link YamlConfiguration} if it could be loaded, {@code null} if fileName is null or if the config failed to load
	 */
	public static YamlConfiguration loadConfig(String fileName) {
		try {
			File file = new File(FOLDER, fileName);
			if(!file.exists()) {
				FOLDER.mkdirs();
				file.createNewFile();
			}
			
			YamlConfiguration config = new YamlConfiguration();
			config.load(file);
			return config;
		} catch(Throwable ex) {
			String error = "Failed to load file '" + FOLDER.getPath() + fileName + "' as a YamlConfiguration:";
			EnderpearlCooldown.LOG.severe(error);
			ex.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Saves a configuration to a file.<br/>
	 * If you use this, all the comments will be deleted and any config options will be overriden
	 * 
	 * @param config The configuration you want to save in YAML format
	 * @param fileName The name of the file in "/plugins/EnderpearlCooldown/"
	 */
	public static void saveConfig(YamlConfiguration config, String fileName) {
		try {
			File file = new File(FOLDER, fileName);
			if(!file.exists()) {
				FOLDER.mkdirs();
				file.createNewFile();
			}
			
			config.save(file);
		} catch(Throwable ex) {
			String error = "Failed to save file '" + FOLDER.getPath() + fileName + "' as a YamlConfiguration:";
			EnderpearlCooldown.LOG.severe(error);
			ex.printStackTrace();
		}
	}
	
	/**
	 * This will copy a file in the jar file for this plugin to "/plugins/EnderpearlCooldown/"<br/>
	 * The file will only be copied IF the file does not already exist in that directory
	 * @param fileName The name of the file to copy
	 */
	public static void copyFromJar(String fileName) {
		try {
			JavaPlugin pl = EnderpearlCooldown.INSTANCE;
			InputStream is = pl.getResource(fileName);
			File file = new File(FOLDER, fileName);
			if(!file.exists()) {
				FOLDER.mkdirs();
				String absolutePath = file.getAbsolutePath();
				Path path = Paths.get(absolutePath);
				Files.copy(is, path, StandardCopyOption.REPLACE_EXISTING);
			}
		} catch(Throwable ex) {
			String error = "Failed to copy '" + fileName + "' from the jar file:";
			EnderpearlCooldown.LOG.severe(error);
			ex.printStackTrace();
		}
	}
}