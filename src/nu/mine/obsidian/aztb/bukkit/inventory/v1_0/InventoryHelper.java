package nu.mine.obsidian.aztb.bukkit.inventory.v1_0;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

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
 * Tool class for inventory manipulation.
 * @author AnorZaken
 * @version 1.0
 */
public class InventoryHelper
{
	/**
	 * Updates a player inventory (1-tick delay).
	 */
	public static void updatePlayerInventory(final JavaPlugin plugin, final Player player)
	{
		if(plugin == null)
			throw new IllegalArgumentException("plugin == null");
		if(player == null)
			throw new IllegalArgumentException("player == null");
		
		player.getServer().getScheduler().runTask(plugin, new Runnable() {
				@SuppressWarnings("deprecation")
				@Override public void run() {
					if(player.isOnline())
						player.updateInventory();
				}
			});
	}
}
