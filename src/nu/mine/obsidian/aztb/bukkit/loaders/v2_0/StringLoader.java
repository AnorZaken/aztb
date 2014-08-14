package nu.mine.obsidian.aztb.bukkit.loaders.v2_0;

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

import java.util.ArrayList;

import nu.mine.obsidian.aztb.bukkit.loaders.v1_1.YAMLLoader;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Tool-class specialized in loading Strings from yaml-configs, intended to simplify multilingual support in plugins. <p/>
 * Other classes that needs Strings loaded must implement the {@link ISubscriber} interface and can then subscribe
 * with {@link #addSubscriber(ISubscriber)}. All subscribed classes will get their {@link IStringToLoad} loaded
 * whenever {@link #loadStrings(CommandSender, MissingAction, boolean)} gets called.<br>( <i>{@link StringLoader} uses
 * {@link ISubscriber#getStringToLoadArray(int)} to acquire these on each load.</i> )<br>
 * 
 * @author AnorZaken
 * @version 2.0
 * @param <T> {@link JavaPlugin} using {@link StringLoader}
 */
public class StringLoader<T extends JavaPlugin>
{
	/**
	 * Each subscriber to {@link StringLoader} need to implement this. </p>
	 * It consists of a single method:<br> 
	 * {@code StringLoader.IStringToLoad[] getStringToLoadArray(int batchIndex);}
	 * @author AnorZaken
	 * @see StringLoader.ISubscriber#getStringToLoadArray(int)
	 * @see StringLoader.IStringToLoad
	 */
	public static interface ISubscriber
	{
		/**
		 * This method should return all {@link IStringToLoad} objects that this {@link ISubscriber} wants loaded. <p/>
		 * To allow more flexibility for the class implementing the {@link ISubscriber} interface the implementor is
		 * allowed to distribute its {@link IStringToLoad} objects into as many arrays as it wants.
		 * StringLoader will call {@link #getStringToLoadArray(int)} starting with {@code batchIndex = 0} and 
		 * incrementing until it returns {@code null}.<p/>
		 * (<i>This will be done for each subscriber whenever {@link StringLoader#loadStrings(CommandSender, 
		 * MissingAction, boolean) StringLoader.loadStrings(...)} gets called.<i/>)
		 * @param batchIndex allows fetching of multiple arrays of {@link IStringToLoad} objects
		 * @return An array of {@link IStringToLoad} objects or {@code null} when there are no further 
		 * {@link IStringToLoad} objects to fetch.
		 */
		StringLoader.IStringToLoad[] getStringToLoadArray(final int batchIndex);
	}
	
	/**
	 * A String-object to be loaded with {@link StringLoader}. <p/>
	 * <i>Tip: A clean and easy way to implement lots of these in a class is with an {@code enum}.<br>
	 * This makes it easy to keep track of, add new, and use existing Strings in your code.<br>
	 * Example code:</i>
	 * <p/>{@code enum MyMessages implements StringLoader.IStringToLoad}
	 * <br>&#123
	 * <br>&nbsp&nbsp&nbsp&nbsp<i>{@code USER_GREETING }</i>{@code ("greetingMsg", "Hello user!"),}
	 * <br>&nbsp&nbsp&nbsp&nbsp<i>{@code USER_GOODBYE }</i>&nbsp {@code ("goodbyeMsg", }&nbsp{@code "Until next time..."),}
	 * <br>&nbsp&nbsp&nbsp ;
	 * <br>&nbsp&nbsp&nbsp&nbsp{@code private final String cfg;}
	 * <br>&nbsp&nbsp&nbsp&nbsp{@code private String msg;}
	 * <br>&nbsp&nbsp&nbsp&nbsp{@code MyMessages(String configVariableName, String defaultMessage) }&#123
	 * <br>&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp{@code cfg = configVariableName; msg = defaultMessage;}
	 * <br>&nbsp&nbsp&nbsp&nbsp&#125
	 * <br>&nbsp&nbsp&nbsp&nbsp<i>{@code @Override}</i>
	 * <br>&nbsp&nbsp&nbsp&nbsp{@code public String getStr() }&#123
	 * <br>&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp{@code return msg;}
	 * <br>&nbsp&nbsp&nbsp&nbsp&#125
	 * <br>&nbsp&nbsp&nbsp&nbsp<i>{@code @Override}</i>
	 * <br>&nbsp&nbsp&nbsp&nbsp{@code public void setStr(String value) }&#123
	 * <br>&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp{@code msg = value;}
	 * <br>&nbsp&nbsp&nbsp&nbsp&#125
	 * <br>&nbsp&nbsp&nbsp&nbsp<i>{@code @Override}</i>
	 * <br>&nbsp&nbsp&nbsp&nbsp{@code public String getCfg() }&#123
	 * <br>&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp{@code return cfg;}
	 * <br>&nbsp&nbsp&nbsp&nbsp&#125
	 * <br>&#125
	 * <p/><i>
	 * And to get them into an array (for the purpose of implementing {@link ISubscriber}) you would simply...
	 * <br>Example code:
	 * <p/>{@code @Override}</i>
	 * <br>{@code public StringLoader.IStringToLoad[] getStringToLoadArray(int batchIndex)}
	 * <br>&#123
	 * <br>&nbsp&nbsp&nbsp&nbsp{@code if (batchIndex == 0)}
	 * <br>&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp{@code return MyMessages.}<i>{@code values()}</i>{@code ;}
	 * <br>&nbsp&nbsp&nbsp&nbsp{@code else}
	 * <br>&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp{@code return null;}
	 * <br>&#125
	 * <p/>
	 * <i>Note: If you use {@link StringLoader#loadStrings(CommandSender, MissingAction, boolean)
	 * StringLoader.loadStrings(...)} with {@link MissingAction#CREATE_EMPTY} it is recommended to modify your
	 * {@code setStr()}-method so it ignores empty Strings (Else you will get some empty strings on next load!)</i>
	 * 
	 * @author AnorZaken
	 * @see StringLoader.ISubscriber
	 */
	public static interface IStringToLoad
	{
		/**
		 * Get the current value of this {@link IStringToLoad}.
		 */
		String getStr();
		/**
		 * Set the value of this {@link IStringToLoad}.
		 */
		void setStr(String value);
		/**
		 * Get the config name of this {@link IStringToLoad}. </p>
		 * <i>The config name is the name under which this String is stored in an yaml file.</i>
		 */
		String getCfg();
	}
	
	// -----
	
	/**
	 * Interface for supplying all the needed messages that StringLoader might need to send.
	 */
	public static interface IStringProvider
	{
		/**
		 * Message used to inform that a loading is taking place.
		 * <p/><i>Default: "Loading %s..."</i>
		 * @param filename name of the file that is being loaded
		 * @return if this returns {@code null} no message will be displayed.
		 */
		String cfg_loading(final String filename);
		/**
		 * Error message used when the loading of the yaml fails. <p/>
		 * <i>Note: Not used when FileNotFound occurs <b>AND</b> the {@code failIfFileNotFound} parameter 
		 * to {@link StringLoader#loadStrings(CommandSender, MissingAction, boolean)} is {@code false}.
		 * <p/><i>(Default for when this is {@code null} is handled by {@link YAMLLoader}.) </i>
		 * @return if this returns {@code null} a default message will be displayed instead.
		 * @see StringLoader#loadStrings(CommandSender, MissingAction, boolean)
		 * @see YAMLLoader#loadYaml(CommandSender, boolean, String)
		 */
		String cfg_errorLoading();
		/**
		 * Message used when a failure other than YAMLInvalid occurs on yaml-loading.
		 * <p/><i>Default: "%s not found or unable to load. Loading skipped..."</i>
		 * @param filename name of the file that is being loaded
		 * @return if this returns {@code null} a default message will be displayed instead.<br>
		 * Unless the reason was FileNotFound and {@code failIfFileNotFound == false}.<br>
		 * In that specific case no message will be displayed if this is {@code null}.
		 * @see StringLoader#loadStrings(CommandSender, MissingAction, boolean)
		 */
		String cfg_fileLoadFail(final String filename);
		/**
		 * Message used when a loaded file does not contain valid YAML.
		 * <p/><i>Default: "%s is not a valid YAML-file. Loading skipped..."</i>
		 * @param filename name of the file that is being loaded
		 * @return if this returns {@code null} a default message will be displayed instead.
		 */
		String cfg_fileYAMLInvalid(final String filename);
		/**
		 * Message used when the type of a variable to load doesn't match the type in the yaml-file. (String)
		 * <p/><i>Default: "Entry \"%1$s\" in %2$s is malformed!"</i>
		 * @param entry name of the variable
		 * @param filename name of the file that is being loaded
		 * @return if this returns {@code null} a default message will be displayed instead.
		 */
		String cfg_warnMalformed(final String entry, final String filename);
		/**
		 * Message used when a variable to load wasn't found in the yaml-file.
		 * <p/><i>Default: "Entry \"%1$s\" is missing from %2$s"</i>
		 * @param entry name of the variable
		 * @param filename name of the file that is being loaded
		 * @return if this returns {@code null} no message will be displayed.
		 */
		String cfg_entryMissing(final String entry, final String filename);
		/**
		 * Message used to inform the player of the number of unrecognized variables found when loading the yaml-file. <p/>
		 * (<i>Note: Includes malformed entries</i>)
		 * <p/><i>Default: "%s contains %d unrecognized or malformed entries!"</i>
		 * @param count number of unrecognized variables
		 * @param filename name of the file that is being loaded
		 * @return if this returns {@code null} no message will be displayed.
		 */
		String cfg_unrecognizedKeys(final int count, final String filename);
		/**
		 * Message used when StringLoader fails to save a yaml-file.
		 * <p/><i>Default: "Error saving the config, see server log for details."</i>
		 * @return if this returns {@code null} a default message will be displayed instead.
		 */
		String cfg_errorSaving();
		/**
		 * Message used to inform the player of the number of missing variables that was added to a yaml while loading it.
		 * <p/><i>Default: "%d missing fields added to %s"</i>
		 * @param count number of missing variables
		 * @param filename name of the file that is being loaded
		 * @return if this returns {@code null} no message will be displayed.
		 */
		String cfg_addedMissing(final int count, final String filename);
		/**
		 * Message used when trying to save a yaml-file but it can't be done because a file with that name already exists and
		 * {@code allowOverwrite == false}.
		 * <p/><i>Default: "%s already exists. Please remove the old file first."</i>
		 * @param filename name of the file that already existed
		 * @return if this returns {@code null} a default message will be displayed instead.
		 * @see StringLoader#saveStrings(CommandSender, boolean, String)
		 */
		String cfg_saveFileExists(final String filename);
		/**
		 * Message used when an {@link IStringToLoad} array returned from an {@link ISubscriber} contains a {@code null} element.
		 * <p/><i>Default: "WARNING: An IStringToLoad[] array from one of StringLoaders subscribers contained a NULL-element -- Ignoring."</i>
		 * @return if this returns {@code null} a default message will be displayed instead.
		 * @see ISubscriber#getStringToLoadArray(int)
		 */
		String cfg_istlIsNull();
		/**
		 * Message used when {@link IStringToLoad#getCfg()} returns {@code null}.
		 * <p/><i>Default: "WARNING: IStringToLoad.getCfg() returned NULL -- Ignoring faulty ISTL-object."</i>
		 * @return if this returns {@code null} a default message will be displayed instead.
		 */
		String cfg_istlCfgIsNull();
		/**
		 * Message used when trying to save an {@link IStringToLoad} to a yaml-file but 
		 * {@link IStringToLoad#getStr()} returns {@code null}.
		 * <p/><i>Default: "INFO: IStringToLoad.getStr() returned NULL -- Saving \"%s\" as an empty string."</i>
		 * @param cfg the config name of the faulty {@link IStringToLoad} object
		 * @return if this returns {@code null} a default message will be displayed instead.
		 */
		String cfg_istlStrIsNull(final String cfg);
	}
	
	protected static class StringProviderWrapper implements IStringProvider
	{
		//Note: if these are changed, a lot of javadoc updates will be required in IStringProvider!
		private final String MSG_COUNT_UNREC_2 = "%s contains %d unrecognized or malformed entries!"; //cfg_unrecognizedKeys(int count, String filename)
		private final String MSG_LOADING_1 = "Loading %s..."; //cfg_loading(filename)
		private final String MSG_MISSING_2 = "Entry \"%1$s\" is missing from %2$s"; //cfg_entryMissing(final String entry, final String filename)
		private final String MSG_COUNT_MISS_2 = "%d missing fields added to %s";
		//...
		private final String MSG_MALFORMED_2 = "Entry \"%1$s\" in %2$s is malformed!"; //cfg_warnMalformed(cfg, filename)
		private final String MSG_FILE_EXIST_1 = "%s already exists. Please remove the old file first."; //cfg_saveFileExists(templateFilename)
		private final String MSG_ISTL_STR_NULL_1 = "INFO: IStringToLoad.getStr() returned NULL -- Saving \"%s\" as an empty string."; //cfg_istlStrIsNull(cfg) -- silenced in loading if null (never silent in saving)
		private final String MSG_ISTL_NULL = "WARNING: An IStringToLoad[] array from one of StringLoaders subscribers contained a NULL-element -- Ignoring."; //cfg_istlIsNull()
		private final String MSG_ISTL_CFG_NULL = "WARNING: IStringToLoad.getCfg() returned NULL -- Ignoring faulty ISTL-object."; //cfg_istlCfgIsNull()
		private final String MSG_YAML_FAIL_1 = "%s is not a valid YAML-file. Loading skipped..."; //cfg_fileYAMLInvalid(filename)
		private final String MSG_LOAD_FAIL_1 = "%s not found or unable to load. Loading skipped..."; //cfg_fileLoadFail(filename) -- displayed if failIfFileNotFound == true
		private final String MSG_SAVE_ERR = "Error saving the config, see server log for details."; //cfg_errorSaving()
		
		// ------
		
		protected final IStringProvider stringProvider;
		
		protected StringProviderWrapper(final IStringProvider stringProvider) {
			this.stringProvider = stringProvider;
		}
		
		@Override
		public String cfg_warnMalformed(String entry, String filename)
		{
			final String s;
			return (stringProvider != null && (s = stringProvider.cfg_warnMalformed(entry, filename)) != null)
					? s : String.format(MSG_MALFORMED_2, entry, filename);
		}
		@Override
		public String cfg_unrecognizedKeys(int count, String filename)
		{
			return stringProvider == null ? String.format(MSG_COUNT_UNREC_2, filename, count)
					: stringProvider.cfg_unrecognizedKeys(count, filename);
		}
		@Override
		public String cfg_saveFileExists(String filename)
		{
			final String s;
			return (stringProvider != null && (s = stringProvider.cfg_saveFileExists(filename)) != null)
					? s : String.format(MSG_FILE_EXIST_1, filename);
		}
		@Override
		public String cfg_loading(String filename)
		{
			return stringProvider == null ? String.format(MSG_LOADING_1, filename)
					: stringProvider.cfg_loading(filename);
		}
		@Override
		public String cfg_istlStrIsNull(String cfg)
		{
			final String s;
			return (stringProvider != null && (s = stringProvider.cfg_istlStrIsNull(cfg)) != null)
					? s : String.format(MSG_ISTL_STR_NULL_1, cfg);
		}
		@Override
		public String cfg_istlIsNull()
		{
			final String s;
			return (stringProvider != null && (s = stringProvider.cfg_istlIsNull()) != null)
					? s : MSG_ISTL_NULL;
		}
		@Override
		public String cfg_istlCfgIsNull()
		{
			final String s;
			return (stringProvider != null && (s = stringProvider.cfg_istlCfgIsNull()) != null)
					? s : MSG_ISTL_CFG_NULL;
		}
		@Override
		public String cfg_fileYAMLInvalid(String filename)
		{
			final String s;
			return (stringProvider != null && (s = stringProvider.cfg_fileYAMLInvalid(filename)) != null)
					? s : String.format(MSG_YAML_FAIL_1, filename);
		}
		@Override
		public String cfg_fileLoadFail(String filename)
		{
			final String s;
			return (stringProvider != null && (s = stringProvider.cfg_fileLoadFail(filename)) != null)
					? s : String.format(MSG_LOAD_FAIL_1, filename);
		}
		@Override
		public String cfg_errorSaving()
		{
			final String s;
			return (stringProvider != null && (s = stringProvider.cfg_errorSaving()) != null)
					? s : MSG_SAVE_ERR;
		}
		@Override
		public String cfg_errorLoading() //cfg_errorLoading() is used by YAMLLoader - and if it is null YAMLLoader uses its own default msg!
		{
			return stringProvider == null ? null : stringProvider.cfg_errorLoading();
		}
		@Override
		public String cfg_entryMissing(String entry, String filename)
		{
			return stringProvider == null ? String.format(MSG_MISSING_2, entry, filename)
					: stringProvider.cfg_entryMissing(entry, filename);
		}
		@Override
		public String cfg_addedMissing(int count, String filename)
		{
			return stringProvider == null ? String.format(MSG_COUNT_MISS_2, count, filename)
					: stringProvider.cfg_addedMissing(count, filename);
		}
	}
	
	// -----
	
	/**
	 * Action to take when an {@link IStringToLoad} objects config name was not found when loading a yaml-file. 
	 * <p/>This enum is used by {@link StringLoader#loadStrings(CommandSender, MissingAction, NotExistAction)}.
	 * @author AnorZaken
	 * @see StringLoader#loadStrings(CommandSender, MissingAction, NotExistAction)
	 */
	public enum MissingAction
	{
		/**
		 * Don't do anything about missing fields (wont touch the yaml-file).
		 */
		NO_ACTION,
		/**
		 * Missing fields will be added to the yaml-file as empty Strings.
		 */
		CREATE_EMPTY,
		/**
		 * Missing fields will be added to the yaml-file with the value retrieved from {@link IStringToLoad#getStr()}.
		 */
		CREATE_FILLED,
	}
	
	// -----
	
	/**
	 * Action to take when trying to load strings but the specified file doesn't exist. 
	 * <p/>This enum is used by {@link StringLoader#loadStrings(CommandSender, MissingAction, NotExistAction)}.
	 * @author AnorZaken
	 * @see StringLoader#loadStrings(CommandSender, MissingAction, NotExistAction)
	 */
	public enum NotExistAction
	{
		/**
		 * Return false and report as a failure to the user.
		 */
		FAIL_HARD,
		/**
		 * Return false and inform the user.
		 */
		FAIL_SOFT,
		/**
		 * Return true and inform the user.
		 */
		SUCCEED,
	}
	
	// -----
	
	/**
	 * Action to take when trying to save strings but the target file already exist. 
	 * <p/>This enum is used by {@link StringLoader#saveStrings(CommandSender, ExistAction, String)}.
	 * @author AnorZaken
	 * @see StringLoader#saveStrings(CommandSender, ExistAction, String)
	 */
	public enum ExistAction
	{
		/**
		 * Leave the existing file alone and report a failure.
		 */
		FAIL,
		/**
		 * Attempt to overwrite the existing file.
		 */
		OVERWRITE,
		/**
		 * Leave the existing file alone and do nothing.
		 */
		NOTHING,
	}
	
	// -----
	
	protected IStringProvider msgProvider; //<-- neverNull
	protected final ArrayList<ISubscriber> subscribers = new ArrayList<ISubscriber>();
//	protected final YAMLLoader<T> loader; //<-- neverNull
	protected final T plugin;
	protected String yamlHeader = null;
	protected boolean reportUnrecognized = false;
	protected Character pathSeparator = null;
	
	/**
	 * Constructs a StringLoader for {@code plugin} that will load Strings from a yaml file named {@code filename}.
	 * @param plugin The {@link JavaPlugin} using this StringLoader (used to find the plugins config folder).
	 * @param stringProvider if this is {@code null} a simplified set of default messages will be used instead.
	 */
	public StringLoader(final T plugin, final IStringProvider stringProvider)
	{
//		loader = new YAMLLoader<T>(plugin, filename);
		this.plugin = plugin;
		setIStringProvider(stringProvider);
	}
	
	// ==============
	
	/**
	 * Get the yaml path separator char.
	 */
	public char pathSeparator() {
		if (pathSeparator == null)
			pathSeparator = new Character((new YamlConfiguration()).options().pathSeparator());
		return pathSeparator.charValue();
	}
	
	/**
	 * Set the yaml path separator char.
	 * @param pathSeparator char to use as path separator for yaml keys
	 */
	public void pathSeparator(final char pathSeparator) {
		this.pathSeparator = new Character(pathSeparator);
	}
		
	/**
	 * Set the IStringProvider of this StringLoader.
	 * @param stringProvider if this is {@code null} a simplified set of default messages will be used instead.
	 */
	public void setIStringProvider(final IStringProvider stringProvider)
	{
		this.msgProvider = new StringProviderWrapper(stringProvider);
	}
	
	/**
	 * Get ReportUnrecognized.
	 * <p/><i>If {@code reportUnrecognized == false} then {@link #loadStrings(CommandSender, MissingAction, boolean)}
	 * will not send any message if the loaded file contained any unrecognized entries (malformed or unused).
	 * <p/>Default is {@code false}.</i>
	 */
	public boolean getReportUnrecognized() {
		return reportUnrecognized;
	}
	
	/**
	 * Set ReportUnrecognized.
	 * <p/><i>If {@link #getReportUnrecognized()} {@code == true} then {@link #loadStrings(CommandSender, MissingAction, boolean)}
	 * will send a message if the loaded file contained any unrecognized entries (malformed or unused).</i>
	 */
	public void setReportUnrecognized(boolean reportUnrecognized) {
		this.reportUnrecognized = reportUnrecognized;
	}
	
	/**
	 * Sets the yaml-header that should be used when modifying or saving the config.
	 */
	public void setYAMLHeader(final String header) {
		this.yamlHeader = header;
	}
	
	/**
	 * Add an {@link ISubscriber} to this {@linkplain StringLoader}.
	 * @throws IllegalArgumentException if {@code subscriber == null}
	 * @return {@code true} if {@code subscriber} was added, {@code false} if {@code subscriber} was already subscribed.
	 */
	public boolean addSubscriber(final ISubscriber subscriber)
	{
		if (subscriber == null)
			throw new IllegalArgumentException("subscriber can not be null!");
		else if (subscribers.contains(subscriber))
			return false;
		else
			return subscribers.add(subscriber);
	}
	
	/**
	 * Remove an {@link ISubscriber} from this StringLoader.
	 * @return {@code true} if the subscriber was removed, otherwise {@code false}.
	 */
	public boolean removeSubscriber(final ISubscriber subscriber) {
		return subscriber != null && subscribers.remove(subscriber);
	}
	
	// -----
	
	/**
	 * Loads all {@link IStringToLoad} objects for all {@link ISubscriber ISubscribers} of this {@link StringLoader}.
	 * @param filename The name of the yaml file containing the Strings -- for example strings.yaml
	 * @param sender {@link CommandSender} to send messages to, or {@code null} if silent operation is desired.
	 * @param missingAction An {@link MissingAction enum} to decides what to do with missing fields (Strings not found 
	 *  in the yaml).
	 * @param notExistAction if this is {@link NotExistAction#FAIL_HARD} FileNotFound will be treated as an error and
	 *  reported to the {@link CommandSender} accordingly. (If FileNotFound occurs the {@link CommandSender} will be
	 *  notified regardless, but only informatively - not as a failure.)
	 * @return {@code true} if yaml-loading succeeded or FileNotFound occurred and {@code notExistAction == SUCCEED},
	 *  otherwise {@code false}.
	 * @throws IllegalStateException if the plugin associated with this {@link StringLoader} isn't properly enabled
	 */
	public boolean loadStrings(final String filename, final CommandSender sender, MissingAction missingAction, final NotExistAction notExistAction)
	{
		final YAMLLoader<T> loader = new YAMLLoader<T>(plugin, filename);
		if (sender != null) {
			String s = msgProvider.cfg_loading(loader.getFileName());
			if (s != null)
				sender.sendMessage(s);
		}
		
		if (missingAction == null)
			missingAction = MissingAction.NO_ACTION;
		
//		if (notExistAction == null) //With current code this has no effect on the result...
//			notExistAction = NotExistAction.FAIL_SOFT;
		
		YAMLLoader.YAMLResult yamlResult = loader.loadYaml(sender, notExistAction == NotExistAction.FAIL_HARD, msgProvider.cfg_errorLoading());
		
		if (yamlResult == null) //Triggering this would indicate plugin being accessed at inappropriate server state
			return false; //That is why I'm not bothering to attempt a save (putting a throw here might be appropriate)
		
		//Language is different to normal configs: we never want to do anything if fileNotFound!
		if (!yamlResult.isFileFound) {
			if (sender != null) {
				final String s = msgProvider.cfg_fileLoadFail(loader.getFileName());
				sender.sendMessage(s);
			}
			return notExistAction == NotExistAction.SUCCEED;
		}
		else if (!yamlResult.isValidConfig) {
			if (sender != null) {
				final String s = msgProvider.cfg_fileYAMLInvalid(loader.getFileName());
				sender.sendMessage(s);
			}
			return false;
		}
		else if (yamlResult.yaml == null) {
			if (sender != null) {
				final String s = msgProvider.cfg_fileLoadFail(loader.getFileName());
				sender.sendMessage(s);
			}
			return false;
		}
		
		
		YamlConfiguration yaml = yamlResult.yaml == null ? new YamlConfiguration() : yamlResult.yaml;
		int totalCount = 0;
		int missingCount = 0;
		int malformedCount = 0;
		
		//Load strings...
		
		for (ISubscriber insl : subscribers)
		{
			IStringToLoad[] istlArr;
			for (int i = 0; (istlArr = insl.getStringToLoadArray(i)) != null; ++i)
			{
				totalCount += istlArr.length;
				for (IStringToLoad istl : istlArr)
				{
					if (istl == null) {
						if (sender != null) {
							final String s = msgProvider.cfg_istlIsNull();
							sender.sendMessage(s);
						}
						continue;
					}
					final String cfg = istl.getCfg();
					if (cfg == null) {
						if (sender != null) {
							final String s = msgProvider.cfg_istlCfgIsNull();
							sender.sendMessage(s);
						}
						continue;
					}
					if (yaml.contains(cfg))
					{
						if (yaml.isString(cfg))
							istl.setStr(yaml.getString(cfg));
						else if (sender != null)
						{
							++malformedCount; //malformedCount is never used if sender == null anyway
							final String s = msgProvider.cfg_warnMalformed(cfg, loader.getFileName());
							sender.sendMessage(s);
						}
					}
					else
					{
						++missingCount;
						if (sender != null) {
							String s = msgProvider.cfg_entryMissing(cfg, loader.getFileName());
							if (s != null)
								sender.sendMessage(s);
						}
						if (missingAction == MissingAction.CREATE_EMPTY)
							yaml.set (cfg, "");
						else if (missingAction == MissingAction.CREATE_FILLED) {
							final String str = istl.getStr();
							if (str != null)
								yaml.set (cfg, str);
							else {
								yaml.set (cfg, "");
								if (sender != null) {
									String s = msgProvider.cfg_istlStrIsNull(cfg);
									if (s != null)
										sender.sendMessage(s);
								}
							}
						}
					}
				}
			}
		}
		
		//Report on entries...
		
		if (reportUnrecognized && sender != null)
		{
			int keyCount = yaml.getKeys(true).size();
			int unrecognizedCount = keyCount + malformedCount - totalCount;
			if (missingAction == MissingAction.NO_ACTION)
				unrecognizedCount += missingCount;
			if (unrecognizedCount > 0) {
				String s = msgProvider.cfg_unrecognizedKeys(unrecognizedCount, loader.getFileName());
				if (s != null)
					sender.sendMessage(s);
			}
		}
		
		//Save strings...
		
		if (missingCount > 0 && missingAction != MissingAction.NO_ACTION)
		{
			if (yamlHeader != null && (yaml.options().header() == null || yaml.options().header().length() == 0))
				yaml.options().header(yamlHeader);
			
			if (loader.saveYaml(sender, yaml, msgProvider.cfg_errorSaving(), true, null))
			{
				if (sender != null) {
					final String s = msgProvider.cfg_addedMissing(missingCount, loader.getFileName());
					if (s != null)
						sender.sendMessage(s);
				}
			}
		}		
		
		return true;
	}
	
	// ----------
	
	/**
	 * Saves all {@link IStringToLoad} objects from all {@link ISubscriber ISubscribers} to a yaml-config. <p/>
	 * (It saves the String from {@link IStringToLoad#getStr()} into the yaml-file as a variable with the name 
	 * {@link IStringToLoad#getCfg()}.)
	 * @param filename The name of the yaml file to save the Strings to -- for example strings.yaml
	 * @param sender {@link CommandSender} to send messages to, or {@code null} if silent operation is desired.
	 * @param existAction if this is {@link ExistAction#FAIL} and error-message will be displayed if the file
	 *  already exist.
	 * @return {@code true} if saving succeeded, otherwise {@code false}.
	 * @throws IllegalStateException if the plugin associated with this {@link StringLoader} isn't properly enabled
	 * @throws IllegalArgumentException if the {@code templateFilename} is <code>null</code> or empty
	 */
	public boolean saveStrings(final String filename, final CommandSender sender, final ExistAction existAction)
	{
		final YAMLLoader<T> saver = new YAMLLoader<T>(plugin, filename);
		
		if (!saver.isPluginEnabledAndHasFolder())
			throw new IllegalStateException("plugin is not properly enabled");
		
		try
		{
			if (saver.getFile().exists() && existAction != ExistAction.OVERWRITE)
			{
				if (existAction == ExistAction.FAIL)
					saver.messageSender.errorFileExists(sender, msgProvider.cfg_saveFileExists(filename));
				return false;
			}
		}
		catch (Exception ex)
		{
			saver.messageSender.errorSaving(sender, msgProvider.cfg_errorSaving(), ex);
			return false;
		}
		
		final YamlConfiguration yaml = buildYaml(sender);
		
		return saver.saveYaml(sender, yaml, msgProvider.cfg_errorSaving(), existAction == ExistAction.OVERWRITE, null);
	}
	
	/**
	 * Builds a {@link YamlConfiguration} from all the {@link ISubscriber ISubscribers}. 
	 * </p><i>Used by {@link #saveStrings(CommandSender, boolean, String)}</i>
	 * @return a {@link YamlConfiguration} ready for saving to file
	 */
	protected YamlConfiguration buildYaml(final CommandSender sender)
	{
		YamlConfiguration yaml = new YamlConfiguration();
		
		if (yamlHeader != null)
			yaml.options().header(yamlHeader);
		
		if (pathSeparator != null)
			yaml.options().pathSeparator(pathSeparator.charValue());
		
		for (ISubscriber insl : subscribers) {
			IStringToLoad[] istlArr;
			for (int i = 0; (istlArr = insl.getStringToLoadArray(i)) != null; ++i)
			{
				for (IStringToLoad istl : istlArr)
				{
					if (istl == null) {
						if (sender != null) {
							final String s = msgProvider.cfg_istlIsNull();
							sender.sendMessage(s);
						}
						continue;
					}
					final String cfg = istl.getCfg();
					if (cfg == null) {
						if (sender != null) {
							final String s = msgProvider.cfg_istlCfgIsNull();
							sender.sendMessage(s);
						}
						continue;
					}
					String str = istl.getStr();
					if (str == null) {
						if (sender != null) {
							final String s = msgProvider.cfg_istlStrIsNull(cfg);
							sender.sendMessage(s);
						}
						str = "";
					}
					yaml.set (cfg, str);
				}
			}
		}
		
		return yaml;
	}
}
