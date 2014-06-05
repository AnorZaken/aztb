package nu.mine.obsidian.aztb.bukkit.subcommand.v1_0;

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

import java.util.List;
import java.util.Locale;


import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;


/**
 * The base-class used to implement SubCommands, for use with a {@link SubCommandGroup}. 
 * <p/><i>Note: Name and aliases are <u>always</u> converted to lowercase with the {@link Locale}
 * that can be specified with {@link SCStatics#setLocale(Locale)}! (This is done for performance reasons.)</i>
 * <p/>(<i>Help coloring inspired by WorldBorder</i>)
 * @see SubCommandGroup
 * @author AnorZaken
 * @version 1.0
 */
public abstract class SubCommand extends SCBase
{
	/**
	 * Interface for supplying all the needed messages that {@link SubCommand} might need to send.
	 */
	public static interface IStringProvider
	{
		/**
		 * Prefix to use when displaying the aliases for a command. 
		 * <p/><i>Default: "Aliases:"</i>
		 * @return if this returns {@code null} aliases will not be displayed!
		 */
		String sc_aliasesPrefix();
	}
	
	// ------
	
	protected final int minArgs;
	protected final int maxArgs;
	protected final String permission; //SHOLD ONLY BE USED by hasPermission(CommandSender) !!
	private String name = null;
	private String[] aliases = null;
	
	private String usageCons = null; //cached chat-formated usage string for console
	private String usagePlay = null; //cached chat-formated usage string for players
	private String aliasFormated = null; //cached chat-formated aliases string
	
	protected static final String MSG_ALIAS = "Aliases:";
	
	protected IStringProvider msgProvider;
	
	/**
	 * Constructor for SubCommand. <p/>Setting {@code minArgs} and {@code maxArgs} correctly can simplify
	 * the implementation of a SubCommand because the contract of {@link SubCommandGroup} towards 
	 * SubCommands guarantees that {@link #onCommand(CommandSender, String[], String) onCommand} and 
	 * {@link #onTabComplete(CommandSender, String[]) onTabComplete}<br>will only be called if the 
	 * {@link CommandSender user} provided an appropriate number of arguments.
	 * <p/><i>Note: The first argument will always be the name/alias that was used to invoke the {@link SubCommand} itself.
	 * <br><u>Thus for a parameter-less command the minimum (and maximum) amount of arguments would be 1.</u></i>
	 * @param minArgs is the lower bound on argument count that this SubCommand needs. (<i>+1</i>)
	 * @param maxArgs is the upper bound on argument count that this SubCommand can use. (<i>+1</i>)
	 * @param permission is the full name of the permission the user needs to be able to use this command
	 * 		<br>(or {@code null} if this command doesn't require any permission). (For more advanced permission-checking
	 * 		override {@link #hasPermission(CommandSender)}).
	 * @throws IllegalArgumentException when&nbsp {@code minArgs <= 0}&nbsp or &nbsp{@code minArgs > maxArgs}.
	 * @see #hasPermission(CommandSender)
	 */
	protected SubCommand(final int minArgs, final int maxArgs, final String permission)
	{
		if (minArgs <= 0)
			throw new IllegalArgumentException("minArgs is LEQ 0");
		if (minArgs > maxArgs)
			throw new IllegalArgumentException("minArgs > maxArgs");
		this.minArgs = minArgs;
		this.maxArgs = maxArgs;
		this.permission = permission;
		setIStringProvider(null);
	}
	
	// ------
	
	/**
	 * Set the IStringProvider of this {@link SubCommand}.
	 * @param msgProvider if this is {@code null} a simplified set of default messages will be used instead.
	 */
	public void setIStringProvider(final IStringProvider msgProvider)
	{
		this.msgProvider = msgProvider != null ? msgProvider
				: new IStringProvider() {
			public String sc_aliasesPrefix() { return MSG_ALIAS; }
		};
	}
	
	/**
	 * Returns whether or not the name and aliases of this {@link SubCommand} has been initialized.
	 * @see #initializeNames()
	 */
	public boolean isInitialized() {
		return name != null;
	}
	
