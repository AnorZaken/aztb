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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Class to automate a lot of SubCommand functionality.<br>(Executing the correct SubCommand, automated help-command and tab-completion).
 * <p/>Also makes it a breeze to add, remove, rename or modify commands while still maintaining a clean and clear code structure.
 * Both offline and at runtime!
 * <p/>Want to support different languages? That is easy! You can change everything about a command, including names and aliases,
 * during runtime. Just change what your SubCommand subclass reports, then call verifyAndInitializeAllNames on the SubCommandGroup
 * to guard against command-name collisions and get the new names activated.
 * @author AnorZaken
 * @version 1.0
 */
public class SubCommandGroup implements TabExecutor
{
	/**
	 * Interface for supplying all the needed messages that {@link SubCommandGroup} might need to send.
	 */
	public static interface IStringProvider extends SubCommand.IStringProvider
	{
		/**
		 * Message to display when a {@link SubCommand command} doesn't exist or the user doesn't have permission to use it.
		 * @param label is the name of the command that the user tried use
		 * @return if this returns {@code null} no message will be displayed.
		 */
		String scg_unavailable(final String label);
		
		/**
		 * Message to display when the user has supplied and incorrect number of arguments to a {@link SubCommand command}.
		 * <p/><i>Note: Incorrect means:<br>
		 * Less than {@link SubCommand#getArgCountMin()} or more than {@link SubCommand#getArgCountMax()}.</i>
		 * @return if this returns {@code null} no message will be displayed.
		 */
		String scg_incorrectNumArgs();
		
		/**
		 * Prefix to use when displaying command usage. <p/>
		 * Example: {@link ChatColor ChatColor.DARK_AQUA} {@code +} {@code "Usage: "} &nbsp&nbsp(note the space after the colon)
		 * <p/><i>Default: "Usage:"</i>
		 * @return if this returns {@code null} a default prefix will be used instead.
		 */
		String scg_usagePrefix();
		
		/**
		 * Can be used to add a single alias to {@link SubCommandGroup SubCommandGroups} built in help-command. 
		 * @return if this returns {@code null} the help-command will have no alias (only it's name: "?").
		 */
		String scg_help_alias();
		
		/**
		 * The usage parameter for the help-command.&nbspOnly needed if a a language other than English is desired. 
		 * <p/><i>Default: "{@literal <command|topic> - shows help for commands & topics}"</i>
		 * @return if this returns {@code null} a default message will be displayed instead.
		 */
		String scg_help_usageParameters();
		
		/**
		 * The description of the help-command. <br>In other words what the user sees when executing<br>{@code /parent ? ?}<br> 
		 * where {@code parent} is the name of the {@link PluginCommand} that this {@link SubCommandGroup} is registered to.
		 * <p/><i>Default: "Helpception ?__?"</i>
		 * @return if this returns {@code null} a (silly) default message will be displayed instead.
		 */
		String scg_help_description();
		
		/**
		 * Message to display instead of a command description when a {@link SubCommand command} lacks description.
		 * <p/>(i.e. when {@link SubCommand#getDescriptionInternal(CommandSender)} returns {@code null}.)
		 * <p/><i>Note: This has nothing to do with the display of Usage and Aliases.</i>
		 * @return if this returns {@code null} no message will be displayed.
		 */
		String scg_help_noDesc();
		
		/**
		 * Message to display when the user tries to use the help command on a command that doesn't exist 
		 * (or doesn't have permission for).
		 * @param unavailableLabel is what the user tried to request help for.
		 * @return if this returns {@code null} no message will be displayed.
		 */
		String scg_help_sorry(final String unavailableLabel);
		
		/**
		 * Error message to display when {@link SubCommandGroup#checkAndInitializeNames(CommandSender)} finds a
		 * {@link SubCommand command} with a full-name that is {@code null}.
		 * <p/><i>Default: "NULL name encountered! >> SubCommand initialization for %s failed!"</i>
		 * @param parentName is the name of the {@link PluginCommand} that this {@link SubCommandGroup} is registered to.
		 * @return if this returns {@code null} a default message will be displayed instead.
		 */
		String scg_verifyNullName(final String parentName);
		
		/**
		 * Error message to display when {@link SubCommandGroup#checkAndInitializeNames(CommandSender)} finds a
		 * {@link SubCommand command} with a name that collides with the name of another {@link SubCommand}.
		 * <p/><i>Default: "Duplicate name: %2$s! >> SubCommand initialization for %1$s failed!"</i>
		 * @param parentName is the name of the {@link PluginCommand} that this {@link SubCommandGroup} is registered to.
		 * @param duplicateName is the name that was not unique.
		 * @return if this returns {@code null} a default message will be displayed instead.
		 */
		String scg_verifyDuplicateName(final String parentName, final String duplicateName);
		
		/**
		 * Warning message to display when {@link SubCommandGroup#checkAndInitializeNames(CommandSender)} finds a
		 * {@link SubCommand command} with an alias that is {@code null}.
		 * <p/><i>Default: "NULL alias in SubCommand %2$s ignored! (Parent: %1$s)"</i>
		 * @param parentName is the name of the {@link PluginCommand} that this {@link SubCommandGroup} is registered to.
		 * @param commandName is the name of the {@link SubCommand} that has one of its aliases set to null.
		 * @return if this returns {@code null} a default message will be displayed instead.
		 */
		String scg_verifyNullAlias(final String parentName, final String commandName);
		
		/**
		 * Message to display when {@link SubCommandGroup#checkAndInitializeNames(CommandSender)} finds a
		 * {@link SubCommand command} with an alias that collides with the name of another {@link SubCommand}.
		 * <p/><i>Default: "SubCommand %2$s wants alias %3$s but there exists another SubCommand with that name! (Parent: %1$s)"</i>
		 * @param parentName is the name of the {@link PluginCommand} that this {@link SubCommandGroup} is registered to.
		 * @param aliasOwnerName name of the {@link SubCommand} that wants an unavailable alias.
		 * @param alias the alias desired (also the name of the other {@link SubCommand}).
		 * @param enforceUnique if this is {@code true} the message is an error, if this is <code>false</code> it is a warning.
		 * @return if this returns {@code null} and {@code enforceUnique == true} then a default message will be displayed,
		 *  otherwise no message will be displayed.
		 */
		String scg_verifyAliasName(final String parentName, final String aliasOwnerName, final String alias, final boolean enforceUnique);
		
