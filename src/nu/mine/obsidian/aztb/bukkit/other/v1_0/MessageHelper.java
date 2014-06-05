package nu.mine.obsidian.aztb.bukkit.other.v1_0;

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

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Tiny helper-class for printing Bukkit info, warning and error messages (to multiple targets).
 * @author AnorZaken
 * @version 1.0
 */
public class MessageHelper
{
	final public static MessageHelper staticInstance = new MessageHelper();
	
	public String prefixTag = "";
	public ChatColor prefixColorInfo = ChatColor.GRAY;
	public ChatColor prefixColorWarn = ChatColor.YELLOW;
	public ChatColor prefixColorSevere = ChatColor.RED;
	public String prefixInfo = "Info: ";
	public String prefixWarn = "Warning: ";
	public String prefixSevere = "ERROR: ";
	
	public MessageHelper()
	{}
	
	
	// -------- Static methods --------
	
	public static void infoS(final String text, final CommandSender... recivers) {
		staticInstance.info(text, recivers);
	}
	
	public static void warningS(final String text, final CommandSender... recivers) {
		staticInstance.warning(text, recivers);
	}
	
	public static void severeS(final String text, final CommandSender... recivers) {
		staticInstance.severe(text, recivers);
	}
	
	
	// -------- Instance methods --------
	
	public void info(final String text, final CommandSender... recivers)
	{
		if (text == null || recivers == null)
			return;
		else
		{
			String cs = null, ps = null;
			for (CommandSender reciver : recivers)
			{
				if (reciver instanceof Player)
					reciver.sendMessage(ps == null ? ps = prefixTag + prefixColorInfo + prefixInfo + ChatColor.RESET + text : ps);
				else
					Bukkit.getServer().getLogger().info(cs == null ? cs = prefixTag + text : cs);
			}
		}
	}
	
	public void warning(final String text, final CommandSender... recivers)
	{
		if (text == null || recivers == null)
			return;
		else
		{
			String cs = null, ps = null;
			for (CommandSender reciver : recivers)
			{
				if (reciver instanceof Player)
					reciver.sendMessage(ps == null ? ps = prefixTag + prefixColorWarn + prefixWarn + ChatColor.RESET + text : ps);
				else
					Bukkit.getServer().getLogger().warning(cs == null ? cs = prefixTag + text : cs);
			}
		}
	}
	
	public void severe(final String text, final CommandSender... recivers)
	{
		if (text == null || recivers == null)
			return;
		else
		{
			String cs = null, ps = null;
			for (CommandSender reciver : recivers)
			{
				if (reciver instanceof Player)
					reciver.sendMessage(ps == null ? ps = prefixTag + prefixColorSevere + prefixSevere + ChatColor.RESET + text : ps);
				else
					Bukkit.getServer().getLogger().severe(cs == null ? cs = prefixTag + text : cs);
			}
		}
	}
}
