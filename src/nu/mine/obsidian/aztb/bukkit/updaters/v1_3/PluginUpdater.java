/*
 * Plugin Updater for Bukkit.
 *
 * This class provides the means to safely and easily update a plugin, or check to see if it is updated using dev.bukkit.org
 * 
 * "PluginUpdater" is a fork by AnorZaken from Gravitys "Updater v2.0" - For problems with this version you should contact AnorZaken, not Gravity!
 */

package nu.mine.obsidian.aztb.bukkit.updaters.v1_3;

/* Copyright (C) 2013-2014 Nicklas Damgren (aka AnorZaken)
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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.lang.NullArgumentException;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

/**
 * Check dev.bukkit.org to find updates for a given plugin, and download the updates if needed.
 * <p/>
 * <b>VERY, VERY IMPORTANT</b>: Because there are no standards for adding auto-update toggles in your plugin's config, this system provides NO CHECK WITH YOUR CONFIG to make sure the user has allowed auto-updating.
 * <br>
 * It is a <b>BUKKIT POLICY</b> that you include a boolean value in your config that prevents the auto-updater from running <b>AT ALL</b>.
 * <br>
 * If you fail to include this option in your config, your plugin will be <b>REJECTED</b> when you attempt to submit it to dev.bukkit.org.
 * <p/>
 * An example of a good configuration option would be something similar to 'auto-update: true' - if this value is set to false you may NOT run the auto-updater.
 * <br>
 * If you are unsure about these rules, please read the plugin submission guidelines:
 * <a href="http://wiki.bukkit.org/BukkitDev:Project_Submission_Guidelines">http://wiki.bukkit.org/BukkitDev:Project_Submission_Guidelines</a>
 * <p/>
 * (PluginUpdater is a non-blocking fork by AnorZaken from Gravitys net.gravitydevelopment.updater v2.0)
 * <p/>
 * <b>How to use most easily:</b><br>
 * Put this one line into your plugins {@code onEnable()} (Don't forget the ability to toggle it off as detailed above):
 * <code>PluginUpdater.easyUpdate(this, getFile(), id_number, EasyMode.Check_Only, getServer().getConsoleSender());</code>
 * <p/>
 * {@code this} is the {@code Plugin}, {@code id_number} is your plugins course-id. (Same as from {@code Updater}2.0).
 * <p/>
 * This one line will do a non-blocking update check and will print out the result.
 * (Instead of the {@code ConsoleSender} the last parameter can be one or many {@code Players}, or any combination of {@code CommandSender}s, for easy use with commands!)
 *
 * @see org.bukkit.command.CommandSender
 *
 * @author AnorZaken
 * @version 1.3.1
 */

public class PluginUpdater
{
	final static String VERSION = "1.3.1";
	
	final static String USER_AGENT = "PluginUpdater/v" + VERSION + " (by AnorZaken)";
	// User-Agent: {Application Name}/v{version} (by {author})
	// http://wiki.bukkit.org/ServerMods_API
	
	final static String MSG_PREFIX_FAIL = ChatColor.RED + "#PU# ";
	final static String MSG_PREFIX_SUCCESS = ChatColor.DARK_GREEN + "#PU# ";
	final static String MSG_PREFIX_AVAILABLE = ChatColor.DARK_AQUA + "#PU# ";
	final static String MSG_PREFIX_MIXEDRESULT = ChatColor.GOLD + "#PU# ";
	final static ChatColor COLOR_TEXT 	= ChatColor.YELLOW;
	final static ChatColor COLOR_PLUGIN = ChatColor.GOLD;
	final static ChatColor COLOR_BUKKIT = ChatColor.GOLD;
	final static ChatColor COLOR_URL 	= ChatColor.DARK_AQUA;
	
	final static String MSGF_PLUGIN_NOT_ENABLED = "#PU# Wo-ha!\n"
			+ "#PU# A plugin just tried to use the PluginUpdater when\n"
			+ "#PU# the plugin isn't enabled! This usually happens if\n"
			+ "#PU# the plugin attempted to use the PluginUpdater before\n"
			+ "#PU# the server has finished basic plugin initialization,\n"
			+ "#PU# (Usually caused by using PluginUpdater for a field\n"
			+ "#PU# initialization). Trying to use a plugin that isnt\n"
			+ "#PU# enabled can cause many of the plugin functions (e.g.\n"
			+ "#PU# plugin.getName() or getServer()) to throw unexpectedly\n"
			+ "#PU# on internal bukkit functions! To avoid any headaches\n"
			+ "#PU# from these non-obvious errors using the PluginUpdater\n"
			+ "#PU# like this is not allowed - it will cause this warning\n"
			+ "#PU# to be displayed and the requested action to fail.\n"
			+ "#PU# (To correct this the plugin developer has to make sure\n"
			+ "#PU# the PluginUpdater isn't used before the plugin is\n"
			+ "#PU# getting enabled - tip: call it from inside onEnable.)\n"
			+ "#PU# This problem was caused by:\n"
			+ "#PU# \"%s\"\n" //Insert plugin.getClass().getName() here
			+ "#PU# Please report this problem to the author of that plugin.";
	
	
	/**
     * Enum that describes for the plugin developer the result of a requested update activity or the {@code PluginUpdater}s current state.
     */
    public enum UpdaterStatus {
    	/**
    	 * The updater hasn't been ordered to do any work yet. This is the status of a newly created {@code PluginUpdater} instance.
    	 */
    	READY,
    	/**
    	 * The updaters worker thread has not finished yet. Please wait...
    	 */
    	PLEASE_WAIT,
    	/**
         * The was successfully in looking up and storing information about the latest version, but no version check or download has been performed.
         */
        LOOKUP_SUCCESS,
    	/**
         * The updater found an update.
         */
        UPDATE_AVAILABLE,
        /**
         * The updater found an update, and has readied it to be loaded the next time the server restarts/reloads.
         */
        UPDATE_DOWNLOADED,
        /**
         * The updater did not find an update, and nothing was downloaded.
         */
        NO_UPDATE,
        /**
         * The current version has a name tag that signals it shouldn't auto-update.
         */
        SPECIAL_TAG,
        /**
         * The server administrator has disabled the updating system
         */
        DISABLED,
        /**
         * For some reason, the updater was unable to contact dev.bukkit.org.
         */
        FAIL_CONNECTION,
        /**
         * The updater found an update, but was unable to download it.
         */
        FAIL_DOWNLOAD,
        /**
         * When running the version check, the name of the download on DBO did not contain a version in the format 'vVersion' such as 'v1.0'.
         */
        FAIL_NOVERSION,
        /**
         * The id provided by the {@code Plugin} running the updater was invalid and doesn't exist on DBO.
         */
        FAIL_BADID,
        /**
         * The server administrator has improperly configured their API key in the configuration
         */
        FAIL_APIKEY,
    }
    
    
    /**
     * Enum that allows the plugin developer to get information about what phase the worker is at.
     * <p/>
     * IMPORTANT:<br>
     * Note that the Worker will ALWAYS progress through all of these phases unless some condition is encountered that causes the worker to terminate.
     * Example: Even if the worker is told not to do a download it will still enter the download phase (where it will decide not to download anything),
     * unless it was also told to do a version check and the version check failed - then it will terminate before it reaches the download phase.
     */
    public enum WorkerPhase
	{
    	/**
    	 * The worker is in the early setup phase, reading the config as well as some early checks and initializations are performed here.
    	 */
		INIT(0),
		/**
		 * The worker has entered the phase where it will handle lookups. (Handle can mean skipping it entirely.)
		 */
		LOOKUP(1),
		/**
		 * The worker has entered the phase where it will handle version checking. (Handle can mean skipping it entirely.)
		 */
		CHECK(2),
		/**
		 * The worker has entered the phase where it will handle downloading. (Handle can mean skipping it entirely.)
		 */
		DOWNLOAD(3),
		/**
		 * The {@code PluginUpdater} has no worker thread doing any work at the moment.
		 */
		IDLE(4),
		
		; //-------
		
		final int order; //Numbered in the order they are progressed through! (Used for faster/simpler logic when in-progress worker changes are requested.)
		WorkerPhase(int order)
		{
			this.order = order;
		}
	}
    
    
    // -------------------------------------
    
    //The PluginUpdater uses this to prevent issues like several instances downloading the same file at once - important!
    private static final Map<String, WeakReference<PluginUpdater>> sPUInstances = new HashMap<String, WeakReference<PluginUpdater>>(); //String is the plugin name
    
    // If the version number contains one of these, don't update.
 	private static final String[] NO_UPDATE_TAG = { "-DEV", "-PRE", "-SNAPSHOT" };
    
 	
 	/**
     * Evaluate whether the version number is marked showing that it should not be updated normally
     */
    private static boolean hasTag(String version)
    {
        for (final String string : PluginUpdater.NO_UPDATE_TAG)
        {
            if (version.contains(string))
                return true;
        }
        return false;
    }
    
    
    /**
     * Helper function that returns the full plugin name, color-coded (or an empty string if {@code plugin == null} or {@code plugin.isEnabled() == false}).
     */
    private static String pluginString(Plugin plugin)
    {
    	if(plugin != null && plugin.isEnabled()) //If the plugin isn't enabled there is risk that .getDescription() will throw!
    		return COLOR_PLUGIN + plugin.getDescription().getFullName();
    	else
    		return "";
    }
    
    
    /**
     * An enum for the different responses available from {@code checkVersion()}.
     */
    public enum CheckVersion
    {
    	/**
    	 * At least one required parameter was {@code null}.
    	 */
    	FAIL_NULL,
    	/**
    	 * {@code updaterResult.hasAllValues()} returned {@code false}, which means it's probably the {@code UpdateResult} of a failed lookup.
    	 */
    	FAIL_UR_VALUES,
    	/**
    	 * The version number of the existing plugin version contains one of the {@code NO_UPDATE_TAG} tags that signals it shouldn't get auto updated.
    	 * <br>
    	 * (Force a no-check download to download anyway.)
    	 */
    	SPECIAL_TAG,
    	/**
    	 * The existing plugin version is the same as the version described in the {@code UpdaterResult}-object: No update required. 
    	 */
    	SAME_VERSION,
    	/**
    	 * The existing plugin version and the version described in the {@code UpdaterResult}-object are different - this is the only response that will "pass" a version-check.
    	 */
    	NOT_SAME_VERSION,
    }
    
    
    /**
     * Does a version check, the existing running version against the version described in {@code updaterResult}, and returns the result.
     *  
     * @param plugin 		The running {@code Plugin} to compare against.
     * @param updaterResult An {@code UpdaterResult} object describing the other version to compare against.
     * @return 				A {@code CheckVersion} enum value describing the result of the version check.
     * @see CheckVersion
     */
    public static CheckVersion checkVersion(Plugin plugin, UpdaterResult updaterResult)
    {
    	if (plugin == null || updaterResult == null)
    		return CheckVersion.FAIL_NULL;
    	else if (updaterResult.hasAllValues())
    		// Not all fields of updaterResult are populated
    		return CheckVersion.FAIL_UR_VALUES;
        final String localVersion = plugin.getDescription().getVersion();
        if (PluginUpdater.hasTag(localVersion))
        	// Local version has a name tag that signals it shouldn't auto-update
        	return CheckVersion.SPECIAL_TAG;
        else if (localVersion.equalsIgnoreCase(updaterResult.getVersion()))
            // We already have this version
        	return CheckVersion.SAME_VERSION;
        else
        	return CheckVersion.NOT_SAME_VERSION;
    }
    