		/**
		 * Message to display when {@link SubCommandGroup#checkAndInitializeNames(CommandSender)} finds a
		 * {@link SubCommand command} with an alias that collides with another alias.
		 * <p/><i>Default: "SubCommands %2$s and %3$s both desire alias %4$s! (Parent: %1$s)"</i>
		 * @param parentName is the name of the {@link PluginCommand} that this {@link SubCommandGroup} is registered to.
		 * @param name1 name of one of the {@link SubCommand SubCommands}.
		 * @param name2 name of the other {@link SubCommand}.
		 * @param alias the alias desired by the {@link SubCommand SubCommands}.
		 * @param enforceUnique if this is {@code true} the message is an error, if this is <code>false</code> it is a warning.
		 * @return if this returns {@code null} and {@code enforceUnique == true} then a default message will be displayed,
		 *  otherwise no message will be displayed.
		 */
		String scg_verifyAliasAlias(final String parentName, final String name1, final String name2
				, final String alias, final boolean enforceUnique);
		
		/**
		 * Message to display when {@link SubCommandGroup#checkAndInitializeNames(CommandSender)} removes a
		 * (non-{@code null}) alias from a {@link SubCommand command}.
		 * <p/><i>Default: "Alias %3$s removed from SubCommand %2$s! (Parent: %1$s)"</i>
		 * @param parentName is the name of the {@link PluginCommand} that this {@link SubCommandGroup} is registered to.
		 * @param commandName is the name of the {@link SubCommand} that has one of its aliases removed.
		 * @param alias the alias removed.
		 * @return if this returns {@code null} no message will be displayed.
		 */
		String scg_verifyAliasRemoved(final String parentName, final String commandName, final String alias);
		
		/**
		 * The word "key" in the usageHeader - (For translation purposes). <br>
		 * (Simply return {@code null} to use the default value if no translation is desired.)
		 */
		String scg_usageHeaderKey();
		
		/**
		 * The word "command" in the usageHeader - (For translation purposes). <br>
		 * (Simply return {@code null} to use the default value if no translation is desired.)
		 */
		String scg_usageHeaderCommand();
		
		/**
		 * The word "required" in the usageHeader - (For translation purposes). <br>
		 * (Simply return {@code null} to use the default value if no translation is desired.)
		 */
		String scg_usageHeaderRequired();
		
		/**
		 * The word "optional" in the usageHeader - (For translation purposes). <br>
		 * (Simply return {@code null} to use the default value if no translation is desired.)
		 */
		String scg_usageHeaderOptional();
		
		/**
		 * Parent command name substitute when {@link SubCommandGroup} lacks parent command - (For translation purposes).
		 * <p/><i>Default: "[UNREGISTERED]"</i>
		 * @return if this returns {@code null} the default text will be used.
		 */
		String scg_unregistered();
	}
	
	protected static class StringProviderWrapper implements IStringProvider
	{
		//Note: if these are changed, a lot of javadoc updates will be required in IStringProvider!
		protected static final String MSG_VERI_NULL_1 = "NULL name encountered! >> SubCommand initialization for %s failed!";
		protected static final String MSG_VERI_DUPL_2 = "Duplicate name: %2$s! >> SubCommand initialization for %1$s failed!";
		protected static final String MSG_USAGE = "Usage:";
		protected static final String MSG_HLP_USAGE = "<command|topic> - shows help for commands & topics";
		protected static final String MSG_HLP_DESC = "Helpception ?__?";
		protected static final String MSG_USG_KEY = "key";
		protected static final String MSG_USG_COM = "command";
		protected static final String MSG_USG_REQ = "required";
		protected static final String MSG_USG_OPT = "optional";
		protected static final String MSG_UNREG = "[UNREGISTERED]";
		protected static final String MSG_INC_NUM_ARGS = "Incorrect number of arguments";
		protected static final String MSG_VERI_NULL_2 = "NULL alias in SubCommand %2$s ignored! (Parent: %1$s)";
		protected static final String MSG_VERI_ALIAS_NAME_3 =
				"SubCommand %2$s wants alias %3$s but there exists another SubCommand with that name! (Parent: %1$s)";
		protected static final String MSG_VERI_ALIAS_ALIAS_4 =
				"SubCommands %2$s and %3$s both desire alias %4$s! (Parent: %1$s)";
		protected static final String MSG_VERI_ALIAS_REMOVED_3 =
				"Alias %3$s removed from SubCommand %2$s! (Parent: %1$s)";
		
		protected final IStringProvider stringProvider;
		
		protected StringProviderWrapper(final IStringProvider stringProvider) {
			this.stringProvider = stringProvider;
		}
		
