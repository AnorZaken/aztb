package nu.mine.obsidian.aztb.bukkit.plugin.wip;

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
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.apache.commons.lang.Validate;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginBase;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.PluginLogger;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.plugin.UnknownDependencyException;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.EbeanServerFactory;
import com.avaje.ebean.config.DataSourceConfig;
import com.avaje.ebean.config.ServerConfig;

/**
 * {@link Plugin} and {@link PluginLoader} classes to enable loading of
 * {@link Plugin} classes from non-file sources.
 * 
 * @see RuntimePlugin
 * @see #loadPlugin(RuntimePlugin, Server)
 * @author AnorZaken
 * @version 0.1
 */
public class RuntimePlugins //TODO: mostly javadoc + testing // WIP - NOT FOR PRODUCTION USE
{
	// ================================
	// ============ Public ============
	// ================================
	
	/**
	 * {@link Plugin} base-class to <b>extend</b> from to enable direct loading.
	 * 
	 * @see RuntimePlugins#loadPlugin(RuntimePlugin, Server)
	 * @author AnorZaken
	 * @version 1.0
	 */
	public static class RuntimePlugin extends PluginBase
	{
		private boolean isLoaded = false;
		private boolean isEnabled = false;
		private boolean isNaggable = true;
		private PluginLoader loader = null;
	    private Server server = null;
	    private final PluginDescriptionFile description;
	    private File dataFolder = null;
	    private EbeanServer ebean = null;
	    private FileConfiguration fileConfig = null;
	    private File configFile = null;
	    private PluginLogger logger = null;
	    
	    // -----
	    
	    public RuntimePlugin(final String name, final String version) //TODO javadoc
	    		throws InvalidDescriptionException
	    {
	    	checkPluginDescription(name, version);
	    	this.description = new PluginDescriptionFile(name, version, "");
	    }
	    
	    public RuntimePlugin(final YamlConfiguration pluginDescriptionYAML) //TODO javadoc
	    		throws InvalidDescriptionException
	    {
	    	final StringReader reader = new StringReader( pluginDescriptionYAML.saveToString() );
	    	description = new PluginDescriptionFile(reader);
	    }
	    
	    public RuntimePlugin(final PluginDescriptionFile pdFile) //TODO javadoc
	    		throws InvalidDescriptionException
	    {
	    	checkPluginDescription(pdFile.getName(), pdFile.getVersion());
	    	description = pdFile;
	    }
	    
	    // -----
	    
	    /**
	     * Performs some basic checks on a plugins name and version Strings.
	     * 
	     * @param name plugin name
	     * @param version plugin version
	     * @throws InvalidDescriptionException if any of the checks fail
	     */
	    protected final void checkPluginDescription(final String name, final String version)
	    		throws InvalidDescriptionException
		{
	    	if (name == null || name.isEmpty())
	    		throw new InvalidDescriptionException("name must be non-NULL and non-empty!");
	    	if (version == null || version.isEmpty())
	    		throw new InvalidDescriptionException("version must be non-NULL and non-empty!");
	    	if(!name.matches("^[A-Za-z0-9 _.-]+$"))
	    		throw new InvalidDescriptionException("name '" + name + "' contains invalid characters.");
		}
	    
	    // -----
		
		@Override
		public List<String> onTabComplete(CommandSender arg0, Command arg1, String arg2, String[] arg3)
		{
			return null;
		}
		
		@Override
		public boolean onCommand(CommandSender arg0, Command arg1, String arg2, String[] arg3)
		{
			return false;
		}
		
		@Override
		public FileConfiguration getConfig()
		{
			if (fileConfig == null)
	            reloadConfig();
	        return fileConfig;
		}
		
		@Override
		public File getDataFolder() { return dataFolder; }
		
		@Override
		public EbeanServer getDatabase()
		{
			return ebean;
		}
		
		/**
		* Provides a list of all classes that should be persisted in the database.
		*
		* @return List of Classes that are Ebeans
		*/
	    public List<Class<?>> getDatabaseClasses() {
	        return new ArrayList<Class<?>>();
	    }
		
		@Override
		public ChunkGenerator getDefaultWorldGenerator(String worldName, String id)
		{
			return null;
		}
		
		@Override
		public PluginDescriptionFile getDescription() { return description; }
		
		@Override
		public Logger getLogger() { return logger; }
		
		@Override
		public PluginLoader getPluginLoader() { return loader; }
		
		/**
		 * Resource handling is not supported for RuntimePlugins!
		 */
		@Override
		public InputStream getResource(String filename)
		{
			throw new RuntimeException("Resource handling is not supported for RuntimePlugins!");
		}
		