    // -------------------------------------
    // -------------------------------------
    
    //TODO: all the nice static updater functions...
    
    /**
     * Get the {@code PluginUpdater} instance associated with {@code plugin}. 
     * If no instance exists it will be created.
     * <p/>
     * Note that {@code PluginUpdater} only stores weak references, so unless you store a
     * reference to the returned {@code PluginUpdater} instance in your class the {@code getUpdater()} function
     * is likely to (but not guaranteed to) return a new {@code PluginUpdater} instance on every call.
     * When this happens {@code PluginUpdater} will output an Info-message in the server log.
     * <p/>
     * The purpose of this function and making the {@code PluginUpdater} constructor private is to
     * prevent plugins from accidentally running two {@code PluginUpdater} instances for the same
     * plugin simultaneously. Doing so could cause unexpected update problems as well as incur
     * completely unnecessary DBO server load.
     * 
     * @param plugin 	The {@code Plugin} that is checking for an update.
     * @return 			A newly created or existing {@code PluginUpdater} instance - or {@code null} if {@code plugin.isEnabled() == false}.
     * @throws NullArgumentException If {@code plugin == null}.
     */
    public static PluginUpdater getUpdater(final Plugin plugin)
    {
//    	if(plugin == null)
//    		return null;
    	
    	if(plugin == null)
    	{
    		Bukkit.getServer().getLogger().warning("#PU# getUpdater() was called with a null plugin reference! A plugin on the server is not using PluginUpdater correctly!");
    		throw new NullArgumentException("plugin");
    		//return null;
    	}
    	else if(!plugin.isEnabled())
    	{
    		//NOTE: using plugin.getServer().getLogger() here can cause exactly the kind of error we are trying to prevent! 
    		Bukkit.getServer().getLogger().warning(String.format(MSGF_PLUGIN_NOT_ENABLED, plugin.getClass().getName()));
    		return null;
    	}
    	
    	String name = plugin.getName();
    	WeakReference<PluginUpdater> wrpu = sPUInstances.get(name);
    	PluginUpdater pu;
    	
    	if(wrpu == null)
    	{
    		//This is the first time getUpdater() is called for this plugin.
    		pu = new PluginUpdater();
    		sPUInstances.put(name, new WeakReference<PluginUpdater>(pu));
    	}
    	else
    	{
    		pu = wrpu.get();
    		if(pu == null)
    		{
//    			//This is not the first time getUpdater() is called for this plugin, but the plugin seems to have lost it's PluginUpdater reference!
//    			//This is not good code behavior - output a warning to the server-log.
//    			final String MSGF_REFLOST =
//    					"PluginUpdater has detected that \"%s\" has lost its PluginUpdater reference. " +
//    					"This is either because the plugin is not using PluginUpdater in the recommended way, " +
//    					"or the plugin has been reloaded since its last use of PluginUpdater in which case you can ignore this warning.";
//    			plugin.getLogger().warning(String.format(MSGF_REFLOST, name));
    			//^After a bit of thinking I now believe this is actually harmless, so this warning was replaced with an info-message.
    			final String MSGF_REFLOST = 
    				"PluginUpdater has detected that \"%s\" has lost its PluginUpdater reference - creating new PluginUpdater instance (old status lost).";
    			plugin.getLogger().info(String.format(MSGF_REFLOST, name));
    			pu = new PluginUpdater();
        		sPUInstances.put(name, new WeakReference<PluginUpdater>(pu));
    		}
    	}
    	
    	return pu;
    }
    
    
    /**
     * The type of update actions to perform (for use with {@code easyUpdate()})
     */
    public enum EasyMode
    {
    	/**
    	 * Check if a new version exists, and if it does attempt to download it.
    	 */
    	Check_Download,
    	/**
    	 * Check if a new version exist, but don't download any updates.
    	 */
    	Check_Only,
    	/**
    	 * Download the latest version, just do it! (Skips the version checking.)
    	 */
    	Download_NoCheck,
    }
    
    
    /**
     * The easiest way to handle plugin updating to date! 
     * <p/>
     * Handles the complete update process.<br>This includes all message notification to the specified targets:
     * <p/>
     * Did the requested update action start successfully?
     * <br>Yes, mixed success, or no. If 'mixed success', what exactly happened? If no, why not?
     * <p/>
     * Did the requested update action complete successfully?
     * <br>If yes, what was the result? If no, why not?
     * 
     * @param plugin           The {@code Plugin} to perform update actions on.
     * @param file             The jar-file of {@code plugin}. Can be retrieved with the plugins (protected) {@code getFile()} function.
     * @param id               The bukkit project id of the plugin. See <a href="http://wiki.bukkit.org/ServerMods_API#Searching_for_project_IDs">this wiki article</a>.
     * @param mode             The type of update action to perform (Check and/or Download).
     * @param announceTargets  An object that describes who to notify (with the above mentioned notifications).
     * @param announceTweak    An object that can contain optional settings for {@code PluginUpdater}.
     * 
     * @return                 The {@code PluginUpdater} instance used to perform the requested update actions, or null if {@code plugin.isEnabled() == false}.
     * @throws NullArgumentException - if {@code plugin} or {@code file} is {@code null}.
     * @see EasyMode
     * @see IAnnounceTargets
     * @see IAnnounceTweak
     */
    public static PluginUpdater easyUpdate(final Plugin plugin, final File file, final int id, final EasyMode mode, final IAnnounceTargets announceTargets, final IAnnounceTweak announceTweak)
    {
    	if(plugin == null)
    	{
    		Bukkit.getServer().getLogger().warning("#PU# easyUpdate() was called with a null plugin reference! A plugin on the server is not using PluginUpdater correctly!");
    		throw new NullArgumentException("plugin");
    		//return null;
    	}
    	else if(file == null)
    	{
    		plugin.getServer().getLogger().warning("#PU# easyUpdate() was called with a null file reference from plugin \""+plugin.getDescription().getFullName()+"\"! Please contact the plugin author about this!");
    		throw new NullArgumentException("file");
    		//return null;
    	}
    	
    	final PluginUpdater upd = getUpdater(plugin);
    	if(upd == null)
    		return null;
    	
    	TryResponse tryResult = upd.tryUpdate(plugin, file, id, true, mode!=EasyMode.Download_NoCheck, mode!=EasyMode.Check_Only, true,
    			new Runnable(){
		    		@Override
		    		public void run() {
		    			announceResult(plugin, upd.mResult, announceTargets, announceTweak);
		    		}
    			}
    		);
    	
    	announceTryResponse(plugin, tryResult, announceTargets);
    	
    	return upd;
    }
    

    //TODO: javadoc
    public static PluginUpdater easyUpdate(final Plugin plugin, final File file, final int id, final EasyMode mode, final IAnnounceTargets announceTargets)
    {
    	return easyUpdate(plugin, file, id, mode, announceTargets, null);
    }
    
    //TODO: javadoc
    public static PluginUpdater easyUpdate(final Plugin plugin, final File file, final int id, final EasyMode mode, final CommandSender... senders)
    {
    	return easyUpdate(plugin, file, id, mode, new AnnounceTargets(senders), null);
    }
    