		public String sc_aliasesPrefix()
		{
			return stringProvider == null ? SubCommand.MSG_ALIAS : stringProvider.sc_aliasesPrefix();
		}
		public String scg_verifyNullName(String parentName)
		{
			final String s;
			return (stringProvider != null && (s = stringProvider.scg_verifyNullName(parentName)) != null)
					? s : String.format(MSG_VERI_NULL_1, parentName);
		}
		public String scg_verifyDuplicateName(String parentName, String duplicateName)
		{
			final String s;
			return (stringProvider != null && (s = stringProvider.scg_verifyDuplicateName(parentName, duplicateName)) != null)
					? s : String.format(MSG_VERI_DUPL_2, parentName, duplicateName);
		}
		public String scg_usagePrefix()
		{
			final String s;
			return (stringProvider != null && (s = stringProvider.scg_usagePrefix()) != null)
					? s : MSG_USAGE;
		}
		public String scg_unavailable(String label)
		{
			return stringProvider == null ? (label + " is not available") : stringProvider.scg_unavailable(label);
		}
		public String scg_incorrectNumArgs()
		{
			return stringProvider == null ? MSG_INC_NUM_ARGS : stringProvider.scg_incorrectNumArgs();
		}
		public String scg_help_usageParameters()
		{
			final String s;
			return (stringProvider != null && (s = stringProvider.scg_help_usageParameters()) != null)
					? s : MSG_HLP_USAGE;
		}
		public String scg_help_alias()
		{
			return stringProvider == null ? null : stringProvider.scg_help_alias();
		}
		public String scg_help_sorry(String unavailableLabel)
		{
			return stringProvider == null ? ("No help found for " + unavailableLabel) : stringProvider.scg_help_sorry(unavailableLabel);
		}
		public String scg_help_noDesc()
		{
			return stringProvider == null ? null : stringProvider.scg_help_noDesc();
		}
		public String scg_help_description()
		{
			return stringProvider == null ? MSG_HLP_DESC : stringProvider.scg_help_description();
		}
		public String scg_usageHeaderKey()
		{
			final String s;
			return (stringProvider != null && (s = stringProvider.scg_usageHeaderKey()) != null)
					? s : MSG_USG_KEY;
		}
		public String scg_usageHeaderCommand()
		{
			final String s;
			return (stringProvider != null && (s = stringProvider.scg_usageHeaderCommand()) != null)
					? s : MSG_USG_COM;
		}
		public String scg_usageHeaderRequired()
		{
			final String s;
			return (stringProvider != null && (s = stringProvider.scg_usageHeaderRequired()) != null)
					? s : MSG_USG_REQ;
		}
		public String scg_usageHeaderOptional()
		{
			final String s;
			return (stringProvider != null && (s = stringProvider.scg_usageHeaderOptional()) != null)
					? s : MSG_USG_OPT;
		}
		public String scg_unregistered()
		{
			final String s;
			return (stringProvider != null && (s = stringProvider.scg_unregistered()) != null)
					? s : MSG_UNREG;
		}
		public String scg_verifyNullAlias(String parentName, String commandName)
		{
			final String s;
			return (stringProvider != null && (s = stringProvider.scg_verifyNullAlias(parentName, commandName)) != null)
					? s : String.format(MSG_VERI_NULL_2, parentName, commandName);
		}
		public String scg_verifyAliasName(String parentName, String aliasOwnerName, String alias, boolean enforceUnique)
		{
			if (stringProvider != null) {
				final String s = stringProvider.scg_verifyAliasName(parentName, aliasOwnerName, alias, enforceUnique);
				if (s != null || !enforceUnique)
					return s;
			}
			return String.format(MSG_VERI_ALIAS_NAME_3, parentName, aliasOwnerName, alias);
		}
		public String scg_verifyAliasAlias(String parentName, String name1,
				String name2, String alias, boolean enforceUnique)
		{
			if (stringProvider != null) {
				final String s = stringProvider.scg_verifyAliasAlias(parentName, name1, name2, alias, enforceUnique);
				if (s != null || !enforceUnique)
					return s;
			}
			return String.format(MSG_VERI_ALIAS_ALIAS_4, parentName, name1, name2, alias);
		}
		public String scg_verifyAliasRemoved(String parentName, String commandName, String alias)
		{
			return stringProvider == null ? String.format(MSG_VERI_ALIAS_REMOVED_3, parentName, commandName, alias)
					: stringProvider.scg_verifyAliasRemoved(parentName, commandName, alias);
		}
	}
	
	// ============
	
	private IStringProvider stringProvider; //<-- neverNull
	protected final ArrayList<SubCommand> commands = new ArrayList<SubCommand>();
	protected final HelpCommand helpCommand;
	private PluginCommand parentCommand = null; //populated in registerParent
	protected String usageHeader1 = null; //populated in updateUsageHeader
	protected String usageHeader2 = null; //populated in updateUsageHeader
	
	protected static final String MSG_HLP_NAME = "?";
	
	// ============
	
	/**
	 * Will construct a SubCommandGroup with the specified {@link IStringProvider} and add an automated {@link HelpCommand help}-
	 * {@link SubCommand}.
	 * @param msgProvider if this is {@code null} a simplified set of default messages will be used instead.
	 * @see SubCommand
	 */
	public SubCommandGroup(final IStringProvider stringProvider)
	{
		setIStringProvider(stringProvider);
		helpCommand = new HelpCommand();
		//commands.add(helpCommand);
	}
	
	// ============
	
	/**
	 * Set the IStringProvider of this {@link SubCommandGroup}.
	 * @param msgProvider if this is {@code null} a simplified set of default messages will be used instead.
	 */
	public void setIStringProvider(final IStringProvider stringProvider)
	{
		this.stringProvider = new StringProviderWrapper(stringProvider);
	}
	
	/**
	 * Sets this {@link SubCommandGroup} as the executer and tab-completer of {@link PluginCommand command}. <p/> 
	 * Note that a {@link SubCommandGroup} can only be registered to <u>one</u> command at a time!<br/>
	 * Subsequent calls will return {@code false} if this {@link SubCommandGroup} is still the executor of the
	 * previous command it was registered to (unless it's the exact same command). Thus, to change the parent command
	 * of a {@link SubCommandGroup} that is already registered to another parent command the command it was previously
	 * registered to needs to have new {@link CommandExecutor} and {@link TabCompleter} set before calling this method.
	 * @param command the {@link PluginCommand} to register on.
	 * @param clearCommandUsage if this is {@code true} (<u>highly recommended</u>) the "Usage" of the {@link
	 *  PluginCommand} will be cleared upon successful command registration. This is to prevent this "Usage" message
	 *  from getting displayed whenever execution of a {@link SubCommand} fails. {@link SubCommandGroup} will handle
	 *  <u>all</u> messages on its own since each {@link SubCommand} requires a separate message!
	 * @param updateUsageHeader if {@code true} the usageHeader will be updated, otherwise it will only be invalidated.
	 * @return {@code false} if {@link PluginCommand command} is {@code null} or still registered to a previous command,
	 *  otherwise {@code true}.
	 */
	public boolean registerParentCommand(final PluginCommand command, final boolean clearCommandUsage, final boolean updateUsageHeader)
	{
		if(command == null)
			return false;
		else if(isRegisteredOrParentIsNot(command))
			return false; //We can't register on a _new_ command if we are still registered to a previous command
		parentCommand = command;
		command.setExecutor(this);
		command.setTabCompleter(this);
		if (clearCommandUsage)
			command.setUsage("");
		if (updateUsageHeader)
			updateUsageHeader();
		else
			invalidateUsageHeader();
		return true;
	}
	