		@Override
		public Server getServer() { return server == null ? Bukkit.getServer() : server; }
		
		@Override
		public boolean isEnabled() { return isEnabled; }
		
		/**
		 * Returns a value indicating whether or not this plugin is currently loaded.
		 * @return <code>true</code> if this plugin is loaded, otherwise <code>false</code>
		 */
		public boolean isLoaded() { return isLoaded; }
		
		@Override
		public boolean isNaggable() { return isNaggable; }
		
		
		@Override
		public void onDisable() {}
		@Override
		public void onEnable() {}
		@Override
		public void onLoad() {} //TODO: verify that this is only called once!
		
		
		@Override
		public void reloadConfig()
		{
			fileConfig = YamlConfiguration.loadConfiguration(configFile);
		}
		
		@Override
		public void saveConfig()
		{
			try {
	            getConfig().save(configFile);
	        } catch (IOException ex) {
	            logger.log(Level.SEVERE, "Could not save config to " + configFile, ex);
	        }
		}
		
		/**
		 * Checks if the plugin has a config file and creates an empty config if it doesn't. 
		 * <br>(RuntimePlugins doesn't have a default config resource!)
		 */
		@Override
		public void saveDefaultConfig() //TODO: change this?
		{
			try {
				configFile.createNewFile();
			} catch (IOException ex) {
				logger.log(Level.SEVERE, "Could not create config file " + configFile, ex);
			}
		}
		
		/**
		 * Resource handling is not supported for RuntimePlugins!
		 */
		@Override
		public void saveResource(String resourcePath, boolean replace)
		{
			throw new RuntimeException("Resource handling is not supported for RuntimePlugins!");
		}

		@Override
		public void setNaggable(boolean canNag) { isNaggable = canNag; }
		
		/**
		 * Convenience method for loading this {@link RuntimePlugin}. 
		 * <br>(Same as calling {@link RuntimePlugins#loadPlugin(RuntimePlugin, Server)})
		 * 
		 * @param server {@link Server} to load the plugin on
		 * @param enableAfterLoad if <code>true</code> the plugin will also be enabled after loading
		 * @return {@link LoadResult#SUCCESS} if the plugin was loaded successfully
		 *  (in which case {@link RuntimePlugin#onLoad()} is called before returning).
		 * @see LoadResult
		 * @throws UnknownDependencyException
		 * @throws InvalidPluginException
		 * @throws InvalidDescriptionException
		 */
		public LoadResult loadMe(final Server server, final boolean enableAfterLoad)
				throws UnknownDependencyException, InvalidPluginException, InvalidDescriptionException
		{
			final LoadResult result = RuntimePlugins.loadPlugin(this, server);
			if (result == LoadResult.SUCCESS)
				RuntimePlugins.enablePlugin(this);
			return result;
		}
		
	}
	
	// ========================
	
	/**
	 * Enum for the possible results of an attempted plugin load.
	 * @see LoadResult#LOAD_FAIL
	 * @see LoadResult#SUCCESS
	 * @see LoadResult#IO_FAIL
	 * @see LoadResult#TIMEOUT
	 * @see LoadResult#INTERRUPTED
	 * @see LoadResult#ALREADY_LOADED
	 * @see LoadResult#NAME_CONFLICT
	 * @see LoadResult#LOADER_INCOMPATIBILITY
	 */
	public enum LoadResult //TODO javadoc
	{
		LOAD_FAIL,
		SUCCESS,
		IO_FAIL,
		TIMEOUT,
		INTERRUPTED,
		ALREADY_LOADED,
		NAME_CONFLICT,
		LOADER_INCOMPATIBILITY,
	}
	
	// ========================
	
	/**
	 * Loads a {@link RuntimePlugin} on the specified {@link Server}. 
	 * <br><b>- Don't forget that you also need to enable it!</b>
	 * 
	 * @param plugin {@link RuntimePlugin} to load
	 * @param server {@link Server} to load it on
	 * @return {@link LoadResult#SUCCESS} if the plugin was loaded successfully
	 *  (in which case {@link RuntimePlugin#onLoad()} is called before returning).
	 * @see LoadResult
	 * @throws UnknownDependencyException
	 * @throws InvalidPluginException
	 * @throws InvalidDescriptionException
	 */
	public static LoadResult loadPlugin(final RuntimePlugin plugin, final Server server)
			throws UnknownDependencyException, InvalidPluginException, InvalidDescriptionException
	{
		Validate.notNull(server, "Server can not be null");
		Validate.notNull(plugin, "Plugin can not be null");
		
		final IRuntimePluginLoader loader = getRPLoader(server);
		if (loader == null)
			return LoadResult.LOADER_INCOMPATIBILITY;
		else
			return loader.loadPlugin(plugin);
	}
	
