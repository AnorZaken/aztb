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

import java.util.Locale;


import org.bukkit.command.CommandSender;

/**
 * Extra help-topic to be used by the help-command in a {@link SubCommandGroup}. <p/>
 * {@link HelpTopic HelpTopics} have higher priority than {@link SubCommand SubCommands}, so if one is
 * provided with the same name as a command it will override the help for that command.
 * @author AnorZaken
 * @version 1.0
 * 
 * @see SubCommandGroup#addHelpTopic(HelpTopic)
 */
public abstract class HelpTopic extends SCBase
{
	protected final String permission; //SHOLD ONLY BE USED by canSee(CommandSender) !!
	
	protected HelpTopic(final String permission) {
		this.permission = permission;
	}
	
	// ------
	
	/**
	 * Get the name of this {@link HelpTopic}. 
	 * <p/><i>Note: The name will be converted to lowercase by {@link #getName()} with the {@link Locale}
	 * that can be specified with {@link SCStatics#setLocale(Locale)}. Thus {@link #getNameInternal()}
	 * should not bother with formating the case in any way.</i>
	 * @return The name of this {@link HelpTopic}. <i><b>MUST BE NON-NULL!</b></i>
	 */
	protected abstract String getNameInternal();
	
	/**
	 * Get the name of this {@link HelpTopic}. 
	 * <p/><i>Note1: Name will <u>always</u> be converted to lowercase with the {@link Locale}
	 * that can be specified with {@link SCStatics#setLocale(Locale)}!
	 * <p/>Note2: HelpTopic names are not cached so unlike the description they can't be invalidated.</i>
	 */
	public final String getName() {
		return getNameInternal().toLowerCase(SCStatics.locale);
	}
	
	/**
	 * Checks if {@link CommandSender sender} has permission to see this {@link HelpTopic}. <br>
	 * Used by {@link SubCommandGroup SubCommandGroups} help-command.
	 * <p/><i>Note1: By default this simply uses the permission-String specified in the {@link #HelpTopic(String)}
	 * constructor. Override this method if a more advanced permission check is required.
	 * <p/>Note2: If a {@link HelpTopic} overrides the description for a {@link SubCommand}
	 * (which it does if its {@link #getName()} returns the same name as one of the {@link SubCommand SubCommands}
	 * in the {@link SubCommandGroup} that this {@link HelpTopic} is added to) then the permission-check from the
	 * command will be used instead of this function!</i>
	 * @param sender The {@link CommandSender} to check permission for (NON-NULL!)
	 * @return {@code false} if the {@link CommandSender} doesn't have what it takes to see this {@link HelpTopic},
	 *  otherwise {@code true}.
	 */
	public boolean canSee(final CommandSender sender) {
		return permission == null || sender.hasPermission(permission);
	}
}