	/**
	 * Get whether or not this {@link SubCommandGroup} is registered to a bukkit-{@link PluginCommand} or not.
	 * @return {@code true} if this {@link SubCommandGroup} currently is the {@link CommandExecutor} and / or
	 * {@link TabCompleter} for a {@link PluginCommand}, otherwise {@code false}.
	 * @see #registerParentCommand(PluginCommand, boolean, boolean)
	 */
	public final boolean isRegistered() {
		return isRegisteredOrParentIsNot(null);
	}
	
	/**
	 * @return {@code false} if not registered to a command or {@code command != null && parentCommand == command},
	 *  otherwise {@code true}.
	 */
	private boolean isRegisteredOrParentIsNot(final PluginCommand command)
	{
		return parentCommand != null && parentCommand != command
				&& (parentCommand.getExecutor() == this || parentCommand.getTabCompleter() == this);
	}
	
	/**
	 * Get the bukkit-{@link PluginCommand} this {@link SubCommandGroup} is registered to.
	 * @return {@code null} if {@link #isRegistered()} would return {@code false}, otherwise the
	 *  {@link PluginCommand} it is registered to.
	 *  @see #isRegistered()
	 */
	public final PluginCommand getRegisteredCommand() {
		return isRegisteredOrParentIsNot(null) ? parentCommand : null;
	}
	
	/**
	 * Get the name of the bukkit-{@link PluginCommand} this {@link SubCommandGroup} is registered to.
	 * @return a default "[UNREGISTERED]" string if {@link #isRegistered()} would return {@code false},
	 *  otherwise the name of the {@link PluginCommand} this {@link SubCommandGroup} is registered to.
	 *  @see #isRegistered()
	 */
	protected final String getParentName() {
		return isRegisteredOrParentIsNot(null) ? parentCommand.getName() : stringProvider.scg_unregistered();
	}
	
	/**
	 * Invalidates cached Strings for all {@link SubCommand SubCommands} in this {@link SubCommandGroup}.
	 * @see SubCommand#invalidateCachedStrings(boolean, boolean, boolean)
	 */
	public void invalidateAllSubCommandsCachedStrings(boolean invalidateUsage, boolean invalidateAlias, boolean invalidateDescription)
	{
		helpCommand.invalidateCachedStrings(invalidateUsage, invalidateAlias, invalidateDescription);
		for (SubCommand sc : commands)
			sc.invalidateCachedStrings(invalidateUsage, invalidateAlias, invalidateDescription);
	}
	
	/**
	 * Updates cached Strings for all {@link SubCommand SubCommands} in this {@link SubCommandGroup}.
	 * @see SubCommand#updateCachedStrings(boolean, boolean, boolean)
	 */
	public void updateAllSubCommandsCachedStrings(boolean updateUsage, boolean updateAlias, boolean updateDescription)
	{
		helpCommand.updateCachedStrings(updateUsage, updateAlias, updateDescription);
		for (SubCommand sc : commands)
			sc.updateCachedStrings(updateUsage, updateAlias, updateDescription);
	}
	
	/**
	 * Invalidates the usageHeader. (Forces it to be recreated the next time it is requested.)
	 */
	public void invalidateUsageHeader() {
		usageHeader1 = null;  usageHeader2 = null;
	}
	
	/**
	 * Updates the (cached) usageHeader* displayed above the list of {@link SubCommand SubCommands} and their
	 * usage-Strings. <p/>
	 * <p/><i>Note: The usageHeader (and the list of {@link SubCommand SubCommands}) gets displayed when calling
	 * the parent command without any parameters.</i>
	 * @return {@code false} if this {@link SubCommandGroup} hasn't been registered to a parent-command yet,
	 *  in which case the usage header will be missing some info, otherwise {@code true}.
	 * @see #isRegistered()
	 * @see #onCommand(CommandSender, Command, String, String[])
	 */
	public boolean updateUsageHeader()
	{
		final PluginCommand parentCommand = getRegisteredCommand();
		
		final String com = stringProvider.scg_usageHeaderCommand();
		
		if(parentCommand == null) //usageHeader2
			usageHeader2 = null;
		else
		{
			List<String> aliases = parentCommand.getAliases();
			if (aliases != null && aliases.size() > 0)
			{
				final String com2 = com.substring(0, 1).toUpperCase(SCStatics.locale) + com.substring(1); //First letter upper-case
				final String ali = stringProvider.sc_aliasesPrefix();
				usageHeader2 = SCStatics.mUsage + com2 + ":" + SCStatics.RESET_SPACE
						+ SCStatics.mParent + parentCommand.getName() + ChatColor.RESET + SCStatics.mText + "  -  "
						+ SCStatics.mAliasPre + (ali == null ? SubCommand.MSG_ALIAS : ali);
				for (String a : aliases)
					usageHeader2 += SCStatics.RESET_SPACE + SCStatics.mAlias + a;
			}
			else
				usageHeader2 = null;
		}
		
		final String
			key = stringProvider.scg_usageHeaderKey(),
			req = stringProvider.scg_usageHeaderRequired(),
			opt = stringProvider.scg_usageHeaderOptional();
		usageHeader1 = SCStatics.mText
				+ (parentCommand == null ? stringProvider.scg_unregistered()
						: parentCommand.getPlugin().getDescription().getFullName())
				+ "  -  " + key + ":" + SCStatics.RESET_SPACE
				+ SCStatics.mName + com + SCStatics.RESET_SPACE
				+ SCStatics.mRequired + SCStatics.uREQ1 + req
				+ SCStatics.uREQ2 + SCStatics.RESET_SPACE
				+ SCStatics.mOptional + SCStatics.uOPT1 + opt
				+ SCStatics.uOPT2;
		
		return parentCommand != null;
	}
	
	// ----------
	