	/**
	 * <b><i>Must be called to initialize the command-names!<br>Must be called again to update the name-cache if the
	 * command name or aliases are changed!</i></b>
	 * <p/><i>Note1: This should <u>never</u> be called directly if the {@link SubCommand} is part of a {@link SubCommandGroup}!
	 * Instead call {@link SubCommandGroup#checkAndInitializeNames(CommandSender)} which will ensure that names and aliases are
	 * only initialized if they are all valid and no name-collisions exists within the group!
	 * <p/>Note2: Name initialization invalidates cached usage Strings (if needed) and alias Strings (always).
	 * <p/>Note3: Checks for {@code null} aliases and removes them.</i>
	 * @see #invalidateCachedStrings(boolean, boolean, boolean)
	 */
	public void initializeNames()
	{
		final String nameLC = getNameInternal().toLowerCase(SCStatics.locale);
		final String[] aArr = getAliasesInternal();
		if (aArr == null)
			initializeNamesInternal(nameLC, null, 0);
		else {
			int vCount = aArr.length;
			for (int i = 0; i < vCount;) {
				if (aArr[i] == null || aArr[i].length() == 0)
					aArr[i] = aArr[--vCount];
				else
					++i;
			}
			//(=) replace bad element by last element,
			//(--vCount) last element "used" so decrease count of valid elements,
			//also we are not advancing the index variable so to get the replacement element checked in next loop,
			initializeNamesInternal(nameLC, aArr, vCount);
		}
	}
	
	/**
	 * @param nameLC the name for this command - already converted to lowercase!
	 * @param checkedAliases an array of aliases where the first {@code validCount}-number of elements are
	 *  the (guaranteed to be non-null) aliases that should be used in the initialization
	 * @param validCount count for number of non-null aliases in the alias-array
	 * @see #initializeNames()
	 */
	void initializeNamesInternal(final String nameLC, final String[] checkedAliases, int validCount)
	{
		if (!nameLC.equals(name)) {
			name = nameLC;
			invalidateCachedStrings(true, false, false); //Invalidate cached usage-strings
		}
		invalidateCachedStrings(false, true, false); //Invalidate cached alias-string
		if (validCount == 0)
			aliases = null;
		else {
			String[] aliases = new String[validCount];
			for (int i = 0; i < validCount; ++i)
				aliases[i] = checkedAliases[i].toLowerCase(SCStatics.locale);
			this.aliases = aliases;
		}	
	}
	
	/**
	 * Invalidates cached Strings forcing them to be recreated the next time they are requested. (Thread safe)
	 * @see  #initializeNames()
	 */
	public void invalidateCachedStrings(boolean invalidateUsage, boolean invalidateAlias, boolean invalidateDescription)
	{
		if (invalidateUsage) { //T safe
			usageCons = null;
			usagePlay = null;
		}
		if (invalidateAlias) { //T safe
			aliasFormated = null;
		}
		if (invalidateDescription) { //T safe
			descFormated = null;
		}
	}
	
	/**
	 * Updates cached Strings for this {@link SubCommand}. (Thread safe) (This is simply a convenience method.)
	 * @return {@code false} if unable to update usage-Strings (happens if the {@link SubCommand} isn't
	 *  initialized), otherwise {@code true}.
	 * @see #isInitialized()
	 * @see #updateUsageStrings()
	 * @see #updateAliasString()
	 * @see #updateDescriptionString()
	 */
	public boolean updateCachedStrings(boolean updateUsage, boolean updateAlias, boolean updateDescription)
	{
		if(updateAlias)
			updateAliasString();
		if(updateDescription)
			updateDescriptionStringInternal();
		if(updateUsage)
			return updateUsageStrings();
		else
			return true;
	}
	
	/**
	 * Updates the cached usage-Strings. (Thread safe)
	 * @return {@code false} if unable to update usage-Strings (happens if the {@link SubCommand} isn't
	 *  initialized), otherwise {@code true}.
	 * @see #isInitialized()
	 */
	public boolean updateUsageStrings() {
		return updateUsageStringsInternal(false) != null;
	}
	
