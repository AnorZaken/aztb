package nu.mine.obsidian.aztb.bukkit.furnace.v1_0;

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

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Furnace;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.FurnaceExtractEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Directional;
import org.bukkit.plugin.java.JavaPlugin;


/**
 * Tool class for Furnace manipulation. <br>
 * Can transparently* "unstuck" a burning furnace after canceling a {@link FurnaceBurnEvent}
 * <br><i>*transparently = any open inventory or items on cursor remains (visually) unaffected.</i>
 * @author AnorZaken
 * @version 1.0
 */
public class FurnaceHelper
{
	/**
	 * Get the {@link Furnace} from a Furnace-event.
	 */
	public static Furnace getFurnace(final FurnaceBurnEvent burnEvent) {
		return (Furnace) burnEvent.getBlock().getState();
	}
	
	/**
	 * Get the {@link Furnace} from a Furnace-event.
	 */
	public static Furnace getFurnace(final FurnaceSmeltEvent smeltEvent) {
		return (Furnace) smeltEvent.getBlock().getState();
	}
	
	/**
	 * Get the {@link Furnace} from a Furnace-event.
	 */
	public static Furnace getFurnace(final FurnaceExtractEvent extractEvent) {
		return (Furnace) extractEvent.getBlock().getState();
	}
	
	// --------------
	
	/**
	 * Cancels a {@link FurnaceBurnEvent} AND <u>makes sure the Furnace doesn't get stuck as
	 * a {@link Material#BURNING_FURNACE}!</u> 
	 * </p>Any open inventories or items on cursors will remain (visually) unaffected!
	 * @param burnEvent the {@link FurnaceBurnEvent} to cancel. (Must be non-<code>null</code>!)
	 */
	public static void cancelBurningNonStuck(final FurnaceBurnEvent burnEvent, final JavaPlugin plugin)
	{
		burnEvent.setCancelled(true);
		
		final Block b = burnEvent.getBlock();
		final Furnace f = (Furnace) b.getState();
		final FurnaceInventory fi = f.getInventory();
		
		if(f.getBurnTime() == ((short)0) && b.getType() == Material.BURNING_FURNACE)
		{
			//...full manual mode...
			final ItemStack[] contents = fi.getContents(); //save all inventory contents
			final int size;
			final HumanEntity[] viewers; //save all viewers
			final ItemStack[] cursors;  //save itemOnCursor for all viewers
			{
				final List<HumanEntity> viewersL = fi.getViewers();
				size = viewersL.size();
				viewers = new HumanEntity[size];
				cursors = new ItemStack[size];
				for(int i = size-1; i >= 0; --i) {
					final HumanEntity he = viewersL.get(i);
					viewers[i] = he;
					cursors[i] = he.getItemOnCursor();
					he.setItemOnCursor(null); //remove old itemOnCursor
				}
			}
			fi.clear(); //empty old inventory so no duplicate item is dropped on the ground
			final BlockFace face = ((Directional)f.getData()).getFacing(); //save block rotation
			f.setType(Material.FURNACE); //change block type (state)
			((Directional)f.getData()).setFacingDirection(face); //re-apply block rotation
			f.update(true); //apply state to block
			//f.getInventory().setContents(contents); //not working - seems this is still the old inventory
			final FurnaceInventory fi2 = ((Furnace)b.getState()).getInventory(); //get the new inventory
			fi2.setContents(contents); //add all inventory content back
			for(int i = size-1; i >= 0; --i) { //concurrent modification exception here if the viewersL list is used directly in a foreach!
				final HumanEntity he = viewers[i];
				//final InventoryView view = 
				he.openInventory(fi2); //update viewers to new inventory
				final ItemStack onCur = cursors[i];
				he.setItemOnCursor(onCur); //re-apply any item on cursor //TODO: item becomes invisible (client side visual glitch)
			}
			
			//DEBUG
			//sender.sendMessage("DEBUG: furnace unstuck!");
		}
	}
}