    //TODO: javadoc
    public static PluginUpdater easyUpdate(final Plugin plugin, final File file, final int id, final EasyMode mode, final IAnnounceTweak announceTweak, final CommandSender... senders)
    {
    	return easyUpdate(plugin, file, id, mode, new AnnounceTargets(senders), announceTweak);
    }
    
    
    //TODO: javadoc
    public static void announceTryResponse(Plugin plugin, TryResponse response, IAnnounceTargets targets)
    {
//    	if(plugin == null || response == null)
//    		return;
    	
    	if(plugin == null)
    	{
    		Bukkit.getServer().getLogger().warning("#PU# announceTryResponse() was called with a null plugin reference! A plugin on the server is not using PluginUpdater correctly!");
    		throw new NullArgumentException("plugin");
    		//return;
    	}
    	else if(response == null)
    	{
    		plugin.getServer().getLogger().warning("#PU# announceTryResponse() was called with a null response reference from plugin \""+plugin.getDescription().getFullName()+"\"! Please contact the plugin author about this!");
    		throw new NullArgumentException("response");
    		//return null;
    	}
    	
    	Server server = plugin.getServer();
    	
    	final String msg;
    	
    	switch(response.result)
    	{
    	default:
    		return;
    	case FAIL_BUSY:
    		msg = MSG_PREFIX_MIXEDRESULT + COLOR_TEXT + "Couldn't perform requested update task for "+ pluginString(plugin) + COLOR_TEXT + ".\n"
    			+ MSG_PREFIX_MIXEDRESULT + COLOR_TEXT + "Other task already in progress for that plugin. Try again later.";
    		break;
    	case FAIL_PARAMETERS:
    		List<String> a = plugin.getDescription().getAuthors();
    		msg = MSG_PREFIX_FAIL + COLOR_TEXT + "Couldn't perform requested update task for "+ pluginString(plugin) + COLOR_TEXT + ".\n"
    			+ MSG_PREFIX_FAIL + COLOR_TEXT + "Cause: Received incorrect parameters.\n"
    			+ MSG_PREFIX_FAIL + COLOR_TEXT + "Please contact the plugin author about this issue."
    			+ ((a == null || a.isEmpty())?"":("\n"+MSG_PREFIX_FAIL + COLOR_TEXT + "Author: " + COLOR_BUKKIT + a.get(0)));
    		break;
    	case FAIL_UR_VALUES:
    		msg = MSG_PREFIX_FAIL + COLOR_TEXT + "Couldn't perform requested update task for "+ pluginString(plugin) + COLOR_TEXT + ".\n"
    			+ MSG_PREFIX_FAIL + COLOR_TEXT + "Supplied / contained UpdateResult object was not suitable.\n"
    			+ MSG_PREFIX_FAIL + COLOR_TEXT + "There can be multiple causes... Recommended: Try again later.";
    		break;
    	case MIXED_SUCCESS:
    		msg = MSG_PREFIX_MIXEDRESULT + COLOR_TEXT + "Mixed success initiating the requested task for "+ pluginString(plugin) + COLOR_TEXT + ".\n"
    			+ MSG_PREFIX_MIXEDRESULT + COLOR_TEXT + "Requested: "+response.flags_requested.toString() + " , In progress: "+response.flags_after.toString();
    		break;
    	case SUCCESS_INPROGRESS:
    		msg = MSG_PREFIX_SUCCESS + COLOR_TEXT + "Requested " + response.flags_requested.toString() + " for "+ pluginString(plugin) + COLOR_TEXT +" already in progress...";
    		break;
    	case SUCCESS_STARTED:
    		msg = MSG_PREFIX_SUCCESS + COLOR_TEXT + response.flags_requested.toString() +" started for "+ pluginString(plugin) + COLOR_TEXT +"...";
    		break;
    	}
    	
    	if(targets == null)
			server.getConsoleSender().sendMessage(msg);
		else
		{
			if(targets.tellConsole())
				server.getConsoleSender().sendMessage(msg);
			String[] pNames = targets.tellPlayers();
			if(pNames != null){
				for(String pName : pNames){
					Player p = server.getPlayerExact(pName);
					if(p != null && p.isOnline())
						p.sendMessage(msg);
				}
			}
		}
    }
    
    
    //TODO: javadoc
    public static void announceResult(Plugin plugin, UpdaterResult result, IAnnounceTargets targets, IAnnounceTweak settings)
    {
//    	if(plugin == null || result == null)
//    		return;
    	
    	if(plugin == null)
    	{
    		Bukkit.getServer().getLogger().warning("#PU# announceResult() was called with a null plugin reference! A plugin on the server is not using PluginUpdater correctly!");
    		throw new NullArgumentException("plugin");
    		//return;
    	}
    	else if(result == null)
    	{
    		plugin.getServer().getLogger().warning("#PU# announceResult() was called with a null result reference from plugin \""+plugin.getDescription().getFullName()+"\"! Please contact the plugin author about this!");
    		throw new NullArgumentException("result");
    		//return null;
    	}
    	
    	Server server = plugin.getServer();
    	
    	final String msg;
    	
    	switch(result.getStatus())
    	{
    	default:
    		return;
    	case DISABLED:
    		msg = MSG_PREFIX_FAIL + COLOR_TEXT + "Updating is globally disabled - (Update request ignored)";
    		break;
    	case FAIL_APIKEY:
    		msg = MSG_PREFIX_FAIL + COLOR_TEXT + "Requested update task couldn't be performed because the bukkit API key specified in the updaters config is incorrect.";
    		break;
    	case FAIL_BADID:
    		List<String> a = plugin.getDescription().getAuthors();
    		msg = MSG_PREFIX_FAIL + COLOR_TEXT + "Requested update task couldn't be performed due to incorrect plugin id.\n"
    			+ MSG_PREFIX_FAIL + COLOR_TEXT + "Please ask the author of " + pluginString(plugin) + COLOR_TEXT + " to correct this."
    			+ ((a == null || a.isEmpty())?"":("\n"+MSG_PREFIX_FAIL + COLOR_TEXT + "Author: " + COLOR_BUKKIT + a.get(0)));
    		break;
    	case FAIL_CONNECTION:
    		msg = MSG_PREFIX_FAIL + COLOR_TEXT + "Failed to connect to bukkit servers!\n"
    			+ MSG_PREFIX_FAIL + COLOR_TEXT + "(Cancelled update action for " + pluginString(plugin) + COLOR_TEXT + ".)";
    		break;
    	case FAIL_DOWNLOAD:
    		msg = MSG_PREFIX_FAIL + COLOR_TEXT + "Failed to download " + COLOR_PLUGIN + result.getName() + COLOR_TEXT + " (or failed to unpack). Try again later...";
    		break;
    	case FAIL_NOVERSION:
    		msg = MSG_PREFIX_FAIL + COLOR_TEXT + "Filename on bukkit lacks proper version description: \"" + COLOR_PLUGIN + result.getName() + COLOR_TEXT + "\".\n"
    			+ MSG_PREFIX_FAIL + COLOR_TEXT + "Please ask the plugin author to fix this.";
    		break;
    	case LOOKUP_SUCCESS:
    		msg = MSG_PREFIX_SUCCESS + COLOR_TEXT + "Result:\n"
        		+ MSG_PREFIX_SUCCESS + COLOR_TEXT + "Server version: " + pluginString(plugin) + COLOR_TEXT + " (on " + COLOR_BUKKIT + plugin.getServer().getBukkitVersion() + COLOR_TEXT + ")\n"
                + MSG_PREFIX_SUCCESS + COLOR_TEXT + "Bukkit version: " + COLOR_PLUGIN + result.getName() + COLOR_TEXT + " (for " + COLOR_BUKKIT + result.getGameVersion() + COLOR_TEXT + ")\n"
            	+ MSG_PREFIX_SUCCESS + COLOR_TEXT + "Download url: " + COLOR_URL + result.getLink()
            	+ ((settings == null || settings.lookupSuccessSuffix() == null)?"":("\n"+MSG_PREFIX_SUCCESS + COLOR_TEXT + settings.lookupSuccessSuffix()));
    		break;
    	case NO_UPDATE:
    		msg = MSG_PREFIX_SUCCESS + COLOR_TEXT + "Result: No update required for " + COLOR_PLUGIN + result.getName();
    		break;
    	case PLEASE_WAIT:
    		msg = MSG_PREFIX_MIXEDRESULT + COLOR_TEXT + "Busy with other tasks for " + pluginString(plugin) + COLOR_TEXT + ". Please wait...";
    		break;
    	case READY:
    		msg = MSG_PREFIX_MIXEDRESULT + COLOR_TEXT + "No update task requested yet. PluginUpdater idle.";
    		break;
    	case SPECIAL_TAG:
    		msg = MSG_PREFIX_MIXEDRESULT + COLOR_TEXT + "Result: Cancelled.\n"
    			+ MSG_PREFIX_MIXEDRESULT + COLOR_TEXT + "Server version: " + pluginString(plugin) + COLOR_TEXT + " (on " + COLOR_BUKKIT + plugin.getServer().getVersion() + COLOR_TEXT + ")\n"
    			+ MSG_PREFIX_MIXEDRESULT + COLOR_TEXT + "Server version tagged for non-update. (DEV, PRE, etc.)";
    		break;
    	case UPDATE_AVAILABLE:
    		msg = MSG_PREFIX_AVAILABLE + COLOR_TEXT + "Result:\n"
        		+ MSG_PREFIX_AVAILABLE + COLOR_TEXT + "Server version: " + pluginString(plugin) + COLOR_TEXT + " (on " + COLOR_BUKKIT + plugin.getServer().getBukkitVersion() + COLOR_TEXT + ")\n"
            	+ MSG_PREFIX_AVAILABLE + COLOR_TEXT + "Bukkit version: " + COLOR_PLUGIN + result.getName() + COLOR_TEXT + " (for " + COLOR_BUKKIT + result.getGameVersion() + COLOR_TEXT + ")\n"
        		+ MSG_PREFIX_AVAILABLE + COLOR_TEXT + "Download url: " + COLOR_URL + result.getLink()
                + ((settings == null || settings.updateAvailableSuffix() == null)?"":"\n"+MSG_PREFIX_AVAILABLE + COLOR_TEXT + settings.updateAvailableSuffix());
    		break;
    	case UPDATE_DOWNLOADED:
    		msg = MSG_PREFIX_SUCCESS + COLOR_TEXT + "Result: " + COLOR_PLUGIN + result.getName() + COLOR_TEXT + " downloaded!\n"
    			+ MSG_PREFIX_SUCCESS + COLOR_TEXT + "It will automatically install on server restart / reload.";
    		break;
    	}
    	
    	if(targets == null)
			server.getConsoleSender().sendMessage(msg);
		else
		{
			if(targets.tellConsole())
				server.getConsoleSender().sendMessage(msg);
			String[] pNames = targets.tellPlayers();
			if(pNames != null){
				for(String pName : pNames){
					Player p = server.getPlayerExact(pName);
					if(p != null && p.isOnline())
						p.sendMessage(msg);
				}
			}
		}
    }
    
    
    // -------------------------------------
    // -------------------------------------
	
    //TODO javadoc
    public interface IAnnounceTweak
    {
    	public String updateAvailableSuffix();
    	public String lookupSuccessSuffix();
    }
    
    //TODO javadoc
    public interface IAnnounceTargets
    {
    	boolean tellConsole();
    	String[] tellPlayers();
    }
    
    /**
     * The {@code PluginUpdater}s default implementation of the {@link IAnnounceTweak} interface.
     * @author AnorZaken
     * @see IAnnounceTweak
     */
    public static class AnnounceTweak implements IAnnounceTweak
    {
    	private String downSuffix;
    	private String checkSuffix;
    	
    	// ------------------
    	
    	/**
    	 * @see IAnnounceTweak.updateAvailableSuffix
    	 */
		@Override
		public String updateAvailableSuffix() {
			return downSuffix;
		}
		
		/**
		 * @see IAnnounceTweak.lookupSuccessSuffix
		 */
		@Override
		public String lookupSuccessSuffix() {
			return checkSuffix;
		}
		
		// ------------------
		
		/**
		 * Sets the message suffix appended on an {@code UPDATE_AVAILABLE} message.
		 * <br>
		 * Recommended use is for informing the user of what command to use to download the found update.
		 * @param downloadDesc The message to append.
		 * @return {@code this} object for easy chaining.
		 */
		public AnnounceTweak setAvailableSuffix(String downloadDesc){
			downSuffix = downloadDesc;
			return this;
		}
		
		/**
		 * Sets the message suffix appended on an {@code LOOKUP_SUCCESS} message.
		 * <br>
		 * Recommended use is for informing the user of what command(s) to use to (version-check /) download the found update.
		 * @param checkDesc The message to append.
		 * @return {@code this} object for easy chaining.
		 */
		public AnnounceTweak setLookupSuffix(String checkDesc){
			checkSuffix = checkDesc;
			return this;
		}
    }
    
    /**
     * The {@code PluginUpdater}s default implementation of the {@link IAnnounceTargets} interface.
     * @author AnorZaken
     * @see IAnnounceTargets
     */
    public static class AnnounceTargets implements IAnnounceTargets
    {
    	private boolean console = false;
    	private HashSet<String> players = null;
    	private HashSet<String> getPlayers()
    	{
    		if(players == null)
    			return players = new HashSet<String>();
    		else
    			return players;	
    	}
    	
    	// ------------------
    	
    	/**
    	 * @see IAnnounceTargets.tellConsole
    	 */
    	@Override
    	public boolean tellConsole(){
    		return console;
    	}
    	
    	/**
    	 * @see IAnnounceTargets.tellPlayers
    	 */
    	@Override
    	public String[] tellPlayers(){
    		return (players==null) ? null : players.toArray(new String[players.size()]);
    	}
    	
    	// ------------------
    	
    	//TODO: javadoc
    	public AnnounceTargets(CommandSender... senders)
    	{
    		for(CommandSender sender : senders)
    			addTarget(sender);
    	}
    	