	/**
	 * Updates both usage strings and returns the one that matches the argument, or {@code null} if not initialized.
	 * <br>(Thread safe)
	 */
	protected final String updateUsageStringsInternal(boolean returnForPlayer)
	{
		final String name = this.name;
		if (name == null)
			return null;
		
		final String p = getUsageParameters();
		final String s = SCStatics.RESET_SPACE + SCStatics.mName + name;
		if (p == null) {
			usageCons = usagePlay = s;
			return s;
		}
		else if (p.indexOf(SCStatics.uDEP1) == -1) //usage same for console and players
		{
			StringBuilder ucons = new StringBuilder(s.length() + p.length()*2).append(s).append(SCStatics.RESET_SPACE);
			SCStatics.formatUsageString(p, ucons, null);
			final String s1 = ucons.toString();
			usageCons = s1;
			usagePlay = s1;
			return s1;
		}
		else //usage differs between console and players
		{
			StringBuilder ucons = new StringBuilder(s.length() + p.length()*2).append(s).append(SCStatics.RESET_SPACE);
			StringBuilder uplay = new StringBuilder(s.length() + p.length()*2).append(s).append(SCStatics.RESET_SPACE);
			SCStatics.formatUsageString(p, ucons, uplay);
			final String s1 = ucons.toString();
			final String s2 = uplay.toString();
			usageCons = s1;
			usagePlay = s2;
			return returnForPlayer ? s2 : s1;
		}
	}
	
	/**
	 * Updates the cached alias-String. (Thread safe)<br>(This is done simply by invalidating it and then requesting it.)
	 */
	public void updateAliasString()
	{
		aliasFormated = null; //invalidate
		getAliasesFormated(); //request (=> update)
	}
	
	// -----
	
	/**
	 * Get the name of this {@link SubCommand}. 
	 * <p/><i>Note: Name and aliases are <u>always</u> converted to lowercase with the {@link Locale}
	 * that can be specified with {@link SCStatics#setLocale(Locale)}! (This is done for performance reasons.)
	 * <p/>This lowercase conversion is done by {@link SubCommand#initializeNames()}, which is called internally by
	 * {@link SubCommandGroup#checkAndInitializeNames(CommandSender)} if the {@link SubCommand} belongs to
	 * a {@link SubCommandGroup}. Thus {@link SubCommand#getNameInternal() getNameInternal()} should not bother with
	 * formating the case in any way.</i>
	 * @return The name of this {@link SubCommand}. <i><b>MUST BE NON-NULL!</b></i>
	 */
	abstract protected String  getNameInternal();
	/**
	 * Get the aliases of this {@link SubCommand}.
	 * <p/><i>Note1: Name and aliases are <u>always</u> converted to lowercase with the {@link Locale}
	 * that can be specified with {@link SCStatics#setLocale(Locale)}! (This is done for performance reasons.)
	 * <p/>This lowercase conversion is done by {@link SubCommand#initializeNames()}, which is called internally by
	 * {@link SubCommandGroup#checkAndInitializeNames(CommandSender)} if the {@link SubCommand} is added to
	 * a {@link SubCommandGroup}. Thus {@link SubCommand#getAliasesInternal() getAliasesInternal()} should not bother
	 * with formating the case in any way.</i>
	 * <p/><i>Note2: It is recommended to have as few aliases as possible for performance reasons!</i>
	 */
	abstract protected String[] getAliasesInternal();
	/**
	 * Should return <u>only</u> the parameters-part of this commands usage syntax. 
	 * <p/>Example:<br>
	 * If the commands full usage syntax reads<br>&nbsp
	 * "{@code /command subcommand }{@literal <}{@code required}{@literal >} {@code [optional|optional]}"<br>
	 * then {@code getUsageParameters()} for that command should return only this part:<br>&nbsp
	 * "{@literal <}{@code required}{@literal >} {@code [optional|optional]}"
	 * <p/>For parameters that are required from console but optional for players use curly braces, e.g:<br>
	 * &nbsp&nbsp&nbsp"&#123{@code required_from_console}&#125"<br>
	 * These will be converted and displayed as the appropriate type (required/optional) at runtime depending on
	 * the {@link CommandSender} viewing it. **They are cached so don't worry about performance!**
	 * <p/>The cached values will also include {@link Markup} (coloring / formating) according to the ({@code static})
	 * {@link Markup} settings of {@link SubCommand}. See for example {@link #getMarkupRequired()}.
	 * @param sender is the {@link CommandSender} who is asking for the usage syntax.
	 * @return a {@link String} containing only the parameter-part of the commands usage syntax, or {@code null}
	 * 		if the command is parameterless.
	 */
	abstract protected String  getUsageParameters();
	
