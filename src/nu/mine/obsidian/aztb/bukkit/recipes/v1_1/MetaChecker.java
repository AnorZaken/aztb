package nu.mine.obsidian.aztb.bukkit.recipes.v1_1;

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
import java.util.Map;
import java.util.Map.Entry;

import nu.mine.obsidian.aztb.bukkit.recipes.v1_1.RecipeHelper.IMetaChecker;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.meta.ItemMeta;

//TODO: javadoc
//TODO: testing

/**
 * A very simple default implementation of {@link RecipeHelper#IMetaChecker}.
 * @author AnorZaken
 * @version 1.1
 * 
 * @see RecipeHelper
 */
public class MetaChecker implements IMetaChecker
{
	// -------- Instance variables (immutable) --------
	final public boolean checkName;
	final public boolean checkLore;
	final public boolean checkEnchants;
	
	
	// -------- Constructors --------
	
	public MetaChecker(final boolean checkName, final boolean checkLore, final boolean checkEnchants)
	{
		this.checkName = checkName;
		this.checkLore = checkLore;
		this.checkEnchants = checkEnchants;
	}
	
	
	// -------- IMetaChecker methods --------
	
	@Override
	public boolean areItemMetaIdentical(ItemMeta meta1, ItemMeta meta2)
	{
		return  (!checkName || hasMatchingName(meta1, meta2)) &&
				(!checkLore || hasMatchingLore(meta1, meta2)) &&
				(!checkEnchants || hasMatchingEnchants(meta1, meta2));
	}

	@Override
	public boolean isValidItemMeta(ItemMeta meta)
	{
		return  (checkName && meta.hasDisplayName()) ||
				(checkLore && meta.hasLore()) ||
				(checkEnchants && meta.hasEnchants());
	}
	
	
	// -------- Static Matching methods --------
	
	
	public static boolean hasMatchingName(ItemMeta meta1, ItemMeta meta2)
	{
		if (meta1.hasDisplayName()) {
			if (meta2.hasDisplayName())
				return meta1.getDisplayName().equals(meta2.getDisplayName()); //both have name
		} else if (!meta2.hasDisplayName())
			return true; //both lack name
		return false; //one has a name, the other doesn't
	}
	
	
	public static boolean hasMatchingLore(ItemMeta meta1, ItemMeta meta2)
	{
		if (meta1.hasLore()) {
			if (meta2.hasLore()) //both has lore
			{
				final List<String> lore1 = meta1.getLore();
				final List<String> lore2 = meta2.getLore();
				final int size = lore1.size();
				if (size == lore2.size())
				{
					for (int i = 0; i < size; ++i) //Compare all sizes first
						if (lore1.get(i).length() != lore2.get(i).length())
							return false;
					for (int i = 0; i < size; ++i) //Compare contents
						if (!lore1.get(i).equals(lore2.get(i)))
							return false;
					return true; //Exact Lore match!
				}
			}
		} else if (!meta2.hasLore())
			return true; //both lack lore
		return false;//one has lore, the other doesn't || size doesn't match
	}
	
	
	public static boolean hasMatchingEnchants(ItemMeta meta1, ItemMeta meta2)
	{
		if (meta1.hasEnchants()) {
			if (meta2.hasEnchants()) //both has enchants
			{
				final Map<Enchantment, Integer> map1 = meta1.getEnchants();
				final Map<Enchantment, Integer> map2 = meta2.getEnchants();
				if (map1.size() == map2.size())
				{
					for (Entry<Enchantment, Integer> e : map1.entrySet())
					{
						final Integer i2 = map2.get(e.getKey());
						if (i2 == null || !i2.equals(e.getValue()))
							return false;
					}
					return true; //Exact enchants match!
				}
			}
		} else if (!meta2.hasEnchants())
			return true; //both lack enchants
		return false; //one has enchants, the other doesn't || size doesn't match
	}
}