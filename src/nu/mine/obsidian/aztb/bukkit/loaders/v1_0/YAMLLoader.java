package nu.mine.obsidian.aztb.bukkit.loaders.v1_0;

/* Copyright (C) 2014 Nicklas Damgren (aka AnorZaken)
 * 
 * This file is part of AZTB (AnorZakens ToolBox).
 *
 * AZTB is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AZTB is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with AZTB.  If not, see <http://www.gnu.org/licenses/>.
*/

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Tool-class that helps with yaml-config loading. <p/>
 * Pros:<br>
 * &nbsp 1. if loading fails you can check why (FileNotFound / InvalidConfiguration / Other)<br>
 * &nbsp 2. it prints messages / logs on failure (FileNotFound message can be toggled off*)
 * <p/>(*<i>Sometimes you want to load a file </i>if<i> it exists - then FileNotFound is not an error.</i>)
 * 
 * @author AnorZaken
 * @version 1.1
 * 
 * @param <T> {@link JavaPlugin} using {@link YAMLLoader}
 */
public class YAMLLoader<T extends JavaPlugin>
{
	protected final T plugin;
	protected final String filename;
	
	/**
	 * Creates a YAMLLoader for the specified plugin and config-file.
	 * @param plugin {@link JavaPlugin} using YAMLLoader
	 * @param filename of the yaml-config to load
	 * @throws IllegalArgumentException if any parameter is {@code null} or {@code filename}
	 *  is an empty String.
	 */
	public YAMLLoader(final T plugin, final String filename)
	{
		if (plugin == null)
			throw new IllegalArgumentException("plugin can not be null");
		if (filename == null)
			throw new IllegalArgumentException("filename can not be null");
		else if (filename.length() == 0)
			throw new IllegalArgumentException("filename can not be an empty String");
		this.plugin = plugin;
		this.filename = filename;
	}
	
	// ===============
	
	/**
	 * Get a {@link File}-object for the file associated with this {@link YAMLLoader}. <p/>
	 * (<i>Not cached!</i>)
	 */
	public File getFile() {
		return new File(plugin.getDataFolder(), filename);
	}
	
	/**
	 * Get the name of the file associated with this {@link YAMLLoader}.
	 */
	public String getFileName() {
		return filename;
	}
	
	// ===============
	
	/**
	 * Class containing the result of an yaml-loading attempt.
	 * @author AnorZaken
	 * @see YAMLLoader#loadYaml(CommandSender, boolean, String)
	 */
	public static class YAMLResult
	{
		private YAMLResult(){}
		/**
		 * The loaded yaml-configuration. Will be {@code null} if loading failed.
		 */
		public YamlConfiguration yaml = new YamlConfiguration();
		/**
		 * Will be {@code true} if attempting to load resulted in FileNotFound.
		 */
		public boolean isFileFound = true;
		/**
		 * Will be {@code true} if attempting to load resulted in InvalidConfiguration.
		 */
		public boolean isValidConfig = true;
	}
	
	/**
	 * Loads the yaml-file associated with this {@link YAMLLoader} and returns the result in the form of an
	 * {@link YAMLLoader.YAMLResult}-object.
	 * @param sender {@link CommandSender} to send messages to (or {@code null} if silent operation is desired)
	 * @param failIfFileNotFound if this is {@code true} FileNotFound will be treated like a failed loading
	 * @param errorLoadingMsg message to send if loading fails (if this is {@code null} a default message will be used)
	 * <br>&nbsp&nbsp&nbsp(<i>Default: "Error loading config, see server log for details."</i>)
	 * @return a {@link YAMLLoader.YAMLResult} object
	 * @throws IllegalStateException if the plugin associated with this {@link YAMLLoader} isn't properly enabled
	 */
	public YAMLResult loadYaml(final CommandSender sender, boolean failIfFileNotFound, final String errorLoadingMsg)
	{
		if (!plugin.isEnabled() || plugin.getDataFolder() == null)
			throw new IllegalStateException("plugin is not properly enabled");
		
		final File configFile = getFile();
		final YAMLResult yamlResult = new YAMLResult();
		
		try
        {
			yamlResult.yaml.load(configFile);
        }
		catch (FileNotFoundException ex)
		{
			if (failIfFileNotFound)
			{
				if (sender != null)
				{
					if (errorLoadingMsg != null)
						sender.sendMessage(errorLoadingMsg);
					else
						sender.sendMessage(ChatColor.RED + "Error loading config, see server log for details.");
					plugin.getLogger().log(Level.WARNING, 
							(new StringBuilder()).append("Cannot load ").append(configFile).toString() + " : " + ex.getClass().toString());
				}
				yamlResult.yaml = null;
			}
			yamlResult.isFileFound = false;
		}
		catch (InvalidConfigurationException ex)
        {
			if (sender != null)
			{
				if (errorLoadingMsg != null)
					sender.sendMessage(errorLoadingMsg);
				else
					sender.sendMessage(ChatColor.RED + "Error loading config, see server log for details.");
				plugin.getLogger().log(Level.WARNING, 
						(new StringBuilder()).append("Cannot load ").append(configFile).toString() + " : " + ex.getClass().toString());
			}
			yamlResult.isValidConfig = false;
			yamlResult.yaml = null;
        }
		catch (Exception ex)
        {
			if (sender != null)
			{
				if (errorLoadingMsg != null)
					sender.sendMessage(errorLoadingMsg);
				else
					sender.sendMessage(ChatColor.RED + "Error loading config, see server log for details.");
				plugin.getLogger().log(Level.WARNING, 
						(new StringBuilder()).append("Cannot load ").append(configFile).toString() + " : " + ex.getClass().toString());
			}
			yamlResult.yaml = null;
        }
		
		return yamlResult;
	}
	
	
	/**
	 * Saves a {@link YamlConfiguration} to the yaml-file associated with this {@link YAMLLoader}. 
	 * @param sender {@link CommandSender} to send messages to (or {@code null} if silent operation is desired)
	 * @param config
	 * @param errorSavingMsg message to send if saving fails (if this is {@code null} a default message will be used)
	 * <br>&nbsp&nbsp&nbsp(<i>Default: "Error saving config, see server log for details."</i>)
	 * @return <code>true</code> if the config was saved successfully, <code>false</code> otherwise
	 * @throws IllegalStateException if the plugin associated with this {@link YAMLLoader} isn't properly enabled
	 * @throws IllegalArgumentException if the {@link YamlConfiguration} is <code>null</code>
	 */
	public boolean saveYaml(final CommandSender sender, final YamlConfiguration config, final String errorSavingMsg)
	{
		if (config == null)
			throw new IllegalArgumentException("config == null");
		if (!plugin.isEnabled() || plugin.getDataFolder() == null)
			throw new IllegalStateException("plugin is not properly enabled");
		
		final File configFile = getFile();
		
		try
        {
			config.save(configFile);
        }
		catch(IOException ex)
        {
			if (sender != null)
			{
				if (errorSavingMsg != null)
					sender.sendMessage(errorSavingMsg);
				else
					sender.sendMessage(ChatColor.RED + "Error saving config, see server log for details.");
				plugin.getLogger().log(Level.WARNING, 
						(new StringBuilder()).append("Could not save config to ").append(configFile).toString() + " : " + ex.getClass().toString());
			}
			return false;
        }
		return true;
	}
}
