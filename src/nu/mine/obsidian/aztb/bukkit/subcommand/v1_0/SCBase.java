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

/**
 * Abstract base-class for {@link SubCommand} and {@link HelpTopic}.
 * @author AnorZaken
 * @version 1.0
 */
abstract class SCBase
{
	protected SCBase() {}
	
	String descFormated = null; //cached chat-formated description string
	
	// ------
	
	/**
	 * Should return a human readable description of what the command does and how to use it.
	 * @return a {@link String} containing the description for this command, or {@code null} (no description).
	 */
	protected abstract String getDescriptionInternal(); //TODO: javadoc: mention raw and formated stuff
	
	/**
	 * Get a chat-formated description for this object. (Thread safe)
	 * <p/><i>Note: The description-String is created and formated the first time it is requested, then it gets cached.</i>
	 */
	public final String getDescriptionFormated()
	{
		String descFormated = this.descFormated;
		if (descFormated == null)
			descFormated = updateDescriptionStringInternal();
		return descFormated;
	}
	
	/**
	 * Invalidates the cached description-String for this object. (Thread safe)<br>
	 * This will cause it to be updated the next time it gets requested.
	 */
	public final void invalidateDescription() {
		descFormated = null;
	}
	
	/**
	 * Updates the cached description-String for this object. (Thread safe)
	 */
	public final void updateDescriptionString()
	{
		updateDescriptionStringInternal();
	}
	
	/**
	 * Updates the cached description-String for this object <i>and</i> returns it. (Thread safe)
	 * @return {@code null} if this object doesn't have a description, otherwise the chat-formated description.
	 */
	protected final String updateDescriptionStringInternal()
	{
		final String s = getDescriptionInternal();
		if (s == null || s.length() == 0) {
			descFormated = null;
			return null;
		} else {
			StringBuilder sb = new StringBuilder(s.length());
			SCStatics.formatDescriptionString(s, sb);
			final String d = sb.toString();
			descFormated = d;
			return d;
		}
	}
}