	/**
	 * Executes this {@link SubCommand} returning its success.
	 * @param sender Source of the command
	 * @param args Passed command arguments, including command label
	 *  <br>&nbsp&nbsp <u>ALWAYS TRUE:</u> {@code args.length >= minArgs}
	 *  <br>&nbsp&nbsp <u>ALWAYS TRUE:</u> {@code args[0] == } &#123used alias of this SubCommand&#125
	 * @param parentLabel Alias of the parent-command which was used
	 * @return {@code false} to cause usage to get displayed for this {@link SubCommand}, otherwise {@code true}
	 * @see CommandExecutor#onCommand(CommandSender, org.bukkit.command.Command, String, String[])
	 */
	abstract public boolean onCommand(final CommandSender sender, final String[] args, final String parentLabel);
	/**
	 * Tab completes this {@link SubCommand}.
	 * @param sender Source of the command
	 * @param args The arguments passed to the command, including final partial argument to be completed and command label 
	 *  <br>&nbsp&nbsp <u>ALWAYS TRUE:</u> {@code args.length >= max(minArgs, 2)}
	 *  <br>&nbsp&nbsp <u>ALWAYS TRUE:</u> {@code args[0] == } &#123used alias of this SubCommand&#125
	 * @return A List of possible completions for the final argument, or null to default to the command executor
	 * @see TabCompleter#onTabComplete(CommandSender, org.bukkit.command.Command, String, String[])
	 */
	abstract public List<String> onTabComplete(final CommandSender sender, final String[] args);
	
	/**
	 * Get the minimum amount of arguments this {@link SubCommand} needs. 
	 * <p/><i>Note: The first argument will always be the name/alias that was used to invoke the {@link SubCommand} itself.
	 * Thus for a parameter-less command the minimum amount of arguments would be 1.</i>
	 */
	public final int getArgCountMin() {
		return minArgs;
	}
	/**
	 * Get the maximum amount of arguments this {@link SubCommand] can make use of.
	 * <p/><i>Note: The first argument will always be the name/alias that was used to invoke the {@link SubCommand} itself.
	 * Thus for a parameter-less command the maximum amount of arguments would be specified to 1.</i>
	 */
	public final int getArgCountMax() {
		return maxArgs;
	}
	/**
	 * Get the name of this {@link SubCommand}. 
	 * <p/><i>Note1: Name and aliases are <u>always</u> converted to lowercase with the {@link Locale}
	 * that can be specified with {@link SCStatics#setLocale(Locale)}! (This is done for performance reasons.)</i>
	 * <p/><i>Note2: This will return {@code null} if {@link #initializeNames()} hasn't been used to initialize
	 * the names and aliases yet.</i>
	 * @see #isInitialized()
	 */
	public final String getName() {
		return name;
	}
	/**
	 * Get the aliases of this {@link SubCommand}.
	 * <p/><i>Note1: Name and aliases are <u>always</u> converted to lowercase with the {@link Locale}
	 * that can be specified with {@link SCStatics#setLocale(Locale)}! (This is done for performance reasons.)</i>
	 * <p/><i>Note2: This will return {@code null} if {@link #initializeNames()} hasn't been used to initialize
	 * the names and aliases yet.</i>
	 * @return Returns a {@code String[]} with the aliases or {@code null} if this {@link SubCommand} doesn't have any aliases
	 *  or hasn't had its names initialized with {@link #initializeNames()} yet.
	 *  @see #isInitialized()
	 */
	public final String[] getAliases() {
		return aliases;
	}
	