	/**
	 * Does a case-insensitive check to verify that there would be no collisions between command names and then initializes them. 
	 * <p/><b><i>Must be called to initialize the command-names!<br>Must be called again to update the name-cache if any of the
	 * command-name/-aliases change!</i></b>
	 * <p/>If the name check fails {@link CommandSender sender} will be notified (unless {@code sender == null}).
	 * <p/><i>Note1: If the name check fails no name initialization is performed and the name cache is not updated!
	 * <p/>Note2: Name initialization invalidates cached usage Strings (if needed) and alias Strings (always).</i>
	 * @param sender {@link CommandSender} to send messages to, or {@code null} if silent operation is desired.
	 * @param enforceAliasUniqueness if this is {@code true} aliases are treated the same as command-names and must all be unique,
	 *  if this is {@code false} aliases name collisions are resolved by removing the / one of the colliding aliases. (<i>When an
	 *  alias collides with a command-name the alias is removed. When an alias collides with another alias the command with the
	 *  most aliases loses the colliding alias - if they both have the same number of aliases the lower-indexed command gets to
	 *  keep its alias, on the principle that more frequently used commands <u>should</u> be added to the {@link SubCommandGroup}
	 *  first.</i>)
	 * @return {@code true} if the {@link SubCommand SubCommands} had their names and aliases initialized, otherwise {@code false}.
	 * @see SubCommand#initializeNames()
	 */
	public final boolean checkAndInitializeNames(final CommandSender sender, final boolean enforceAliasUniqueness)
	{
		class B
		{
			B(SubCommand sc, String name, String[] aliases)
				{ s = sc;  n = name;  a = aliases; }
			final SubCommand s;
			final String n; //name (in lowercase!)
			final String[] a; //aliases array
			int v = 0; //valid entries in aliases array
		}
		class C
		{
			C(B b, int ai)
				{ this.b = b;  this.ai = ai; }
			final B b;
			final int ai; //alias index (name == -1)
		}
		final B[] bArr = new B[commands.size() + 1];
		final HashMap<String, C> scNames = new HashMap<String, C>(commands.size() * 3); //Room for name + 2 aliases
		final String parentName = getParentName();
		B b; C c; String n;
		
		//Add help-command!
		n = MSG_HLP_NAME.toLowerCase(SCStatics.locale);
		b = new B(helpCommand, n, helpCommand.getAliasesInternal());
		bArr[commands.size()] = b;
		c = new C(b, -1);
		scNames.put(n, c);
		
		//Check names
		for (int i = 0; i < commands.size(); ++i)
		{
			final SubCommand sc = commands.get(i);
			n = sc.getNameInternal();
			if (n == null || n.length() == 0) {
				if (sender != null) {
					final String s = stringProvider.scg_verifyNullName(parentName);
					sender.sendMessage(s);
				}
				return false;
			}
			n = n.toLowerCase(SCStatics.locale);
			b = new B(sc, n, sc.getAliasesInternal());
			c = new C(b, -1);
			c = scNames.put(n, c);
			if (c != null) //"name" was occupied!!
			{
				if (sender != null) {
					final String s = stringProvider.scg_verifyDuplicateName(parentName, n);
					sender.sendMessage(s);
				}
				return false;
			}
			bArr[i] = b;
		}
		
		//Check aliases - remove illegal/null and initialize b.v for all commands
		for (int i = 0; i < bArr.length; ++i)
		{
			b = bArr[i];
			if (b.a != null) {
				b.v = b.a.length;
				for (int k = 0; k < b.v;)
				{
					final String a = b.a[k];
					if(a == null || a.length() == 0) //Illegal/Null alias detected - remove!
					{
						b.a[k] = b.a[--b.v]; // <-- See SubCommand.initializeNames() for code comments
						if (sender != null) {
							final String s = stringProvider.scg_verifyNullAlias(parentName, b.n);
							sender.sendMessage(s);
						}
					}
					else
						++k; //nothing changed in b --> don't forget to move along!!!
				}
			}
		}
		
		//Check aliases - check collisions
		for (int i = 0; i < bArr.length; ++i)
		{
			b = bArr[i];
			for (int k = 0; k < b.v;)
			{
				final String a = b.a[k];
				c = new C(b, k);
				c = scNames.put(a, c);
				if (c != null) //alias occupied by other name/alias !!
				{
					if (enforceAliasUniqueness)
					{
						if (sender != null) {
							if (c.ai == -1) { //command "b.n" wants alias "a" but there exists a command named "a"
								final String s = stringProvider.scg_verifyAliasName(parentName, b.n, a, enforceAliasUniqueness);
								sender.sendMessage(s);
							}
							else { //commands "b.n" and "c.b.n" both wants alias "a"
								final String s = stringProvider.scg_verifyAliasAlias(parentName, b.n, c.b.n, a, enforceAliasUniqueness);
								sender.sendMessage(s);
							}
						}
						return false;
					}
					else
					{
						if (c.ai == -1) //command "b.n" wants alias "a" but there exists a command named "a"
						{
							scNames.put(c.b.n, c); //first: put back the command-name!
							b.a[k] = b.a[--b.v]; //second: punish that pompous alias, sentence: death! >:c
							if (sender != null) { //(as above) + alias "a" removed from "b.n" //<-Skipped (reason: implied)
								final String s = stringProvider.scg_verifyAliasName(parentName, b.n, a, enforceAliasUniqueness);
								if (s != null)
									sender.sendMessage(s);
							}
						}
						else //commands "b.n" and "c.b.n" both wants alias "a"
						{
							//resolution: the one with the fewest aliases win!
							//if both have the same number of aliases the command with the lowest index wins
							//...(by the principle that important commands should be added first --> lower index == more important)
							if (c.b.v > b.v) //lower index command has more aliases!
							{
								c.b.a[c.ai] = c.b.a[--c.b.v];
								if (sender != null) { //(as above) + alias "a" removed from "c.b.n"
									String s = stringProvider.scg_verifyAliasAlias(parentName, b.n, c.b.n, a, enforceAliasUniqueness);
									if (s != null) {
										sender.sendMessage(s);
										s = stringProvider.scg_verifyAliasRemoved(parentName, c.b.n, a);
										if (s != null)
											sender.sendMessage(s);
									}
								}
								++k; //nothing changed in b --> don't forget to move along!!!
							}
							else
							{
								scNames.put(c.b.n, c); //first: put back the lower indexed commands alias
								b.a[k] = b.a[--b.v]; //second: remove alias from b
								if (sender != null) { //(as above) + alias "a" removed from "b.n"
									String s = stringProvider.scg_verifyAliasAlias(parentName, b.n, c.b.n, a, enforceAliasUniqueness);
									if (s != null) {
										sender.sendMessage(s);
										s = stringProvider.scg_verifyAliasRemoved(parentName, b.n, a);
										if (s != null)
											sender.sendMessage(s);
									}
								}
							}
						}
					}
				}
				else //(no collision)
					++k; //nothing changed in b --> don't forget to move along!
			}
		}
		
		//Initialize all names and (remaining) aliases
		for (int i = 0; i < bArr.length; ++i)
		{
			b = bArr[i];
			b.s.initializeNamesInternal(b.n, b.a, b.v);
		}
		
		return true;
	}
	