    	//TODO: javadoc
    	public AnnounceTargets(String... playerNames)
    	{
    		for(String playerName : playerNames)
    			addTarget(playerName);
    	}
    	
    	// ------------------
    	
    	/**
    	 * Set whether or not messages should be outputed for the console.
    	 */
    	public void setTellConsole(boolean tellConsole){
    		console = tellConsole;
    	}
    	
    	// ------------------
    	
    	/**
    	 * Add {@code playerName} as a message target.
    	 * @param playerName Name of {@code Player} to add.
    	 * @return {@code true} if {@code playerName} was a non-empty string and this player name wasn't already a message target.
    	 */
    	public boolean addTarget(String playerName)
    	{
    		if(playerName == null || playerName.isEmpty())
    			return false;
    		else
    			return getPlayers().add(playerName);
    	}
    	
    	/**
    	 * Checks if {@code playerName} is a message target.
    	 */
    	public boolean isTarget(String playerName){
    		return getPlayers().contains(playerName);
    	}
    	
    	/**
    	 * Removes {@code playerName} from the message targets.
    	 * @param playerName Name of {@code Player} to remove.
    	 * @return {@code true} if {@code playerName} was a message target.
    	 */
    	public boolean removeTarget(String playerName){
    		return getPlayers().remove(playerName);
    	}
    	
    	// ------------------
    	
    	/**
    	 * Add {@code sender} as a message target.
    	 * @param sender The CommandSender to add.
    	 * @return {@code true} if {@code sender} was not {@code null} and this sender wasn't already a message target.
    	 * @see org.bukkit.command.CommandSender
    	 */
    	public boolean addTarget(CommandSender sender)
    	{
    		if(sender == null)
    			return false;
    		else if(sender instanceof Player)
    			return addTarget(sender.getName());
    		else if(console)
    			return false;
    		else
    			return console = true;
    	}
    	
    	//TODO: isTarget and removeTarget for CommandSender
    }
    
    // -------------------------------------
    // -------------------------------------
    
	//Result data (can contain not yet populated (null) fields...)
    private ResultImpl mResult;
    
	//Updater needs this while working (not final: nullified when no longer needed)
    private Worker mWorker = null;
    
    
    /**
     * Create a new {@code PluginUpdater} instance. It wont do any work on creation.
     */
    private PluginUpdater()
    {
    	mResult = new ResultImpl(UpdaterStatus.READY);
    }
    
    
    // -------------------------------------
    
    /**
     * An enum for the different responses available from {@code tryUpdate()}, {@code tryLookup()}, and {@code tryDownload()}.
     */
    public enum TryResult
    {
    	/**
    	 * At least one required parameter was null.
    	 * <p/>
    	 * {@code tryUpdate()} will also return this if the {@code lookup}, {@code checkVersion} and {@code download} flags are all {@code false}.
    	 */
    	FAIL_PARAMETERS,
    	/**
    	 * They worker thread is busy and has progressed into a phase where it is too late to make it behave as requested. Try again later.
    	 */
    	FAIL_BUSY,
    	/**
    	 * For {@code tryUpdate()}: Running {@code hasAllValues()} on the internal result-object returned {@code false}, which means it's probably the {@code UpdaterResult} of a failed lookup.
    	 * <p>
    	 * For {@code tryDownload()}: {@code updaterResult.hasAllValues()} returned {@code false}, which means it's probably the {@code UpdaterResult} of a failed lookup.
    	 */
    	FAIL_UR_VALUES,
    	/**
    	 * The requested update action started successfully.
    	 */
    	SUCCESS_STARTED,
    	/**
    	 * They worker thread was busy but its flags has been successfully changed to match, or already matched, the flags requested.
    	 */
    	SUCCESS_INPROGRESS,
    	/**
    	 * If runnables where specified this result is returned if it was too late to add the requested runnables,
    	 * but requesting the update task itself was a (perhaps partial) success, or the same (or a similar) task was already in progress.
    	 * <p/>
    	 * For {@code tryUpdate()} this can also mean that the worker thread is busy and only some of the requested flags could be set
    	 * <br>
    	 * Compare {@code TryResponse.flags_requested} with {@code TryResponse.flags_after} to find out what happened.
    	 */
    	MIXED_SUCCESS,
    }
    
    //TODO javadoc
    public enum UpdateFlags
	{
    	None 				(false,false,false),
    	Lookup 				(true,false,false),
    	LookupCheck 		(true,true,false),
    	LookupDownload 		(true,false,true),
    	LookupCheckDownload (true,true,true),
    	Check 				(false,true,false),
    	CheckDownload 		(false,true,true),
    	Download 			(false,false,true),
    	;
    	public final boolean lookup;
		public final boolean check;
		public final boolean download;
		
		/**
		 * Returns the {@code UpdateFlags} enum that corresponds to the chosen flag values.
		 */
		public static UpdateFlags getFlag(final boolean lookup, final boolean check, final boolean download)
		{
			if(lookup)
			{
				if(check)
					return download?LookupCheckDownload:LookupCheck;
				else
					return download?LookupDownload:Lookup;
			}
			else
			{
				if(check)
					return download?CheckDownload:Check;
				else
					return download?Download:None;
			}
		}
		
		private UpdateFlags(final boolean lookup, final boolean check, final boolean download)
		{
			this.lookup = lookup; this.check = check; this.download = download;
		}
	}
    
    /**
     * Contains the data returned from the {@code tryUpdate()} function.
     * @author AnorZaken
     */
    public class TryResponse
    {
    	/**
    	 * The result of trying to request a set of update actions. (Not the result of the update actions themselves!) 
    	 * @see TryResult
    	 */
    	public final TryResult result;
    	
    	/**
    	 * The update actions that was requested.
    	 * @see UpdateFlags
    	 */
    	public final UpdateFlags flags_requested;
    	
    	/**
    	 * The update flags that was granted / is in progress.
    	 * @see UpdateFlags
    	 */
    	public final UpdateFlags flags_after;
    	
    	/**
    	 * Whether or not any {@code Runnable}s where queued.
    	 */
    	public final boolean runnablesQueued;
    	
    	private TryResponse(final TryResult result, final UpdateFlags flags_requested, final UpdateFlags flags_after, final boolean runnablesQueued)
    	{
    		this.result = result;
    		this.flags_requested = flags_requested;
    		this.flags_after = flags_after;
    		this.runnablesQueued = runnablesQueued;
    	}
    }
    
  //TODO javadoc: return
    /**
     * Performs the requested update actions.
     * <br>
     * Can optionally run one or several {@code Runnable} objects as specified by the {@code runAfter} parameter once the worker-thread has completed.
     * These will be run by the main server thread.
     * <p/>
     * <b>NOTE1:</b> If this method succeeds in starting the requested update task(s) the Runnable(s) will be run once the worker thread finishes,
     * <i>even if</i> the worker thread failed to complete the assigned update task(s)!
     * In other words the only time the Runnable(s) wont run is if this method fails to start the requested task.
     * <br>
     * <b>NOTE2:</b> If several Runnables are specified there is no guarantee that they will run in the specified order!
     * 
     * @param plugin 	The {@code Plugin} that is checking for an update.
     * @param file 		The file that the {@code Plugin} is running from, get this by doing {@code this.getFile()} from within your {@code Plugin} class.
     * @param id 		The dev.bukkit.org id of the plugin project.
     * @param uFlags 	{@code UpdateFlags} enum describing the desired update actions.
     * @param queueRunnablesOnMixedSuccess
     * 					If this is {@code true} then will attempt to add the {@code runAfter} Runnables even if there was {@code Mixed_Success}.
     * @param runAfter 	Runnables to run (in the main server thread) once the worker thread finishes. (If this method fails they will not be run.)
     * 
     * @see TryResponse
     */
    public TryResponse tryUpdate(final Plugin plugin, final File file, final int id, final UpdateFlags uFlags,
    		final boolean queueRunnablesOnMixedSuccess, final Runnable... runAfter)
    {
    	return tryUpdate(plugin, file, id, uFlags.lookup, uFlags.check, uFlags.download, queueRunnablesOnMixedSuccess, runAfter);
    }
    
