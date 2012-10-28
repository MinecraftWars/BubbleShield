package net.sqdmc.bubbleshield;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Logger;

import net.sqdmc.bubbleshield.ShieldListener;
import net.sqdmc.bubbleshield.BSConfiguration;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class BubbleShield extends JavaPlugin {
	PluginManager pluginmanager;
	Logger log = Bukkit.getServer().getLogger();
	private static final String PLUGIN_NAME = "BubbleShield";
	private static String version;
	private BSConfiguration config = new BSConfiguration(this);
	private final ShieldListener entityListener = new ShieldListener(this);
		
	public void onEnable() {
		PluginDescriptionFile pdfFile = getDescription();
		version = pdfFile.getVersion();

		try {
			config.loadConfig();
			config.LoadShieldFromFile();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		entityListener.setShieldDurability(config.loadDurabilityFromFile());

		log.info(PLUGIN_NAME + " v" + version + " enabled");
		
		pluginmanager = getServer().getPluginManager();
		
		
		
		getServer().getPluginManager().registerEvents(entityListener, this);
	}
	
	public void onDisable(){
		try {
			config.saveDurabilityToFile();
			config.SaveShieldsToFile();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		log.info(PLUGIN_NAME + " disabled");
	}
	
	public BSConfiguration getBSConfig() {
		return config;
	}
	
	public ShieldListener getListener() {
		return entityListener;
	}
	
	public static String getPluginName() {
		return PLUGIN_NAME;
	}
	
	public static String getVersion() {
		return version;
	}
}