	// ----------
	
	/**
	 * Adds a {@link SubCommand} to this {@link SubCommandGroup}. (NOT Thread safe)<p/>
	 * <i>The order in which commands are added is preserved, and it is recommended to add frequently used commands
	 * first, and more rarely used commands last.</i>
	 * @param subCommand the {@link SubCommand} to add.
	 * @throws IllegalArgumentException if {@code subCommand} is {@code null}.
	 */
	public final void addCommand(final SubCommand subCommand)
	{
		if (subCommand == null)
			throw new IllegalArgumentException("Can't add null as a SubCommand!");
		commands.add(subCommand);
	}
	
	/**
	 * Search for a {@link SubCommand} with {@code commandName}, remove it and return the removed {@link SubCommand}. 
	 * <br>(NOT Thread safe)<p/><i>This is a stable operation - the order of the commands are preserved.</i> 
	 * @param commandName name of the {@link SubCommand} to remove. (Case insensitive!)
	 * @return The removed {@link SubCommand} or null if no such {@link SubCommand} was found.
	 */
	public final SubCommand removeCommand(String commandName)
	{
		int idx = commands.size() - 1;
		if (idx < 0)
			return null;
		commandName = commandName == null ? null : commandName.toLowerCase(SCStatics.locale);
		for (; idx >= 0; --idx) {
			final SubCommand sc = commands.get(idx);
			final String s = sc.getName();
			if (s == commandName || (s != null && s.equals(commandName))) { //The "==" is basically for the null == null case
				commands.remove(idx);
				return sc;
			}
		}
		return null;
	}
	
	// ----------
	
	/**
	 * Get the number of {@link SubCommand SubCommands} in this {@link SubCommandGroup} (excluding the help-command).
	 */
	public final int getCount() {
		return commands.size();
	}
	
	// ----------
	
	/**
	 * Adds an {@link HelpTopic} to this {@link SubCommandGroup SubCommandGroups} help-command. <p/>
	 * <i>Note: {@link HelpTopic HelpTopics} are not checked for duplicates or name-collisions.</i>
	 * @param helpTopic a non-{@code null} {@link HelpTopic} (Will throw if {@code null}.)
	 */
	public final void addHelpTopic(final HelpTopic helpTopic)
	{
		if (helpTopic == null)
			throw new IllegalArgumentException("Can't add null as a IHelpTopic!");
		helpCommand.getOrCreateHTList().add(helpTopic);
	}
	
	/**
	 * Search for an {@link HelpTopic} with {@code helpTopicName}, remove it and return the removed {@link HelpTopic}. 
	 * @param helpTopicName name of the {@link HelpTopic} to remove. (Case insensitive!)
	 * @return The removed {@link HelpTopic} or null if no such {@link HelpTopic} was found.
	 */
	public final HelpTopic removeHelpTopic(String helpTopicName)
	{
		final ArrayList<HelpTopic> htArr = helpCommand.helpTopics;
		if (htArr == null)
			return null;
		int idx = htArr.size() - 1;
		if (idx < 0)
			return null;
		helpTopicName = helpTopicName == null ? null : helpTopicName.toLowerCase(SCStatics.locale);
		for (; idx >= 0; --idx) {
			final HelpTopic ht = htArr.get(idx);
			final String s = ht.getName() == null ? null : ht.getName().toLowerCase(SCStatics.locale);
			if (s == helpTopicName || (s != null && s.equals(helpTopicName))) {
				htArr.remove(idx);
				return ht;
			}
		}
		return null;
	}
	
	// ----------
	
	//REMOVED: Initialization works a bit better now (inside checkAndInitialize)
//	/**
//	 * Calls {@link SubCommand#initializeNames()} on all {@link SubCommands} in this {@link SubCommandGroup}. 
//	 * <p/><i>Note: Should only be called from inside {@link #checkAndInitializeNames(CommandSender)}, i.e.
//	 * <u>it should only be called after it is confirmed that there are no name-collisions!</u></i>
//	 */
//	protected final void initializeAllNames()
//	{
//		helpCommand.initializeNames();
//		for (SubCommand sc : commands)
//			sc.initializeNames();
//	}
	
	//REMOVED: use updateAllSubCommansCachedStrings instead
//	/**
//	 * (Re-)Caches the usage-Strings of all {@link SubCommand SubCommands} of this {@link SubCommandGroup}.
//	 * @return Returns the number of usage-Strings that failed to cache - Zero if all usage-Strings where cached successfully.
//	 *  (<i>Usage-String generation fails if a {@link SubCommand} has not been initialized.</i>)
//	 * @see SubCommand#isInitialized()
//	 * @see #updateAllSubCommandsAliasStrings()
//	 */
//	public int updateAllSubCommandsUsageStrings()
//	{
//		int failCount;
//		if(!helpCommand.updateUsageStrings())
//			failCount = 1;
//		else
//			failCount = 0;
//		for (SubCommand sc : commands)
//			if(!sc.updateUsageStrings())
//				++failCount;
//		return failCount;
//	}
	
	//REMOVED: use updateAllSubCommansCachedStrings instead
//	/**
//	 * (Re-)Caches the alias-Strings of all {@link SubCommand SubCommands} of this {@link SubCommandGroup}.
//	 * @see #updateAllSubCommandsUsageStrings()
//	 */
//	public void updateAllSubCommandsAliasStrings()
//	{
//		final String s = msgProvider.scg_aliasesPrefix();
//		helpCommand.updateAliasString(s);
//		for (SubCommand sc : commands)
//			sc.updateAliasString(s);
//	}
	
