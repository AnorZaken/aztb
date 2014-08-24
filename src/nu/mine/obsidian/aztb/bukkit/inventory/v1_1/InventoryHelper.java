package nu.mine.obsidian.aztb.bukkit.inventory.v1_1;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.InventoryView;
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
 * @version 1.1
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
	
	
	/**
	 * Get the {@link SlotType} of an inventory slot.
	 * <p><i>Copyright (C) <a href="https://github.com/Bukkit">The Bukkit Project</a> (2014-08-24)</i>
	 * @param invView an {@link InventoryView}
	 * @param rawSlot the {@link InventoryView} (aka "raw") slot number to get {@link SlotType} for
	 * @return the {@link SlotType} of the {@link InventoryView} raw-slot
	 * @see <a href="https://github.com/Bukkit/CraftBukkit/blob/37c79691615fbd28b49c7371a64700e4f5713eca/src/main/java/org/bukkit/craftbukkit/inventory/CraftInventoryView.java">Original CraftBukkit Code!</a>
	 */
	public static SlotType getSlotType(InventoryView invView, int rawSlot)
	{
		SlotType type = SlotType.CONTAINER;
		if (rawSlot >= 0 && rawSlot < invView.getTopInventory().getSize()) {
			switch(invView.getType()) {
			case FURNACE:
				if (rawSlot == 2) {
					type = SlotType.RESULT;
				} else if(rawSlot == 1) {
					type = SlotType.FUEL;
				} else {
					type = SlotType.CRAFTING;
				}
				break;
			case BREWING:
				if (rawSlot == 3) {
					type = SlotType.FUEL;
				} else {
					type = SlotType.CRAFTING;
				}
				break;
			case ENCHANTING:
				type = SlotType.CRAFTING;
				break;
			case WORKBENCH:
			case CRAFTING:
				if (rawSlot == 0) {
					type = SlotType.RESULT;
				} else {
					type = SlotType.CRAFTING;
				}
				break;
			case MERCHANT:
				if (rawSlot == 2) {
					type = SlotType.RESULT;
				} else {
					type = SlotType.CRAFTING;
				}
				break;
			case BEACON:
				type = SlotType.CRAFTING;
				break;
			case ANVIL:
				if (rawSlot == 2) {
					type = SlotType.RESULT;
				} else {
					type = SlotType.CRAFTING;
				}
				break;
			default:
				// Nothing to do, it's a CONTAINER slot
			}
		} else {
			if (rawSlot == -999) {
				type = SlotType.OUTSIDE;
			} else if (invView.getType() == InventoryType.CRAFTING) {
				if (rawSlot < 9) {
					type = SlotType.ARMOR;
				} else if (rawSlot > 35) {
					type = SlotType.QUICKBAR;
				}
			} else if (rawSlot >= (invView.countSlots() - 9)) {
				type = SlotType.QUICKBAR;
			}
		}
		return type;
	}
}