  //TODO javadoc: return
    // The one thing that annoys me about this function is that 99% of the time it will most likely report SUCCESS_STARTED - the most simple case!
    // All that other complicated logic is to handle the other 1%. But it's either that or 1% crappy behavior, unless I can figure out a better way...
    /**
     * Performs the requested update actions.
     * <br>
     * Can optionally run one or several {@code Runnable} objects as specified by the {@code runAfter} parameter once the worker-thread has completed.
     * These will be run by the main server thread.
     * <p/>
     * <b>NOTE1:</b> If this method succeeds in starting the requested update task(s) the Runnable(s) will be run once the worker thread finishes,
     * <i>even if</i> the worker thread failed to complete the assigned update task(s)!
     * In other words the only time the Runnable(s) wont run is if this method fails to start the requested task.
     * <br>
     * <b>NOTE2:</b> If several Runnables are specified there is no guarantee that they will run in the specified order!
     * 
     * @param plugin 		The {@code Plugin} that is checking for an update.
     * @param file 			The file that the {@code Plugin} is running from, get this by doing {@code this.getFile()} from within your {@code Plugin} class.
     * @param id 			The dev.bukkit.org id of the plugin project
     * @param lookup 		Will do a lookup if this is {@code true}, or if running {@code updaterResult.hasAllValues()} on the internal {@code UpdaterResult} object returns {@code false}.
     * @param checkVersion 	Will do a version check if this is {@code true}.
     * @param download 		Will do a download if this is {@code true} (unless {@code checkVersion} is {@code true} and the version check says otherwise).
     * @param queueRunnablesOnMixedSuccess
     * 						If this is {@code true} then will attempt to add the {@code runAfter} Runnables even if there was {@code Mixed_Success}.
     * @param runAfter 		Runnables to run (in the main server thread) once the worker thread finishes. (If this method fails they will not be run.)
     * 
     * @see TryResponse
     */
    public TryResponse tryUpdate(final Plugin plugin, final File file, final int id, boolean lookup, final boolean checkVersion, final boolean download,
    		final boolean queueRunnablesOnMixedSuccess, final Runnable... runAfter)
    {
    	final UpdateFlags flagsR = UpdateFlags.getFlag(lookup, checkVersion, download);
    	final UpdateFlags flagsA;
    	final TryResult result;
    	final boolean runnablesQueued;
    	
    	Worker w = mWorker;
    	
    	if(plugin == null || file == null || flagsR == UpdateFlags.None)
    	{
    		flagsA = w == null? UpdateFlags.None: UpdateFlags.getFlag(w.lookup, w.check, w.download);
    		runnablesQueued = false;
    		result = TryResult.FAIL_PARAMETERS;
    	}
    	else
    	{
	    	if(!lookup && !mResult.hasAllValues())
	    		lookup = true;
	    	
	    	if(w == null) //The 99%
	    	{
	    		mResult.status = UpdaterStatus.PLEASE_WAIT;
	    		w = new Worker(plugin, file, id, mResult, lookup, checkVersion, download, runAfter);
	    		flagsA = UpdateFlags.getFlag(lookup, checkVersion, download);
	    		runnablesQueued = (runAfter == null)? false : true;
	    		result = TryResult.SUCCESS_STARTED;
	    	}
	    	else //The 1%
	    	{
	    		if(lookup == w.lookup)
    			{
    				if(download)
	    			{
	    				if(checkVersion)
	    				{
	    					if(w.trySetCheck(true))
	    					{
	    						if(w.trySetDownload(true))
	    						{
	    							// Got the flags we wanted! 
	    							flagsA = flagsR;
	    							if(runAfter == null)
	    							{
	    								runnablesQueued = false;
	    								result = TryResult.SUCCESS_INPROGRESS;
	    							}
	    							else if(tryAddRunnables(runAfter))
	    							{
	    								runnablesQueued = true;
	    								result = TryResult.SUCCESS_INPROGRESS;
	    							}
	    							else
	    							{
	    								runnablesQueued = false;
	    								result = TryResult.MIXED_SUCCESS;
	    							}
	    						}
	    						else
	    						{
	    							// Download denied...
	    							flagsA = UpdateFlags.getFlag(lookup, true, false);
	    							result = TryResult.FAIL_BUSY;
	    							runnablesQueued = false;
	    						}
	    					}
	    					else
	    					{
	    						if(w.download)
	    						{
	    							// Wanted a checked download, got a no-check download.
	    							flagsA = UpdateFlags.getFlag(lookup, false, true);
	    							result = TryResult.MIXED_SUCCESS;
	    							runnablesQueued = (runAfter != null && queueRunnablesOnMixedSuccess) ? tryAddRunnables(runAfter) : false;
	    						}
	    						else
	    						{
	    							// Check denied...
	    							flagsA = UpdateFlags.getFlag(lookup, false, false);
	    							result = TryResult.FAIL_BUSY;
	    							runnablesQueued = false;
	    						}
	    					}
	    				}
	    				else //d !c
	    				{
	    					if(w.trySetDownload(true))
	    					{
	    						if(w.trySetCheck(false))
	    						{
	    							// Got the flags we wanted! 
	    							flagsA = flagsR;
	    							if(runAfter == null)
	    							{
	    								runnablesQueued = false;
	    								result = TryResult.SUCCESS_INPROGRESS;
	    							}
	    							else if(tryAddRunnables(runAfter))
	    							{
	    								runnablesQueued = true;
	    								result = TryResult.SUCCESS_INPROGRESS;
	    							}
	    							else
	    							{
	    								runnablesQueued = false;
	    								result = TryResult.MIXED_SUCCESS;
	    							}
	    						}
	    						else
	    						{
	    							if(w.phase == WorkerPhase.DOWNLOAD)
	    							{
	    								// We requested no-check download, but the worker has already done the check - and the check passed!
	    								// In other words the check didn't prevent our requested download!
	    								flagsA = UpdateFlags.getFlag(lookup, true, true);
		    							if(runAfter == null)
		    							{
		    								runnablesQueued = false;
		    								result = TryResult.SUCCESS_INPROGRESS;
		    							}
		    							else if(tryAddRunnables(runAfter))
		    							{
		    								runnablesQueued = true;
		    								result = TryResult.SUCCESS_INPROGRESS;
		    							}
		    							else
		    							{
		    								runnablesQueued = false;
		    								result = TryResult.MIXED_SUCCESS;
		    							}
	    							}
	    							else
	    							{
	    								// We requested no-check, but it was too late to change this setting.
	    								// (The check is probably in progress right now... we don't know )
		    							flagsA = UpdateFlags.getFlag(lookup, true, true);
		    							result = TryResult.MIXED_SUCCESS;
		    							runnablesQueued = (runAfter != null && queueRunnablesOnMixedSuccess) ? tryAddRunnables(runAfter) : false;
	    							}
	    						}
	    					}
	    					else
	    					{
	    						// wanted a download, failed to get it
	    						flagsA = UpdateFlags.getFlag(lookup, w.check, false);
    							result = TryResult.FAIL_BUSY;
    							runnablesQueued = false;
	    					}
	    				}
	    			}
    				else //!d
    				{
						if(checkVersion)
						{
							if(w.check)
							{
								// We wanted a check... a check + maybe a download, is in progress
								// (A download doesn't make the check unsuccessful).
								flagsA = UpdateFlags.getFlag(lookup, true, w.download);
    							if(runAfter == null)
    							{
    								runnablesQueued = false;
    								result = TryResult.SUCCESS_INPROGRESS;
    							}
    							else if(tryAddRunnables(runAfter))
    							{
    								runnablesQueued = true;
    								result = TryResult.SUCCESS_INPROGRESS;
    							}
    							else
    							{
    								runnablesQueued = false;
    								result = TryResult.MIXED_SUCCESS;
    							}
							}
							else
							{
    							if(w.download) //If someone else started a no-check download we wont cancel it just because we now want a check!
    	    					{
    								//We wanted a lookup+check... a lookup+download is in progress
    								flagsA = UpdateFlags.getFlag(lookup, false, true);
	    							result = TryResult.FAIL_BUSY;
	    							runnablesQueued = false;
    	    					}
    							else
    							{
    								if(w.trySetCheck(true))
    								{
    									//We wanted a check, no download was in progress, and we successfully added a check!
    									flagsA = UpdateFlags.getFlag(lookup, true, false);
    	    							if(runAfter == null)
    	    							{
    	    								runnablesQueued = false;
    	    								result = TryResult.SUCCESS_INPROGRESS;
    	    							}
    	    							else if(tryAddRunnables(runAfter))
    	    							{
    	    								runnablesQueued = true;
    	    								result = TryResult.SUCCESS_INPROGRESS;
    	    							}
    	    							else
    	    							{
    	    								runnablesQueued = false;
    	    								result = TryResult.MIXED_SUCCESS;
    	    							}
    								}
    								else
    								{
    									//Too late to set the check-flag, try again later.
    									flagsA = UpdateFlags.getFlag(lookup, false, false);
		    							result = TryResult.FAIL_BUSY;
		    							runnablesQueued = false;
    								}
    							}
							}
						}
						else //!d !c
						{
							// We requested a lookup (no check, no download).
							// A lookup is in progress... (and a check or a download doesn't make the lookup unsuccessful).
							// (if check and download are both false, then lookup has to be true, because they can't all be false!)
							flagsA = UpdateFlags.getFlag(true, w.check, w.download);
							if(runAfter == null)
							{
								runnablesQueued = false;
								result = TryResult.SUCCESS_INPROGRESS;
							}
							else if(tryAddRunnables(runAfter))
							{
								runnablesQueued = true;
								result = TryResult.SUCCESS_INPROGRESS;
							}
							else
							{
								runnablesQueued = false;
								result = TryResult.MIXED_SUCCESS;
							}
						}
    				}
    			}
	    		else // lookup != w.lookup:
	    		{
	    			if(lookup)
	    			{
	    				//We wanted a lookup but didn't get it...
	    				flagsA = UpdateFlags.getFlag(false, w.check, w.download);
						result = TryResult.FAIL_BUSY;
						runnablesQueued = false;
	    			}
	    			else //!l
	    			{
	    				//We didn't want a lookup but got one anyway...
	    				if(download)
	    				{
	    					if(w.download)
	    					{
	    						// We wanted a no-lookup download, we got a download with lookup...
	    						// (regardless of check-flag this will be "mixed result")
	    						flagsA = UpdateFlags.getFlag(true, w.check, true);
								result = TryResult.MIXED_SUCCESS;
								runnablesQueued = (runAfter != null && queueRunnablesOnMixedSuccess) ? tryAddRunnables(runAfter) : false;
	    					}
	    					else
	    					{
	    						// Download denied...
	    						flagsA = UpdateFlags.getFlag(true, w.check, false);
								result = TryResult.FAIL_BUSY;
								runnablesQueued = false;
	    					}
	    				}
	    				else // !l !d
	    				{
	    					if(checkVersion)
	    					{
	    						if(w.download)
	    						{
	    							if(w.check)
	    							{
	    								// We only wanted a check 
	    								flagsA = UpdateFlags.getFlag(true, true, true);
	    								result = TryResult.MIXED_SUCCESS;
	    								runnablesQueued = (runAfter != null && queueRunnablesOnMixedSuccess) ? tryAddRunnables(runAfter) : false;
	    							}
	    							else
	    							{
	    								// Check denied...
	    								flagsA = UpdateFlags.getFlag(true, false, true);
	    								result = TryResult.FAIL_BUSY;
	    								runnablesQueued = false;
	    							}
	    						}
	    						else
	    						{
	    							if(w.trySetCheck(true))
	    							{
	    								// Check granted. Got a lookup that we didn't ask for though...
	    								flagsA = UpdateFlags.getFlag(true, true, false);
	    								result = TryResult.MIXED_SUCCESS;
	    								runnablesQueued = (runAfter != null && queueRunnablesOnMixedSuccess) ? tryAddRunnables(runAfter) : false;
	    							}
	    							else
	    							{
	    								// Check denied...
	    								flagsA = UpdateFlags.getFlag(true, false, false);
	    								result = TryResult.FAIL_BUSY;
	    								runnablesQueued = false;
	    							}
	    						}
	    					}
	    					else // !l !c !d
	    					{
	    						// We can't actually get here... but complier can't see that... this is just needed to make it compile...
	    						flagsA = UpdateFlags.getFlag(w.lookup, w.check, w.download);
	    						result = TryResult.FAIL_PARAMETERS;
	    						runnablesQueued = false;
	    					}
	    				}
	    			}
	    		}
	    	} // w != null end.
    	} // parameters-check success end.
    	
    	return new TryResponse(result, flagsR, flagsA, runnablesQueued);
    }
    