	// ----------
	
	/**
	 * Returns the {@link SubCommand} that matches cmdName or null if no match found.
	 */
	protected SubCommand stringToCommand(String cmdName)
	{
		cmdName = cmdName.toLowerCase(SCStatics.locale);
		if (cmdName.equals(helpCommand.getName()))
			return helpCommand;
		for(SubCommand sc : commands)
			if (cmdName.equals(sc.getName()))
				return sc;
		for(SubCommand sc : commands)
			if (sc.getAliases() != null)
				for (String a : sc.getAliases())
					if (cmdName.equals(a))
						return sc;
		return null;
	}
	
	/**
	 * Gets a tab-list matching the argument {@code arg} against the name (and aliases) of all the
	 * {@link SubCommand SubCommands} of this {@link SubCommandGroup}.
	 */
	protected ArrayList<String> tabHelper(final CommandSender sender, String arg)
	{
		ArrayList<String> list = new ArrayList<String>();
		arg = arg.toLowerCase(SCStatics.locale);
		
		for(SubCommand sc : commands) {
			if(sc.hasPermission(sender)) {
				String[] aArr;
				if(sc.getName().startsWith(arg))
					list.add(sc.getName());
				else if((aArr = sc.getAliases()) != null)
					for(String a : aArr)
						if(a != null && a.startsWith(arg))
							list.add(sc.getName());
			}
		}
		if (list.size() == 0)
			return null;
		else {
			list.add(helpCommand.getName()); //weird way of hinting at the existence of the help command...
			return list;
		}
	}
	
	/**
	 * Shows a short usage description of {@link SubCommand command} for {@link CommandSender sender}.
	 * @param label The parent command alias used by the {@code sender}.
	 */
	protected void showCmdUsage(final CommandSender sender, final SubCommand command, final String label) {
		final String s = stringProvider.scg_usagePrefix();
		sender.sendMessage(SCStatics.mUsage + s	+ SCStatics.RESET_SPACE
				+ command.getUsageFormated(label, sender instanceof Player));
	}
	
	/**
	 * Shows usage-Strings for all {@link SubCommand SubCommands} that the {@link CommandSender} has permission to
	 * use, including the help-command, together with the owning plugins name and version, parent command name and
	 * aliases (if it has any aliases), and a key to understanding the parameter-syntax. 
	 * <p/><i>Note: Nothing is shown unless the {@link CommandSender} has permission to use at least 1 of the
	 * {@link SubCommand SubCommands} from this {@link SubCommandGroup} (help-command doesn't count).</i>
	 * @param sender {@link CommandSender} to show it for
	 * @param label alias/name that was used to invoke the parent {@link Command}
	 * @return The number of {@link SubCommand SubCommands} shown (including the help-command). Will be zero if the
	 * {@link CommandSender} didn't have permission for any of the {@link SubCommand SubCommands}.
	 */
	protected int showAllCmdUsage(final CommandSender sender, final String label)
	{
		final boolean isPlayer = sender instanceof Player;
		int count = 0;
		for(SubCommand sc : commands) {
			if(sc.hasPermission(sender)) {
				if (count == 0) { 	//Print the "key" & help-command
					if (usageHeader1 == null)
						updateUsageHeader();
					sender.sendMessage(usageHeader1);
					if (usageHeader2 != null)
						sender.sendMessage(usageHeader2);
					sender.sendMessage(helpCommand.getUsageFormated(label, isPlayer));
					++count;
				}
				sender.sendMessage(sc.getUsageFormated(label, isPlayer));
				++count;
			}
		}
		return count;
	}
	
	// =============
	