	/**
	 * Get a chat-formated String with the usage syntax for this command. (Thread safe)
	 * <p/><i>Note: The usage-Strings are created and formated the first time they are requested, then they are cached.</i>
	 * @param label is the name of the parent command/alias used
	 * @param isPlayer should be {@code true} if the {@link CommandSender} who is asking for the usage syntax
	 *  is an instance of a {@link Player} 
	 * @return the requested usage-String, or {@code null} if this {@link SubCommand} hasn't been initialized yet.
	 * @see #isInitialized()
	 */
	public final String getUsageFormated(final String label, final boolean isPlayer)
	{
		if (isPlayer) {
			String usagePlay = this.usagePlay;
			if (usagePlay != null || (usagePlay = updateUsageStringsInternal(true)) != null)
				return SCStatics.mParent + "/" + label + usagePlay;
			else
				return null;
		}
		else {
			String usageCons = this.usageCons;
			if (usageCons != null || (usageCons = updateUsageStringsInternal(false)) != null)
				return SCStatics.mParent + label + usageCons;
			else
				return null;
		}
	}
	
	/**
	 * Get a chat-formated String with the aliases for this command (including alias prefix). (Thread safe)
	 * <p/><i>Note: The alias-String is created and formated the first time it is requested, then it gets cached.</i>
	 * @return the alias-String, or {@code null} if this {@link SubCommand} doesn't have any aliases (or display of
	 *  aliases has been disabled because {@link IStringProvider#sc_aliasesPrefix()} returned {@code null}).
	 */
	public final String getAliasesFormated()
	{
		final String aliasPrefix = msgProvider.sc_aliasesPrefix();
		final String[] aliases = this.aliases;
		if (aliasPrefix == null || aliases == null)
			return aliasFormated = null;
		else {
			String aliasFormated = this.aliasFormated;
			if (aliasFormated == null)
				this.aliasFormated = aliasFormated = SCStatics.formatAliasString(aliasPrefix, aliases);
			return aliasFormated;
		}
	}
	
	/**
	 * Checks if {@link CommandSender sender} has permission to use this command. <br>
	 * Used by {@link SubCommandGroup} whenever permission checking for a {@link SubCommand} is needed.
	 * <p/><i>Note: By default this simply uses the permission-String specified in the {@link #SubCommand(int, int, String)}
	 * constructor. Override this method if a more advanced permission check is required.</i>
	 * @param sender The {@link CommandSender} to check permission for (NON-NULL!)
	 * @return {@code false} if the {@link CommandSender} doesn't have what it takes to run this {@link SubCommand},
	 *  otherwise {@code true}.
	 */
	public boolean hasPermission(CommandSender sender) {
		return permission == null || sender.hasPermission(permission);
	}
	
	
	// ==================== Helper functions ==================
	
	
	/**
	 * Does an inexact (tab-completion style) match of userInput against {@code commandName} and {@code aliases},
	 * returning {@code true} if a close enough match is found.
	 * @param userInput The user input (gets converted to lower-case with appropriate Locale)
	 * @param commandName The name of the {@link SubCommand} to tab-complete {@code userInput} against
	 * @param aliases The aliases of the {@link SubCommand} to tab-complete {@code userInput} against
	 * @return {@code true} if {@code userInput} can be tab-completed to the provided name / aliases, otherwise {@code false}.
	 * @see SCStatics#setLocale(Locale)
	 */
	public static boolean tabHelper(String userInput, final String commandName, final String[] aliases)
	{
		if (userInput == null)
			return false;
		else
			userInput = userInput.toLowerCase(SCStatics.locale);
		
		if (commandName != null && commandName.startsWith(userInput))
			return true;
		if(aliases != null)
			for(String s : aliases)
				if(s != null && s.startsWith(userInput))
					return true;
		return false;
	}
	
	/**
	 * Does an exact match of {@code userInput} to {@code commandName} and {@code aliases}, returning {@code true} if a match is found.
	 * @param userInput The user input (gets converted to lower-case with appropriate Locale)
	 * @param commandName The name of the {@link SubCommand} to test {@code userInput} against
	 * @param aliases The aliases of the {@link SubCommand} to test {@code userInput} against
	 * @return {@code true} if {@code userInput} matches any of the provided name / aliases, otherwise {@code false}.
	 * @see SCStatics#setLocale(Locale)
	 */
	public static boolean comHelper(String userInput, final String commandName, final String[] aliases)
	{
		if (userInput == null)
			return false;
		else
			userInput = userInput.toLowerCase(SCStatics.locale);
		
		if (commandName != null && commandName.equals(userInput))
			return true;
		if(aliases != null)
			for(String s : aliases)
				if(s != null && s.equals(userInput))
					return true;
		return false;
	}
}