	/**
	 * Enables a loaded {@link RuntimePlugin}. 
	 * <br>(Convenience method)
	 * 
	 * @param plugin a loaded {@link RuntimePlugin} to enable
	 * @return <code>false</code> if the plugins isn't loaded, otherwise <code>true</code>.
	 */
	public static boolean enablePlugin(final RuntimePlugin plugin)
	{
		Validate.notNull(plugin, "Plugin can not be null");
		if (!plugin.isLoaded())
			return false;
		plugin.server.getPluginManager().enablePlugin(plugin);
		return true;
	}
	
	/**
	 * Disables a loaded {@link RuntimePlugin}. 
	 * <br>(Convenience method)
	 * 
	 * @param plugin a loaded {@link RuntimePlugin} to disable
	 * @return <code>false</code> if the plugins isn't loaded, otherwise <code>true</code>.
	 */
	public static boolean disablePlugin(final RuntimePlugin plugin)
	{
		Validate.notNull(plugin, "Plugin can not be null");
		if (!plugin.isLoaded())
			return false;
		plugin.server.getPluginManager().disablePlugin(plugin);
		return true;
	}
	
	// ========================
	
	public static interface IRuntimePluginLoaderPlugin  //TODO javadoc
	{
		public IRuntimePluginLoader getRPLoader();
	}
	
	// -----
	
	public static interface IRuntimePluginLoader  //TODO javadoc
	{
		public nu.mine.obsidian.aztb.bukkit.plugin.wip.RuntimePlugins.LoadResult loadPlugin(final RuntimePlugin plugin)
				throws UnknownDependencyException, InvalidPluginException, InvalidDescriptionException;
	}
	
	
	// =============================================
	// ============ Private / Protected ============
	// =============================================
	
	private static final String RUNTIME_PLUGIN_LOADER_NAME = "RuntimePluginLoader";
	private static final String RUNTIME_PLUGIN_LOADER_VERSION = "1.0";
	private static volatile IRuntimePluginLoader rpLoader = null;
	
	static
	{
		packageCheck();
	}
	
	// DO NOT CHANGE THIS UNLESS YOU KNOW EXACTLY WHAT YOU ARE DOING AND WHY THIS CHECK EXISTS!!!
	private static final void packageCheck() // ...or else other plugins could randomly fail!
	{
		if (!RuntimePlugins.class.getName().startsWith("nu.mine.obsidian.aztb.bukkit.plugin."))
			throw new RuntimeException("The RuntimePlugins class is not in the correct package!");
	}
	
	// ========================
	
	protected static synchronized IRuntimePluginLoader getRPLoader(final Server server) //TODO javadoc
			throws UnknownDependencyException, InvalidPluginException, InvalidDescriptionException
	{
		IRuntimePluginLoader rpLoader = RuntimePlugins.rpLoader;
		if (rpLoader != null)
			return rpLoader;
		else
		{
			Plugin rplPlugin = server.getPluginManager().getPlugin(RUNTIME_PLUGIN_LOADER_NAME);
			if (rplPlugin == null)
			{
				server.getPluginManager().registerInterface(RuntimePluginLoader.class);
				//^Calls constructor of RuntimePluginLoader -> sets RuntimePlugins.rpLoader to itself
				rpLoader = RuntimePlugins.rpLoader;
				rpLoader.loadPlugin(new RuntimePluginLoaderPlugin(rpLoader));
			}
			else if (rplPlugin instanceof RuntimePluginLoaderPlugin)
			{
				rpLoader = ((RuntimePluginLoaderPlugin)rplPlugin).getRPLoader();
			}
			else
			{
				try {
					final Method mPlugin = rplPlugin.getClass().getMethod("getRPLoader");
					final Class<?> cLoader = mPlugin.getReturnType();
					if (IRuntimePluginLoader.class.isAssignableFrom(cLoader))
						rpLoader = (IRuntimePluginLoader) mPlugin.invoke(rplPlugin);
					else
						getRPLoaderThrowingHelper(null);
				} catch (NoSuchMethodException ex) {
					getRPLoaderThrowingHelper(ex);
				} catch (SecurityException ex) {
					getRPLoaderThrowingHelper(ex);
				} catch (IllegalAccessException ex) {
					getRPLoaderThrowingHelper(ex);
				} catch (IllegalArgumentException ex) {
					getRPLoaderThrowingHelper(ex);
				} catch (InvocationTargetException ex) {
					getRPLoaderThrowingHelper(ex);
				}
			}
			return rpLoader; //can be null!
		}
	}
	