  //TODO javadoc: return
    /**
     * Gets information about the latest version of {@code plugin} from DBO. No version check and no download. 
     * <p/>
     * Can optionally run one or several {@code Runnable} objects as specified by the {@code runAfter} parameter once the worker-thread has completed.
     * These will be run by the main server thread.
     * <p/>
     * <b>NOTE1:</b> If this method succeeds in starting the requested update task(s) the Runnable(s) will be run once the worker thread finishes,
     * <i>even if</i> the worker thread failed to complete the assigned update task(s)!
     * In other words the only time the Runnable(s) wont run is if this method fails to start the requested task.
     * <br>
     * <b>NOTE2:</b> If several Runnables are specified there is no guarantee that they will run in the specified order!
     * 
     * @param plugin 	The {@code Plugin} that is checking for an update.
     * @param file 		The file that the {@code Plugin} is running from, get this by doing {@code this.getFile()} from within your {@code Plugin} class.
     * @param id 		The dev.bukkit.org id of the plugin project
     * @param runAfter 	Runnables to run after the worker thread is finished. (Will run in main-thread.)
     * 
     * @see TryResult
     */
    public TryResult tryLookup(final Plugin plugin, final File file, final int id, final Runnable... runAfter)
    {
    	if(plugin == null || file == null)
    		return TryResult.FAIL_PARAMETERS;
    	
    	Worker w = mWorker;
    	if(w == null)
    	{
    		mResult.status = UpdaterStatus.PLEASE_WAIT;
    		w = new Worker(plugin, file, id, mResult, true, false, false, runAfter);
    		return TryResult.SUCCESS_STARTED;
    	}
    	else if(w.lookup)
    	{
    		if(runAfter == null || tryAddRunnables(runAfter))
    			return TryResult.SUCCESS_INPROGRESS;
    		else
    			return TryResult.MIXED_SUCCESS;
    	}
    	else
    		return TryResult.FAIL_BUSY;
    }
    
  //TODO javadoc: return
    /**
     * Tries to downloads the version described in {@code updateResult} - No version checking.
     * If {@code updateResult == null} it tries to downloads the version found during the last performed lookup.
     * <p/>
     * Can optionally run one or several {@code Runnable} objects as specified by the {@code runAfter} parameter once the worker-thread has completed.
     * These will be run by the main server thread.
     * <p/>
     * <b>NOTE1:</b> If this method succeeds in starting the requested update task(s) the Runnable(s) will be run once the worker thread finishes,
     * <i>even if</i> the worker thread failed to complete the assigned update task(s)!
     * In other words the only time the Runnable(s) wont run is if this method fails to start the requested task.
     * <br>
     * <b>NOTE2:</b> If several Runnables are specified there is no guarantee that they will run in the specified order!
     * 
     * @param plugin 		The {@code Plugin} that is checking for an update.
     * @param file 			The file that the {@code Plugin} is running from, get this by doing {@code this.getFile()} from within your {@code Plugin} class.
     * @param id 			The dev.bukkit.org id of the plugin project
     * @param updaterResult	An {@code UpdateResult} object containing information about what is to be downloaded.
     *  					Or {@code null} if we want to download the version found during the last performed lookup.
     * @param runAfter 		Runnables to run after the worker thread is finished. (Will run in main-thread.)
     * 
     * @see TryResult
     */
    public TryResult tryDownload(final Plugin plugin, final File file, final int id, final UpdaterResult updaterResult, final Runnable... runAfter)
    {
    	if(plugin == null || file == null)
    		return TryResult.FAIL_PARAMETERS;
    	else if(!(updaterResult==null?mResult:updaterResult).hasAllValues())
        	return TryResult.FAIL_UR_VALUES;	
    	
    	Worker w = mWorker;
    	if(w == null)
    	{
    		if(updaterResult != null)
    			mResult = new ResultImpl(updaterResult);
    		mResult.status = UpdaterStatus.PLEASE_WAIT;
    		w = new Worker(plugin, file, id, mResult, false, false, true, runAfter);
    		return TryResult.SUCCESS_STARTED;
    	}
    	else if((updaterResult==null?mResult:updaterResult).getLink().equals(w.updaterResult.getLink()) && w.trySetDownload(true))
    	{
    		if(runAfter == null || tryAddRunnables(runAfter))
    			return TryResult.SUCCESS_INPROGRESS;
    		else
    			return TryResult.MIXED_SUCCESS;
    	}
    	else
    		return TryResult.FAIL_BUSY;
    }
    
  //TODO javadoc: return
    /**
     * Tries to downloads the version found during the latest lookup. No version checking.
     * 
     * @param plugin 		The {@code Plugin} that is checking for an update.
     * @param file 			The file that the {@code Plugin} is running from, get this by doing {@code this.getFile()} from within your {@code Plugin} class.
     * @param id 			The dev.bukkit.org id of the plugin project
     * 
     * @see TryResult
     */
    public TryResult tryDownload(final Plugin plugin, final File file, final int id)
    {
    	if(plugin == null || file == null)
    		return TryResult.FAIL_PARAMETERS;
    	else if(!mResult.hasAllValues())
    		return TryResult.FAIL_UR_VALUES;
    	
    	Worker w = mWorker;
    	if(w == null)
    	{
    		mResult.status = UpdaterStatus.PLEASE_WAIT;
    		w = new Worker(plugin, file, id, mResult, false, false, true);
    		return TryResult.SUCCESS_STARTED;
    	}
    	else if(mResult.getLink().equals(w.updaterResult.getLink()) && w.trySetDownload(true))
    		return TryResult.SUCCESS_INPROGRESS;
    	else
    		return TryResult.FAIL_BUSY;
    }
    
    /**
     * Get the current phase of the update process, or {@code WorkerPhase.IDLE} if no work is being done at the moment.
     * <p/>
     * IMPORTANT:<br>
     * The Worker will ALWAYS progress through all update phases unless some condition is encountered that causes the worker to terminate.
     * Example: Even if the worker is told not to do a download it will still enter the download phase (where it will decide not to download anything),
     * unless it was also told to do a version check and the version check failed - then it will terminate before it reaches the download phase.
     * 
     * @see WorkerPhase
     * @return The current {@code WorkerPhase}.
     */
    public WorkerPhase getWorkerPhase()
    {
    	Worker w = mWorker;
    	if(w == null)
    		return WorkerPhase.IDLE;
    	else
    		return w.phase;
    }
    
    /**
     * Get the current {@code UpdaterStatus} of the {@code PluginUpdater}.
     * @see UpdaterStatus
     */
    public UpdaterStatus getStatus()
    {
    	return mResult.getStatus();
    }
    
    /**
     * Returns {@code true} if this {@code PluginUpdaters} worker-thread is currently doing work.
     * For more information about what it is doing use {@code getWorkerPhase()} instead.
     * 
     * @see getWorkerPhase
     * @return {@code true} if this {@code PluginUpdater}s worker thread is busy.
     */
    public boolean isBusy()
    {
    	return isBusy(mResult);
    }
    
    /**
     * Get the progress of an ongoing or completed download.
     * 
     * @return the current download progress [0-100] if download in progress/completed, otherwise 0.
     */
    public int tryGetDownloadProgress()
    {
    	int i = 0;
    	
    	Worker w = mWorker;
    	if(w != null)
    	{
    		i = w.getDownloadProgress();
    	}
    	if(getStatus() == UpdaterStatus.UPDATE_DOWNLOADED)
    		i = 100;
    	
    	return i;
    }
    
    /**
     * Get the {@code UpdaterResult} from the latest update / lookup / download,<br>or {@code null} if {@code PluginUpdater.isBusy() == true}.
     * 
     * @see UpdaterResult
     * @return a cloned {@code UpdaterResult} object or {@code null} if {@code isBusy() == true}.
     */
    public UpdaterResult tryGetResult()
    {
    	UpdaterResult ur = mResult.copy();
    	if(isBusy(ur))
    		return null;
    	else
    		return ur;
    }
    
    
    // -------------------------------------
    
    
    private boolean isBusy(UpdaterResult updateResult)
    {
    	return updateResult.getStatus() == UpdaterStatus.PLEASE_WAIT;
    }
    
    private final ReentrantLock runnableLock = new ReentrantLock();
    
