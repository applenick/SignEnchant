package com.sky8the2flies.util;

import java.io.File;
import java.io.IOException;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

public class SettingsManager {
	static SettingsManager instance = new SettingsManager();
	Plugin p;
	FileConfiguration tokens;
	File tokensFile;

	public static SettingsManager getInstance() {
		return instance;
	}

	public void setup(Plugin p) {
		this.tokensFile = new File(p.getDataFolder(), "tokens.yml");
		if (!this.tokensFile.exists()) {
			try {
				this.tokensFile.createNewFile();
			} catch (IOException e) {
				Bukkit.getServer().getLogger()
						.severe(ChatColor.RED + "Could not create tokens.yml!");
			}
		}

		this.tokens = YamlConfiguration.loadConfiguration(this.tokensFile);
	}

	public FileConfiguration getTokens() {
		return this.tokens;
	}

	public void saveTokens() {
		try {
			this.tokens.save(this.tokensFile);
		} catch (IOException e) {
			Bukkit.getServer().getLogger()
					.severe(ChatColor.RED + "Could not save tokens.yml!");
		}
	}

	public void reloadTokens() {
		this.tokens = YamlConfiguration.loadConfiguration(this.tokensFile);
	}
}