	// -----
	
	protected static void getRPLoaderThrowingHelper(Exception ex) {
		throw new RuntimeException("A plugin with name '" + RUNTIME_PLUGIN_LOADER_NAME + "' is already loaded but is not method signature compatible.", ex);
	}
	
	// ========================
	
	/**
	 * Plugin that holds a reference to the currently instantiated {@link RuntimePluginLoader}.
	 * @author AnorZaken
	 * @version 1.0
	 */
	protected static class RuntimePluginLoaderPlugin extends RuntimePlugin implements IRuntimePluginLoaderPlugin
	{
		protected final IRuntimePluginLoader loader;
		
		public RuntimePluginLoaderPlugin(final IRuntimePluginLoader loader)
				throws InvalidDescriptionException
		{
			super(RUNTIME_PLUGIN_LOADER_NAME, RUNTIME_PLUGIN_LOADER_VERSION);
			Validate.notNull(loader, "Provided RuntimePluginLoader is NULL.");
			this.loader = loader;
		}
		
		@Override
		public IRuntimePluginLoader getRPLoader()
		{
			return loader;
		}
	}
	
	// ========================
	
	/**
	 * {@link PluginLoader} for {@link RuntimePlugin} plugins.
	 * 
	 * @author AnorZaken
	 * @version 1.0
	 */
	protected static class RuntimePluginLoader implements PluginLoader, IRuntimePluginLoader
	{
		private final File runtimeDir = new File("runtimeplugins");
		private final String configName = "config.runtimeplugin.yml";
		private final Pattern[] filePatterns = new Pattern[]{ Pattern.compile("^config\\.runtimeplugin\\.yml$") };
		private final Server server;
		
		// -----
		
		public RuntimePluginLoader(final Server server)
		{
			Validate.notNull(server, "Server can not be null");
			this.server = server;
			RuntimePlugins.rpLoader = this;
		}
		
		// -----

		@Override
		public Map<Class<? extends Event>, Set<RegisteredListener>>
			createRegisteredListeners(Listener listener, Plugin plugin)
		{
			Validate.notNull(plugin, "Plugin can not be null");
	        Validate.notNull(listener, "Listener can not be null");
	        
	        server.getLogger().log(Level.SEVERE, String.format(
	        		"Plugin \"%s\" tried to automatically register events, this is not supported by %s v%s",
	        		plugin.getName(), RUNTIME_PLUGIN_LOADER_NAME, RUNTIME_PLUGIN_LOADER_VERSION ));
			return Collections.emptyMap(); //TODO not supported (yet)
		}

		@Override
		public void disablePlugin(Plugin plugin)
		{
			final RuntimePlugin rp;
			if (plugin instanceof RuntimePlugin)
				rp = (RuntimePlugin) plugin;
			else
				throw new IllegalArgumentException("Tried to disable a non-RuntimePlugin with RuntimePluginLoader");
			
			if (rp.isEnabled())
			{
				server.getPluginManager().callEvent(new PluginDisableEvent(plugin));
				try {
					rp.onDisable();
				} catch (Throwable ex) {
					server.getLogger().log(Level.SEVERE, "Error occurred while disabling " + plugin.getDescription().getFullName(), ex);
				}
				rp.isEnabled = false;
			}
		}

		@Override
		public void enablePlugin(Plugin plugin)
		{
			final RuntimePlugin rp;
			if (plugin instanceof RuntimePlugin)
				rp = (RuntimePlugin) plugin;
			else
				throw new IllegalArgumentException("Tried to enable a non-RuntimePlugin with RuntimePluginLoader");
			
			if (!rp.isEnabled())
			{
				rp.isEnabled = true;
				try {
					rp.onEnable();
				} catch (Throwable ex) {
					server.getLogger().log(Level.SEVERE, "Error occurred while enabling " + plugin.getDescription().getFullName(), ex);
				}
				server.getPluginManager().callEvent(new PluginEnableEvent(plugin));
			}
		}
		
		@Override
		public Pattern[] getPluginFileFilters()
		{
			return filePatterns.clone();
		}
		
		// -----
		
		private volatile RuntimePlugin plugin2load = null;
		private final Lock plugin2loadLock = new ReentrantLock(true);
		
