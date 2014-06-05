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

import org.bukkit.ChatColor;

/**
 * Support class for managing text formating inside the SubCommand-framework.
 * @author AnorZaken
 * @version 1.0
 */
public class Markup
{
	private ChatColor color = ChatColor.WHITE;
	private boolean bold = false;
	private boolean italics = false;
	private boolean underline = false;
	protected String toString = null;
	
	/**
	 * Create a {@link Markup} with plain white text.
	 */
	public Markup()
	{
		toString = color.toString();
	}
	
	/**
	 * Create a {@link Markup} with the specified {@link ChatColor}.
	 * @param color A few values are not accepted, see {@link #color(ChatColor)}.
	 * @see #color(ChatColor)
	 */
	public Markup(final ChatColor color)
	{
		color(color);
		buildString();
	}
	
	/**
	 * Create a {@link Markup} with the specified {@link ChatColor} and other settings.
	 * @param color A few values are not accepted, see {@link #color(ChatColor)}.
	 * @see #color(ChatColor)
	 */
	public Markup(final ChatColor color, final boolean bold, final boolean italics, final boolean underline)
	{
		color(color);
		bold(bold);
		italics(italics);
		underline(underline);
		buildString();
	}
	
	// -----
	
	public boolean bold() {
		return bold;
	}
	public void bold(boolean bold) {
		if (this.bold != bold) {
			this.bold = bold;
			toString = null;
		}
	}
	
	public boolean italics() {
		return italics;
	}
	public void italics(boolean italics) {
		if (this.italics != italics) {
			this.italics = italics;
			toString = null;
		}
	}
	
	public boolean underline() {
		return underline;
	}
	public void underline(boolean underline) {
		if (this.underline != underline) {
			this.underline = underline;
			toString = null;
		}
	}
	
	/**
	 * Get the color of this {@link Markup}. <p/><i>Note: This will always only return a color!<br>
	 * Use {@link #bold()}, {@link #italics()} and {@link #underline()} to get other formating.</i>
	 */
	public ChatColor color() {
		return color;
	}
	/**
	 * Set the color (or other formating) of this {@link Markup}.
	 * @param color Any {@link ChatColor} ({@link ChatColor#RESET RESET} and 
	 * {@link ChatColor#STRIKETHROUGH STRIKETHROUGH} are not supported and will be ignored.)
	 * @return {@code true} if the specified {@link ChatColor} is supported by {@link Markup}, otherwise {@code false}.
	 */
	public boolean color(ChatColor color)
	{
		if (color == null) {
			return false;
		}else if (color == ChatColor.BOLD) {
			bold(true);
			return true;
		}else if (color == ChatColor.ITALIC) {
			italics(true);
			return true;
		}else if (color == ChatColor.UNDERLINE) {
			underline(true);
			return true;
		}else if ( color == ChatColor.RESET || color == ChatColor.STRIKETHROUGH ) {
			return false;
		}else if ( color != this.color ) {
			this.color = color;
			toString = null;
			return true;
		} else
			return true;
	}
	
	/**
	 * Returns the chat string that will yield the effect of this {@link Markup}.
	 */
	@Override
	public String toString()
	{
		String s = toString;
		return s == null ? buildString() : s;
	}
	
	// -----
	
	/**
	 * Builds the {@code toString}-String, saves it, and also returns it for convenience.
	 */
	protected String buildString()
	{
		if (bold) {
			if (italics) {
				if (underline)
					return toString = color.toString() + ChatColor.BOLD + ChatColor.ITALIC + ChatColor.UNDERLINE;
				else
					return toString = color.toString() + ChatColor.BOLD + ChatColor.ITALIC;
			}
			else {
				if (underline)
					return toString = color.toString() + ChatColor.BOLD + ChatColor.UNDERLINE;
				else
					return toString = color.toString() + ChatColor.BOLD;
			}
		}
		else {
			if (italics) {
				if (underline)
					return toString = color.toString() + ChatColor.ITALIC + ChatColor.UNDERLINE;
				else
					return toString = color.toString() + ChatColor.ITALIC;
			}
			else {
				if (underline)
					return toString = color.toString() + ChatColor.UNDERLINE;
				else
					return toString = color.toString();
			}
		}
	}
}