	/**
	 * Handles command-execution. <p/>
	 * - If the provided {@link Command} doesn't match the parent-command that this {@link SubCommandGroup} is registered 
	 * to this will fail.<br>
	 * - Else if the provided argument {@code String[]} is empty it will print out a list of all
	 * {@link SubCommand SubCommands} that the provided {@link CommandSender} has permission to view together with the
	 * owning plugins name and version, parent command name and aliases if it has any, and a key to understanding the
	 * parameter-syntax.
	 * Unless the {@code sender} doesn't have permission to view <i>any</i> of the {@link SubCommand SubCommands} in which
	 * case {@link IStringProvider#scg_unavailable(String)} will be displayed.<br>
	 * - Else if the provided arguments doesn't match any {@link SubCommand} or matches a {@link SubCommand} that the 
	 * {@code sender} doesn't have permission to use it will also display {@link IStringProvider#scg_unavailable(String)}.
	 * <br>- Else if the arguments matches a {@link SubCommand}, but the number of arguments is outside the min-max range of
	 * allowed arguments to that {@link SubCommand}, {@link IStringProvider#scg_incorrectNumArgs()} and the usage-String for
	 * that {@link SubCommand} will be displayed.<br>
	 * - Else it will execute {@link SubCommand#onCommand(CommandSender, String[], String)} - if that fails (i.e. returns
	 * {@code false}) it will show the usage-String for that {@link SubCommand}.
	 * @return {@code true} if a valid {@link SubCommand} that was executed successfully, otherwise {@code false}.
	 * @see JavaPlugin#onCommand(CommandSender, Command, String, String[]) JavaPlugin.onCommand(...) for explanaition of parameters
	 */
	@Override
	public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args)
	{
		if(command != parentCommand)
			return false;
		
		if(args.length == 0) {
			if (0 == showAllCmdUsage(sender, label)) //If the sender doesn't have permission for any subcommands then...
			{
				String s = stringProvider.scg_unavailable(label); //...essentially the sender doesn't have permission for the parent
				if (s == null)
					return false;
				else
					sender.sendMessage(SCStatics.mText + s);
			}
			return false;
		}
		
		SubCommand cmd = stringToCommand(args[0]);
		
		if(cmd == null)
		{
			String s = stringProvider.scg_unavailable(args[0]);
			if (s != null)
				sender.sendMessage(SCStatics.mText + s);
			return false;
		}
		
		if(!cmd.hasPermission(sender)) //Don't reveal existence of commands you don't have permission for!
		{
			String s = stringProvider.scg_unavailable(args[0]);
			if (s != null)
				sender.sendMessage(SCStatics.mText + s);
			return false;
		}
		
		if(args.length < cmd.minArgs || args.length > cmd.maxArgs)
		{
			String s = stringProvider.scg_incorrectNumArgs();
			if (s != null)
				sender.sendMessage(SCStatics.mText + s);
			showCmdUsage(sender, cmd, label);
			return false;
		}
		
		if (cmd.onCommand(sender, args, label))
			return true;
		else
		{
			showCmdUsage(sender, cmd, label);
			return false;
		}
	}
	
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args)
	{
		if(command != parentCommand)
			return null;
		
		if(args.length == 1)
			return tabHelper(sender, args[0]);
		
		SubCommand cmd = stringToCommand(args[0]);
		if(cmd == null)
			return null;
		
		if(args.length < cmd.minArgs || args.length > cmd.maxArgs)
			return null;
		
		if(!cmd.hasPermission(sender))
			return null;
		
		return cmd.onTabComplete(sender, args);
	}
	
	
	// =======================
	// =======================
	
	
	/**
	 * Automatic Help-(Sub)Command!
	 * @see SubCommandGroup
	 * @author AnorZaken
	 */
	private final class HelpCommand extends SubCommand
	{
		private HelpCommand()
		{
			super( 2, 2, null );
		}
		//	super( MIN_ARGS, MAX_ARGS, PERMISSION );
		//--------------------------------------------
		
		private ArrayList<HelpTopic> helpTopics = null;
		private ArrayList<HelpTopic> getOrCreateHTList() {
			return helpTopics == null ? (helpTopics = new ArrayList<HelpTopic>(1)) : helpTopics;
		}
		
		/**
		 * Returns the {@link HelpTopic} that matches {@code name} or null if no match found.
		 */
		private HelpTopic stringToHT(String name)
		{
			final ArrayList<HelpTopic> htArr = helpTopics;
			if (htArr == null)
				return null;
			
			name = name.toLowerCase(SCStatics.locale);
			for(HelpTopic eht : htArr)
				if (name.equals(eht.getName().toLowerCase(SCStatics.locale)))
					return eht;
				
			return null;
		}
		
		/**
		 * Gets a tab-list matching the argument {@code arg} against the
		 * names of all the {@link HelpTopic IHelpTopics} of this {@link HelpCommand}.
		 */
		private ArrayList<String> tabHelper(final CommandSender sender, String arg, ArrayList<String> list)
		{
			final ArrayList<HelpTopic> htArr = helpTopics;
			if (htArr == null)
				return list;
			
			arg = arg.toLowerCase(SCStatics.locale);
			if (list == null)
				list = new ArrayList<String>();
			
			for(HelpTopic eht : htArr) {
				if(eht.canSee(sender)) {
					String n = eht.getName().toLowerCase(SCStatics.locale);
					if(n.startsWith(arg))
						list.add(n);
				}
			}
			return list.size() > 0? list : null;
		}
		
		/**
		 * Shows the chat-formated aliases for a {@link SubCommand}. <br>(Only shown if the command actually has aliases
		 * and {@link IStringProvider#sc_aliasesPrefix()} {@code != null})
		 */
		private void showCmdAliases(final CommandSender sender, final SubCommand cmd)
		{
			String s = cmd.getAliasesFormated();
			if (s != null)
				sender.sendMessage(s);
		}
		
		// -----

		@Override
		protected String getNameInternal() {
			return MSG_HLP_NAME;
		}

		private final String[] aliArr = new String[1]; //Used to reduce gc-load in getAliasesInternal()
		
		@Override
		protected String[] getAliasesInternal()
		{
			final String s = SubCommandGroup.this.stringProvider.scg_help_alias();
			if (s == null)
				return null;
			aliArr[0] = s;
			return aliArr;
		}

		@Override
		protected String getUsageParameters() {
			final String s = SubCommandGroup.this.stringProvider.scg_help_usageParameters();
			return s;
		}

		@Override
		public String getDescriptionInternal() {
			final String s = SubCommandGroup.this.stringProvider.scg_help_description();
			return SCStatics.mText + s;
		}

		@Override
		public boolean onCommand(final CommandSender sender, String[] args, final String label)
		{
			final SubCommand cmd = stringToCommand(args[1]);
			String s;
			if (cmd != null)
			{
				if (cmd == this) {
					sender.sendMessage(getDescriptionFormated());
					return true;
				} else if (!cmd.hasPermission(sender)) {
					s = SubCommandGroup.this.stringProvider.scg_help_sorry(args[1]);
					if (s != null)
						sender.sendMessage(SCStatics.mText + s);  //Don't give help for commands you can't use!
					return true;
				}// else
				showCmdUsage(sender, cmd, label);
				showCmdAliases(sender, cmd);
			}
			
			final HelpTopic eht;
			if (cmd == null) {
				if ((eht = stringToHT(args[1])) == null) {
					s = SubCommandGroup.this.stringProvider.scg_help_sorry(args[1]);
					if (s != null)
						sender.sendMessage(SCStatics.mText + s);
					return true;
				}
				else if (!eht.canSee(sender)) {
					s = SubCommandGroup.this.stringProvider.scg_help_sorry(args[1]);
					if (s != null)
						sender.sendMessage(SCStatics.mText + s);  //Don't give help for commands you can't use!
					return true;
				}
				else
					s = eht.getDescriptionFormated();
			}
			else if ((eht = stringToHT(cmd.getName())) == null || (s = eht.getDescriptionFormated()) == null)
				s = cmd.getDescriptionFormated();
			
			if (s == null)
			{
				s = SubCommandGroup.this.stringProvider.scg_help_noDesc();
				if (s != null)
					sender.sendMessage(SCStatics.mDescFull + s);
			}
			else
				sender.sendMessage(s);
			
			return true;
		}

		@Override
		public List<String> onTabComplete(CommandSender sender, String[] args)
		{
			return tabHelper(sender, args[1], SubCommandGroup.this.tabHelper(sender, args[1]));
		}
	}
	
	// -----
}