		// ---
		
		@Override
		public PluginDescriptionFile getPluginDescription(File file)
				throws InvalidDescriptionException
		{
			final RuntimePlugin plugin2load = this.plugin2load;
			if (plugin2load == null)
				throw new InvalidDescriptionException("RuntimePlugins doesn't load this way...");
			else
				return plugin2load.description;
		}

		@Override
		public Plugin loadPlugin(File file)
				throws InvalidPluginException, UnknownDependencyException
		{
			final RuntimePlugin plugin2load = this.plugin2load;
			if (plugin2load != null)
			{
				plugin2load.loader = this;
				setUpDatabase(plugin2load);
				plugin2load.isLoaded = true;
			}
			return plugin2load;
		}
		
		private final void setUpDatabase(final RuntimePlugin plugin)
		{
			synchronized (plugin)
			{
				if (plugin.getDescription().isDatabaseEnabled() && plugin.ebean == null)
				{
		            ServerConfig db = new ServerConfig();

		            db.setDefaultServer(false);
		            db.setRegister(false);
		            db.setClasses(plugin.getDatabaseClasses());
		            db.setName(plugin.getName());
		            server.configureDbConfig(db);

		            DataSourceConfig ds = db.getDataSourceConfig();

		            ds.setUrl(replaceDatabaseString(ds.getUrl(), plugin.dataFolder, plugin.getName()));
		            plugin.dataFolder.mkdirs();
		            plugin.ebean = EbeanServerFactory.create(db);
		        }
			}
		}
		
		private final String replaceDatabaseString(String input, final File dataFolder, final String pluginName)
		{
	        input = input.replaceAll("\\{DIR\\}", dataFolder.getPath().replaceAll("\\\\", "/") + "/");
	        input = input.replaceAll("\\{NAME\\}", pluginName.replaceAll("[^\\w_-]", ""));
	        return input;
	    }
		
		// -----
		
		@Override
		public LoadResult loadPlugin(final RuntimePlugin plugin)
				throws UnknownDependencyException, InvalidPluginException, InvalidDescriptionException
		{
			if (plugin == null)
				throw new IllegalArgumentException("plugin is NULL");
			
			if (plugin.isLoaded())
				return LoadResult.ALREADY_LOADED;
			
			{
				final Plugin loaded = server.getPluginManager().getPlugin(plugin.getName());
				if (loaded != null)
					return LoadResult.NAME_CONFLICT;
			}
			
			try
			{
				if (plugin2loadLock.tryLock(2047L, TimeUnit.MILLISECONDS)) //TODO: configurable time-limit
				{
					plugin2load = plugin;
					final File dataDir = new File(runtimeDir, plugin.getName());
					if (dataDir.exists() && !dataDir.isDirectory())
			            throw new InvalidPluginException(String.format(
			                "Projected datafolder: \"%s\" for %s exists and is not a directory",
			                dataDir, plugin.getDescription().getFullName() ));
					dataDir.mkdirs();
					final File configFile = new File(dataDir, configName);
					try
					{
						configFile.createNewFile();
					}
					catch (IOException ex) {
						server.getLogger().log(Level.SEVERE, "Could not create config file " + configFile, ex);
						server.getLogger().log(Level.SEVERE, "Loading of " + plugin.getName() + " failed.");
						return LoadResult.IO_FAIL;
					}
					plugin.configFile = configFile;
					plugin.dataFolder = dataDir;
					plugin.logger = new PluginLogger(plugin);
					if (server.getPluginManager().loadPlugin(configFile) == null)
					{
						server.getLogger().log(Level.SEVERE, "Loading of " + plugin.getName() + " failed.");
						return LoadResult.LOAD_FAIL;
					}
					else if (plugin.isLoaded())
					{
						plugin.onLoad();
						return LoadResult.SUCCESS;
					}
					else
						return LoadResult.LOAD_FAIL;
				}
				else
				{
					server.getLogger().log(Level.SEVERE, "Failed to aquire RuntimePluginLoader lock within the time limit");
					server.getLogger().log(Level.SEVERE, "Loading of " + plugin.getName() + " failed.");
					return LoadResult.TIMEOUT;
				}
			}
			catch (InterruptedException ex) {
				server.getLogger().log(Level.SEVERE, "Loading of " + plugin.getName() + " was interrupted.");
				Thread.currentThread().interrupt();
				return LoadResult.INTERRUPTED;
			}
			finally
			{
				plugin2load = null;
				plugin2loadLock.unlock();
			}
		}
	}
}