    /**
     * Used by the worker thread when it is finished and want to report the outcome.
     * @param status The result from the worker thread.
     */
    private void setStatusAndRemoveWorker(UpdaterStatus status, Plugin plugin, Worker worker)
    {
    	runnableLock.lock();
    	try
    	{
	    	if(plugin != null && worker != null && worker.runnables != null)
	    	{
	    		for(Runnable r : worker.runnables)
	    		{
	    			if(r != null)
	    				plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, r);
	    		}
	    	}
	    	mWorker = null;
	    	mResult.status = status;
    	}
    	finally
    	{
    		runnableLock.unlock();
    	}
    }
    
    /**
     * Tries to queue {@code Runnable}s for the current worker thread to run on completion.
     * @param runnables Array of {@code Runnable} instances to queue on the worker.
     * @return {@code true} if {@code runnables} successfully queued or if {@code runnables.length == 0}.
     */
    private boolean tryAddRunnables(Runnable[] runnables)
    {
    	if(runnables == null || mWorker == null)
    		return false;
    	else if(runnables.length == 0)
    		return true;
    	else if(runnableLock.tryLock())
    	{
    		try
    		{
    			if(mWorker != null) //Double check that mWorker isn't null to make sure (threading)
    			{
    				if(mWorker.runnables == null || mWorker.runnables.length == 0)
    					mWorker.runnables = runnables;
    				else
    				{
    					Runnable[] combined = Arrays.copyOf(mWorker.runnables, mWorker.runnables.length + runnables.length);
    					System.arraycopy(runnables, 0, combined, mWorker.runnables.length, runnables.length);
    					mWorker.runnables = combined;
    				}
    				return true;
    			}
    			else
    				return false;
    		}
    		finally
    		{
    			runnableLock.unlock();
    		}
    	}
    	else
    		return false;
    }
    
    
    // -------------------------------------
    
    /**
     * Public interface for the object used by the {@code PluginUpdater} to store and return the result of its work.
     * @author AnorZaken
     */
    public interface UpdaterResult
    {
    	/**
    	 * Get the result of the associated requested update action, or {@code UpdaterStatus.PLEASE_WAIT} if a {@code PluginUpdater}s worker thread is busy using this object.
    	 */
    	UpdaterStatus getStatus();
    	
    	/**
    	 * Get the name of the latest version (including version number), or {@code null} if lookup not completed.
    	 */
    	String getName();
    	/**
    	 * Get the version part of the latest versions name-string, or {@code null} if lookup not completed.
    	 */
    	String getVersion();
    	/**
    	 * Get the download link to the latest version, or {@code null} if lookup not completed.
    	 */
    	String getLink();
    	/**
    	 * Get the release type (alpha/beta/release) of the latest version, or {@code null} if lookup not completed.
    	 */
    	String getReleaseType();
    	/**
    	 * Get the intended bukkit version of the latest version, or {@code null} if lookup not completed.
    	 */
    	String getGameVersion();
    	
    	/**
    	 * Checks if all get-functions can return 'appropriate' strings ({@code getStatus()} excluded).<br>
    	 * In {@code PluginUpdaters} implementation this is a {@code null} check.<br>
    	 * Basically it can be used to quickly reject {@code UpdaterResult}s that has not completed a successful lookup.
    	 */
    	boolean hasAllValues();
    }
    
    /**
     * {@code PluginUpdater}s default implementation of the {@link UpdaterResult} interface.
     * @see UpdaterResult
     * @author AnorZaken
     */
    private class ResultImpl implements UpdaterResult
    {
        private UpdaterStatus status = UpdaterStatus.READY;
    	private String versionName = null;
    	private String versionNumber = null;
    	private String versionLink = null;
    	private String versionType = null;
    	private String versionGameVersion = null;
    	
    	@Override
 		public UpdaterStatus getStatus() {
 			return status;
 		}
    	@Override
    	public String getName() {
			return versionName;
		}
    	@Override
    	public String getVersion() {
			return versionNumber;
		}
    	@Override
		public String getLink() {
			return versionLink;
		}
    	@Override
		public String getReleaseType() {
			return versionType;
		}
    	@Override
		public String getGameVersion() {
			return versionGameVersion;
		}
    	
		//Basically clone() - except clone() is an abomination... (Google java.lang.cloneable if you don't believe me)
		private ResultImpl copy()
		{
			ResultImpl uri = new ResultImpl(status);
			
			synchronized(this)
			{
				uri.status = status;
				uri.versionLink = versionLink;
				uri.versionName = versionName;
				uri.versionNumber = versionNumber;
				uri.versionType = versionType;
				uri.versionGameVersion = versionGameVersion;
			}
			
			return uri;
		}
		
		private ResultImpl(UpdaterStatus status)
		{
			this.status = status;
		}
		
		private ResultImpl(UpdaterResult updaterResult)
		{
			status = updaterResult.getStatus();
			versionLink = updaterResult.getLink();
			versionName = updaterResult.getName();
			versionNumber = updaterResult.getVersion();
			versionType = updaterResult.getReleaseType();
			versionGameVersion = updaterResult.getGameVersion();
		}
		
		@Override
		public boolean hasAllValues()
		{
			if(versionLink == null || versionName == null || versionNumber == null || versionType == null || versionGameVersion == null)
				return false;
			else
				return true;
		}
		
		// Used before a lookup is made to ensure that we don't have a mix of old and new values in case the lookup only manages to populate some of the fields
		private synchronized void resetVersionData()
		{
	    	versionName = null;
	    	versionNumber = null;
	    	versionLink = null;
	    	versionType = null;
	    	versionGameVersion = null;
		}
    }
    

    // -------------------------------------
    
    /**
     * The worker class that takes care of the actual update work. 
     * <p/>
     * This class is a modified version of Gravitys {@code Updater}2.0-class.<br>(The {@code read()}, {@code saveFile()} and {@code unzip()} methods are more or less the same).
     * 
     * @author AnorZaken
     * @author Gravity
     */
    private class Worker
    {
    	private static final int BYTE_SIZE = 1024; // Used for downloading files
    	
    	private static final String TITLE_VALUE = "name"; // Gets remote file's title
        private static final String LINK_VALUE = "downloadUrl"; // Gets remote file's download link
        private static final String TYPE_VALUE = "releaseType"; // Gets remote file's release type
        private static final String VERSION_VALUE = "gameVersion"; // Gets remote file's build version
        private static final String QUERY = "/servermods/files?projectIds="; // Path to GET
        private static final String HOST = "https://api.curseforge.com"; // Slugs will be appended to this to get to the project's RSS feed
        
    	//Updater Construtor parameters (in order):
        private final Plugin 		plugin;
        private final File 			file; // The plugin's file
        private final int 			id;
        private final ResultImpl 	updaterResult;
        private final boolean 		lookup;
        private volatile boolean 	check;
        private volatile boolean 	download;
        private volatile Runnable[] runnables;
        
        //Constructed from constructor parameters (in order):
        private final String 	updateFolder;// The folder that downloads will be placed in
        private final String 	apiKey; // BukkitDev ServerMods API key
        private final URL 		url; // Connecting to RSS
        private final Thread 	thread; // Worker thread
        
        private volatile int downloadProgress = 0;
        
        // IMPORTANT: this variable should never be written to directly - not even by the Worker-class itself - it should use the synchronized setPhase() function!
        private volatile WorkerPhase phase = WorkerPhase.INIT; //Describes how far the worker thread has progressed
        
        
        /**
         * Get the current progress of a download [0-100], or 0 if no download in progress/completed.
         */
        public int getDownloadProgress()
        {
        	return downloadProgress;
        }
        
        /**
         * Used by the Worker to change it's phases in a thread-safe way
         */
        private synchronized void setPhase(WorkerPhase phase)
        {
        	this.phase = phase;
        }
        
        /**
         * Tries to alter the workers check setting.
         * @param check the desired setting.
         * @return {@code true} if the desired setting is already active or if the worker has not yet reached that phase and the value was successfully changed. 
         */
        private synchronized boolean trySetCheck(final boolean check)
        {
        	if(this.check == check)
        		return true;
        	else if(phase.order < WorkerPhase.CHECK.order)
        	{
        		this.check = check;
        		return true;
        	}
        	else
        		return false;
        }
        
        /**
         * Tries to alter the workers download setting.
         * @param download the desired setting.
         * @return {@code true} if the desired setting is already active or if the worker has not yet reached that phase and the value was successfully changed. 
         */
        private synchronized boolean trySetDownload(final boolean download)
        {
        	if(this.download == download)
        		return true;
        	else if(phase.order < WorkerPhase.DOWNLOAD.order)
        	{
        		this.download = download;
        		return true;
        	}
        	else
        		return false;
        }
        
        // --------------------------------------------
        
        /**
         * Creates the {@code Worker} object and performs some initial quick checks.<br>If these checks pass the actual async worker thread is started.
         * Otherwise the failed result is recorded and the {@code Worker} requests a removal of itself.
         */
        private Worker(final Plugin plugin, final File file, final int id, final ResultImpl updaterResult, boolean lookup, boolean check, boolean download, final Runnable... runnables)
        {
        	this.plugin = plugin;
        	this.file = file;
        	this.id = id;
            this.updaterResult = updaterResult;
            this.lookup = lookup;
            this.check = check;
            this.download = download;
            this.runnables = runnables;
            
            this.updateFolder = plugin.getServer().getUpdateFolder();
            
            final File pluginFile = plugin.getDataFolder().getParentFile();
            final File updaterFile = new File(pluginFile, "Updater");
            final File updaterConfigFile = new File(updaterFile, "config.yml");

            if (!updaterFile.exists()) {
            	try {
            		updaterFile.mkdir();
            	} catch (final Exception e) {
            		plugin.getLogger().severe("The updater could not create Updater folder " + updaterFile.getAbsolutePath());
                    e.printStackTrace();
            	}
            }
            if (!updaterConfigFile.exists()) {
                try {
                    updaterConfigFile.createNewFile();
                } catch (final Exception e) {
                    plugin.getLogger().severe("The updater could not create a configuration in " + updaterFile.getAbsolutePath());
                    e.printStackTrace();
                }
            }
            
            final YamlConfiguration config = YamlConfiguration.loadConfiguration(updaterConfigFile);

            config.options().header("This configuration file affects all plugins using the Updater system (version 2+ - http://forums.bukkit.org/threads/96681/ )" + '\n'
                    + "If you wish to use your API key, read http://wiki.bukkit.org/ServerMods_API and place it below." + '\n'
                    + "Some updating systems will not adhere to the disabled value, but these may be turned off in their plugin's configuration.");
            config.addDefault("api-key", "PUT_API_KEY_HERE");
            config.addDefault("disable", false);

            if (config.get("api-key", null) == null) {
                config.options().copyDefaults(true);
                try {
                    config.save(updaterConfigFile);
                } catch (final IOException e) {
                    plugin.getLogger().severe("The updater could not save the configuration in " + updaterFile.getAbsolutePath());
                    e.printStackTrace();
                }
            }

            if (config.getBoolean("disable")) {
                //this.updaterResult.status = UpdaterStatus.DISABLED;
                PluginUpdater.this.setStatusAndRemoveWorker(UpdaterStatus.DISABLED, plugin, this);
                this.apiKey = null;
                this.url = null;
                this.thread = null;
                return;
            }

            String key = config.getString("api-key");
            if (key.equalsIgnoreCase("PUT_API_KEY_HERE") || key.equals("")) {
                key = null;
            }

            this.apiKey = key;

            URL u;
            try {
                u = new URL(HOST + QUERY + id);
            } catch (final MalformedURLException e) {
            	this.url = null;
            	this.thread = null;
                plugin.getLogger().severe("The project ID provided for updating, " + id + " is invalid.");
                //this.updaterResult.status = UpdaterStatus.FAIL_BADID;
                PluginUpdater.this.setStatusAndRemoveWorker(UpdaterStatus.FAIL_BADID, plugin, this);
                e.printStackTrace();
                return;
            }
            this.url = u;
            
            this.thread = new Thread(new WorkerRunnable());
            this.thread.setPriority((Thread.NORM_PRIORITY + Thread.MIN_PRIORITY) / 2); //aka "low" priority (Maybe?)
            this.thread.start();
        }
        
        // -------------------------------------------------------
        
        /**
         * Save an update from dev.bukkit.org into the server's update folder.
         */
        private void saveFile(File folder, String fileString, String urlString) {
            if (!folder.exists()) {
                folder.mkdir();
            }
            BufferedInputStream in = null;
            FileOutputStream fout = null;
            try {
                // Download the file
                final URL url = new URL(urlString);
                final int fileLength = url.openConnection().getContentLength();
                in = new BufferedInputStream(url.openStream());
                fout = new FileOutputStream(folder.getAbsolutePath() + "/" + fileString);

                final byte[] data = new byte[Worker.BYTE_SIZE];
                int count;
//                if (this.announce) { //TODO
//                    this.plugin.getLogger().info("About to download a new update: " + this.updaterInfoImpl.versionName); //Is this even thread safe?
//                }
                long downloaded = 0;
                while ((count = in.read(data, 0, Worker.BYTE_SIZE)) != -1) {
                    downloaded += count;
                    fout.write(data, 0, count);
                    final int percent = (int) ((downloaded * 100L) / fileLength);
                    downloadProgress = percent;
//                    if (this.announce && ((percent % 10) == 0)) { //TODO
//                        this.plugin.getLogger().info("Downloading update: " + percent + "% of " + fileLength + " bytes.");
//                    }
                }
                //Just a quick check to make sure we didn't leave any files from last time...
                for (final File xFile : new File(this.plugin.getDataFolder().getParent(), this.updateFolder).listFiles()) {
                    if (xFile.getName().endsWith(".zip")) {
                        xFile.delete();
                    }
                }
                // Check to see if it's a zip file, if it is, unzip it.
                final File dFile = new File(folder.getAbsolutePath() + "/" + fileString);
                if (dFile.getName().endsWith(".zip")) {
                    // Unzip
                    this.unzip(dFile.getCanonicalPath());
                }
//                if (this.announce) { //TODO
//                    this.plugin.getLogger().info("Finished updating.");
//                }
                //this.updaterResult.status = UpdaterStatus.UPDATE_DOWNLOADED;
                PluginUpdater.this.setStatusAndRemoveWorker(UpdaterStatus.UPDATE_DOWNLOADED, plugin, this);
            } catch (final Exception ex) {
                this.plugin.getLogger().warning("The plugin-updater tried to download a new update, but was unsuccessful.");
                //this.updaterResult.status = UpdaterStatus.FAIL_DOWNLOAD;
                PluginUpdater.this.setStatusAndRemoveWorker(UpdaterStatus.FAIL_DOWNLOAD, plugin, this);
            } finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                    if (fout != null) {
                        fout.close();
                    }
                } catch (final Exception ex) { }
            }
        }

        /**
         * Part of Zip-File-Extractor, modified by Gravity for use with Bukkit
         */
        private void unzip(String file) {
            try {
                final File fSourceZip = new File(file);
                final String zipPath = file.substring(0, file.length() - 4);
                ZipFile zipFile = new ZipFile(fSourceZip);
                Enumeration<? extends ZipEntry> e = zipFile.entries();
                while (e.hasMoreElements()) {
                    ZipEntry entry = e.nextElement();
                    File destinationFilePath = new File(zipPath, entry.getName());
                    destinationFilePath.getParentFile().mkdirs();
                    if (entry.isDirectory()) {
                        continue;
                    } else {
                        final BufferedInputStream bis = new BufferedInputStream(zipFile.getInputStream(entry));
                        int b;
                        final byte buffer[] = new byte[Worker.BYTE_SIZE];
                        final FileOutputStream fos = new FileOutputStream(destinationFilePath);
                        final BufferedOutputStream bos = new BufferedOutputStream(fos, Worker.BYTE_SIZE);
                        while ((b = bis.read(buffer, 0, Worker.BYTE_SIZE)) != -1) {
                            bos.write(buffer, 0, b);
                        }
                        bos.flush();
                        bos.close();
                        bis.close();
                        final String name = destinationFilePath.getName();
                        if (name.endsWith(".jar") && this.pluginFile(name)) {
                            destinationFilePath.renameTo(new File(this.plugin.getDataFolder().getParent(), this.updateFolder + "/" + name));
                        }
                    }
                    entry = null;
                    destinationFilePath = null;
                }
                e = null;
                zipFile.close();
                zipFile = null;

                // Move any plugin data folders that were included to the right place, Bukkit won't do this for us.
                for (final File dFile : new File(zipPath).listFiles()) {
                    if (dFile.isDirectory()) {
                        if (this.pluginFile(dFile.getName())) {
                            final File oFile = new File(this.plugin.getDataFolder().getParent(), dFile.getName()); // Get current dir
                            final File[] contents = oFile.listFiles(); // List of existing files in the current dir
                            for (final File cFile : dFile.listFiles()) // Loop through all the files in the new dir
                            {
                                boolean found = false;
                                for (final File xFile : contents) // Loop through contents to see if it exists
                                {
                                    if (xFile.getName().equals(cFile.getName())) {
                                        found = true;
                                        break;
                                    }
                                }
                                if (!found) {
                                    // Move the new file into the current dir
                                    cFile.renameTo(new File(oFile.getCanonicalFile() + "/" + cFile.getName()));
                                } else {
                                    // This file already exists, so we don't need it anymore.
                                    cFile.delete();
                                }
                            }
                        }
                    }
                    dFile.delete();
                }
                new File(zipPath).delete();
                fSourceZip.delete();
            } catch (final IOException ex) {
                this.plugin.getLogger().warning("The plugin-updater tried to unzip a new update file, but was unsuccessful.");
                //this.updaterResult.status = UpdaterStatus.FAIL_DOWNLOAD;
                PluginUpdater.this.setStatusAndRemoveWorker(UpdaterStatus.FAIL_DOWNLOAD, plugin, this);
                ex.printStackTrace();
            }
            new File(file).delete();
        }

        /**
         * Check if the name of a jar is one of the plugins currently installed, used for extracting the correct files out of a zip.
         */
        private boolean pluginFile(String name) {
            for (final File file : new File("plugins").listFiles()) {
                if (file.getName().equals(name)) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Check to see if the program should continue by evaluation whether the plugin is already updated, or shouldn't be updated
         */
        private boolean versionCheck()
        {
        	if(this.updaterResult.versionNumber == null)
        	{
        		PluginUpdater.this.setStatusAndRemoveWorker(UpdaterStatus.FAIL_NOVERSION, plugin, this);
                return false;
        	}
        	
            final String localVersion = this.plugin.getDescription().getVersion();
            if (PluginUpdater.hasTag(localVersion))
        	{
            	// Local version has a name tag that signals it shouldn't auto-update
            	PluginUpdater.this.setStatusAndRemoveWorker(UpdaterStatus.SPECIAL_TAG, plugin, this);
            	return false;
        	}
            else if(localVersion.equalsIgnoreCase(this.updaterResult.versionNumber))
            {
                // We already have the latest version
            	PluginUpdater.this.setStatusAndRemoveWorker(UpdaterStatus.NO_UPDATE, plugin, this);
                return false;
            }
            else
            {
            	return true;
            }
        }

        /**
         * Open a connection to DBO and obtain information on the latest release
         */
        private boolean read() //TODO: expand to more than latest version to enable convenient downgrading
        {
        	this.updaterResult.resetVersionData();
            try
            {
                final URLConnection conn = this.url.openConnection();
                conn.setConnectTimeout(5000);

                if (this.apiKey != null) {
                    conn.addRequestProperty("X-API-Key", this.apiKey);
                }
                conn.addRequestProperty("User-Agent", USER_AGENT);

                conn.setDoOutput(true);

                final BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                final String response = reader.readLine();

                final JSONArray array = (JSONArray) JSONValue.parse(response);

                if (array.size() == 0) {
                    this.plugin.getLogger().warning("The updater could not find any files for the project id " + this.id);
                    //this.updaterResult.status = UpdaterStatus.FAIL_BADID;
                    PluginUpdater.this.setStatusAndRemoveWorker(UpdaterStatus.FAIL_BADID, plugin, this);
                    return false;
                }

                this.updaterResult.versionName = (String) ((JSONObject) array.get(array.size() - 1)).get(Worker.TITLE_VALUE);
                this.updaterResult.versionLink = (String) ((JSONObject) array.get(array.size() - 1)).get(Worker.LINK_VALUE);
                this.updaterResult.versionType = (String) ((JSONObject) array.get(array.size() - 1)).get(Worker.TYPE_VALUE);
                this.updaterResult.versionGameVersion = (String) ((JSONObject) array.get(array.size() - 1)).get(Worker.VERSION_VALUE);
                
                final String[] temp = this.updaterResult.versionName.split(" v");
                if (temp.length == 2)
                {
                    this.updaterResult.versionNumber = temp[1].split(" ")[0]; // Get the newest file's version number
                    return true;
                }
                else
                {
                	// The file's name did not contain the string 'vVersion'
                    final String authorInfo = this.plugin.getDescription().getAuthors().size() == 0 ? "" : " (" + this.plugin.getDescription().getAuthors().get(0) + ")";
                    this.plugin.getLogger().warning("The author of this plugin" + authorInfo + " has misconfigured their Plugin Update system");
                    this.plugin.getLogger().warning("File versions should follow the format 'PluginName vVERSION'");
                    this.plugin.getLogger().warning("Please notify the author of this error.");
                    //this.updaterResult.status = UpdaterStatus.FAIL_NOVERSION;
                    PluginUpdater.this.setStatusAndRemoveWorker(UpdaterStatus.FAIL_NOVERSION, plugin, this);
                    return false;
                }
            }
            catch (final IOException e)
            {
                if (e.getMessage().contains("HTTP response code: 403")) {
                    this.plugin.getLogger().warning("curseforge.com rejected the API key provided in plugins/Updater/config.yml");
                    this.plugin.getLogger().warning("Please double-check your configuration to ensure it is correct.");
                    //this.updaterResult.status = UpdaterStatus.FAIL_APIKEY;
                    PluginUpdater.this.setStatusAndRemoveWorker(UpdaterStatus.FAIL_APIKEY, plugin, this);
                } else {
                    this.plugin.getLogger().warning("The updater could not contact curseforge.com for updating.");
                    this.plugin.getLogger().warning("The site may be experiencing heavy load or temporary downtime.");
                    //this.updaterResult.status = UpdaterStatus.FAIL_CONNECTION;
                    PluginUpdater.this.setStatusAndRemoveWorker(UpdaterStatus.FAIL_CONNECTION, plugin, this);
                }
                //e.printStackTrace();
                this.plugin.getLogger().info(e.getMessage());
                return false;
            }
        }

        
        // -------------------------------------------------------
        
        /**
         * The class that is used for the async worker thread.
         */
        private class WorkerRunnable implements Runnable
        {
            @Override
            public void run()
            {
                if (Worker.this.url != null)
                {
                	Worker.this.setPhase(WorkerPhase.LOOKUP);  // <- thread safe synchronized method //UpdaterWorker.this.phase = WorkerPhase.LOOKUP; // <- Not thread safe
                	if(!Worker.this.lookup || Worker.this.read())
                	{
                		Worker.this.setPhase(WorkerPhase.CHECK); //UpdaterWorker.this.phase = WorkerPhase.CHECK;
                		if(!Worker.this.check || Worker.this.versionCheck())
                		{
                			Worker.this.setPhase(WorkerPhase.DOWNLOAD); //UpdaterWorker.this.phase = WorkerPhase.DOWNLOAD;
                			if(Worker.this.download)
                    			download();
                    		else if(Worker.this.check)
                    			PluginUpdater.this.setStatusAndRemoveWorker(UpdaterStatus.UPDATE_AVAILABLE, Worker.this.plugin, Worker.this);
                    		else
                    			PluginUpdater.this.setStatusAndRemoveWorker(UpdaterStatus.LOOKUP_SUCCESS, Worker.this.plugin, Worker.this);
                		}
                	}
                }
                else
                {
                	PluginUpdater.this.setStatusAndRemoveWorker(UpdaterStatus.FAIL_CONNECTION, Worker.this.plugin, Worker.this);
                }
            }
            
            private void download()
            {
            	if (Worker.this.updaterResult.versionLink == null)
            	{
        			//UpdaterWorker.this.updaterResult.status = UpdaterStatus.FAIL_DOWNLOAD;
            		PluginUpdater.this.setStatusAndRemoveWorker(UpdaterStatus.FAIL_DOWNLOAD, Worker.this.plugin, Worker.this);
            	}
        		else
        		{
        			String name;
                    if (Worker.this.updaterResult.versionLink.endsWith(".zip")) // If it's a zip file, it shouldn't be downloaded as the plugin's name
                    {
                        final String[] split = Worker.this.updaterResult.versionLink.split("/");
                        name = split[split.length - 1];
                    }
                    else
                    	name = Worker.this.file.getName();
                    
                    Worker.this.saveFile(new File(Worker.this.plugin.getDataFolder().getParent(), Worker.this.updateFolder), name, Worker.this.updaterResult.versionLink);
        		}
            }
        }
        
    } //Worker class end.
}