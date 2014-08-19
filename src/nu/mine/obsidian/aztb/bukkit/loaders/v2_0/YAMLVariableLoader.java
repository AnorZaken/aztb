package nu.mine.obsidian.aztb.bukkit.loaders.v2_0;

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
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.util.Vector;

/**
 * Helper class to simplify loading of variables from a {@link ConfigurationSection}. 
 * </p><i>
 * ... For loading of the yaml-file itself see {@link YAMLLoader} )<br>
 * ... For String loading see also {@link StringLoader} )</i>
 * 
 * @author AnorZaken
 * @version 2.0a
 */
public abstract class YAMLVariableLoader
{
	public static interface YAMLVariable
	{
		/**
		 * Get the variables name in the yaml file. </p>
		 * Example:
		 * <br><i>MyConfig.yaml</i> contains the value
		 * <br>&nbsp;&nbsp;<code>CheckForUpdates: true</code>
		 * <br>and this {@link YAMLVariable} should represent this variable,
		 * <br>then {@link #getName()} should return the {@link String} <code>"CheckForUpdates"</code>
		 * @return The name of the variable as written in the yaml-file
		 */
		public String getName();
	}
	
	public static interface YAMLBool extends YAMLVariable
	{
		public boolean getBool();
		public void setValue(boolean value);
	}
	public static interface YAMLColor extends YAMLVariable
	{
		public Color getColor();
		public void setValue(Color value);
	}
	public static interface YAMLDouble extends YAMLVariable
	{
		public double getDouble();
		public void setValue(double value);
	}
	public static interface YAMLInt extends YAMLVariable
	{
		public int getInt();
		public void setValue(int value);
	}
	public static interface YAMLItemStack extends YAMLVariable
	{
		public ItemStack getItemStack();
		public void setValue(ItemStack value);
	}
	public static interface YAMLLong extends YAMLVariable
	{
		public long getLong();
		public void setValue(long value);
	}
	public static interface YAMLString extends YAMLVariable
	{
		public String getString();
		public void setValue(String value);
	}
	public static interface YAMLVector extends YAMLVariable
	{
		public Vector getVector();
		public void setValue(Vector value);
	}
	//--- v1.2
	public static interface YAMLFloat extends YAMLVariable //Convenience type (internally similar to double)
	{
		public float getFloat();
		public void setValue(float value);
	}
	//--- v2.0
	public static interface YAMLPermissionDefault extends YAMLVariable
	{
		public PermissionDefault getPermissionDefault();
		public void setValue(PermissionDefault value);
	}
	public static interface YAMLMaterial extends YAMLVariable
	{
		public Material getMaterial();
		public void setValue(Material value);
	}
	public static interface YAMLChatColor extends YAMLVariable
	{
		public ChatColor getChatColor();
		public void setValue(ChatColor value);
	}
	
	// ====================
	
	
	private static String loadHelper(final ConfigurationSection config, final YAMLVariable variable)
	{
		if(config == null)
			throw new IllegalArgumentException("config == null");
		if(variable == null)
			throw new IllegalArgumentException("variable == null");
		final String cfg = variable.getName();
		if(cfg == null || cfg.length() == 0)
			throw new IllegalStateException("variable name null or empty");
		return cfg;
	}
	
	// ------
	
	/**
	 * Attempts to load some {@link YAMLBool} from a {@link ConfigurationSection}.
	 * @param config the {@link ConfigurationSection} to read/write from/to. <b>Must be non-<code>null</code>!</b>
	 * @param readonly if a variable doesn't exist in the {@link ConfigurationSection} (or exists but isn't of
	 *  the expected type) and this is <code>false</code> the variable will be added to the config with the
	 *  current value of {@link YAMLBool#getBool() variable.getBool()} (good for default initialization!)
	 * @param variables some {@link YAMLBool} to load(/initialize) from(/to) the {@link ConfigurationSection}.
	 *  <b>Must be non-<code>null</code>!</b>
	 * @return <code>null</code> if all variables was loaded from the config, otherwise a {@link List} of all
	 *  variables that failed to load
	 * @throws IllegalArgumentException if any argument is <code>null</code> or empty
	 * @throws IllegalStateException if {@link YAMLVariable#getName() variable.getName()} is <code>null</code> or empty
	 */
	public static List<YAMLBool> load(final ConfigurationSection config, final boolean readonly, final YAMLBool... variables)
	{
		if(variables == null || variables.length == 0)
			throw new IllegalArgumentException("variables null or empty");
		ArrayList<YAMLBool> list = null; // <----- type
		for(YAMLBool variable : variables)
		{
			final String cfg = loadHelper(config, variable);
			if(config.isBoolean(cfg)) { // <-------------------- type
				variable.setValue(config.getBoolean(cfg)); // <--- type
				continue;
			} else {
				if(!readonly)
					config.set(cfg, Boolean.valueOf(variable.getBool())); // <----------- type
				(list == null ? list = new ArrayList<YAMLBool>() : list).add(variable); // <-- type
			}
		}
		return list;
	}
	
	/**
	 * Attempts to load some {@link YAMLColor} from a {@link ConfigurationSection}.
	 * @param config the {@link ConfigurationSection} to read/write from/to. <b>Must be non-<code>null</code>!</b>
	 * @param readonly if a variable doesn't exist in the {@link ConfigurationSection} (or exists but isn't of
	 *  the expected type) and this is <code>false</code> the variable will be added to the config with the
	 *  current value of {@link YAMLColor#getColor() variable.getColor()} (good for default initialization!)
	 * @param variables some {@link YAMLColor} to load(/initialize) from(/to) the {@link ConfigurationSection}.
	 *  <b>Must be non-<code>null</code>!</b>
	 * @return <code>null</code> if all variables was loaded from the config, otherwise a {@link List} of all
	 *  variables that failed to load
	 * @throws IllegalArgumentException if any argument is <code>null</code> or empty
	 * @throws IllegalStateException if {@link YAMLVariable#getName() variable.getName()} is <code>null</code> or empty
	 */
	public static List<YAMLColor> load(final ConfigurationSection config, final boolean readonly, final YAMLColor... variables)
	{
		if(variables == null || variables.length == 0)
			throw new IllegalArgumentException("variables null or empty");
		ArrayList<YAMLColor> list = null; // <----- type
		for(YAMLColor variable : variables) // <----- type
		{
			final String cfg = loadHelper(config, variable);
			if(config.isColor(cfg)) { // <-------------------- type
				variable.setValue(config.getColor(cfg)); // <--- type
				continue;
			} else {
				if(!readonly)
					config.set(cfg, variable.getColor()); // <----------- type
				(list == null ? list = new ArrayList<YAMLColor>() : list).add(variable); // <-- type
			}
		}
		return list;
	}
	
	/**
	 * Attempts to load some {@link YAMLDouble} from a {@link ConfigurationSection}.
	 * @param config the {@link ConfigurationSection} to read/write from/to. <b>Must be non-<code>null</code>!</b>
	 * @param readonly if a variable doesn't exist in the {@link ConfigurationSection} (or exists but isn't of
	 *  the expected type) and this is <code>false</code> the variable will be added to the config with the
	 *  current value of {@link YAMLDouble#getDouble() variable.getDouble()} (good for default initialization!)
	 * @param variables some {@link YAMLDouble} to load(/initialize) from(/to) the {@link ConfigurationSection}.
	 *  <b>Must be non-<code>null</code>!</b>
	 * @return <code>null</code> if all variables was loaded from the config, otherwise a {@link List} of all
	 *  variables that failed to load (a number that results in infinity will count as a failure)
	 * @throws IllegalArgumentException if any argument is <code>null</code> or empty
	 * @throws IllegalStateException if {@link YAMLVariable#getName() variable.getName()} is <code>null</code> or empty
	 */
	public static List<YAMLDouble> load(final ConfigurationSection config, final boolean readonly, final YAMLDouble... variables)
	{
		if(variables == null || variables.length == 0)
			throw new IllegalArgumentException("variables null or empty");
		ArrayList<YAMLDouble> list = null; // <----- type
		for(YAMLDouble variable : variables) // <----- type
		{
			final String cfg = loadHelper(config, variable);
			final double value; // <----------- type
			if(config.isDouble(cfg) && !Double.isInfinite(value = config.getDouble(cfg))) { // <-- type
				variable.setValue(value);
				continue;
			} else {
				if(!readonly)
					config.set(cfg, variable.getDouble()); // <----------- type
				(list == null ? list = new ArrayList<YAMLDouble>() : list).add(variable); // <-- type
			}
		}
		return list;
	}
	
	/**
	 * Attempts to load some {@link YAMLInt} from a {@link ConfigurationSection}.
	 * @param config the {@link ConfigurationSection} to read/write from/to. <b>Must be non-<code>null</code>!</b>
	 * @param readonly if a variable doesn't exist in the {@link ConfigurationSection} (or exists but isn't of
	 *  the expected type) and this is <code>false</code> the variable will be added to the config with the
	 *  current value of {@link YAMLInt#getInt() variable.getInt()} (good for default initialization!)
	 * @param variables some {@link YAMLInt} to load(/initialize) from(/to) the {@link ConfigurationSection}.
	 *  <b>Must be non-<code>null</code>!</b>
	 * @return <code>null</code> if all variables was loaded from the config, otherwise a {@link List} of all
	 *  variables that failed to load
	 * @throws IllegalArgumentException if any argument is <code>null</code> or empty
	 * @throws IllegalStateException if {@link YAMLVariable#getName() variable.getName()} is <code>null</code> or empty
	 */
	public static List<YAMLInt> load(final ConfigurationSection config, final boolean readonly, final YAMLInt... variables)
	{
		if(variables == null || variables.length == 0)
			throw new IllegalArgumentException("variables null or empty");
		ArrayList<YAMLInt> list = null; // <----- type
		for(YAMLInt variable : variables) // <----- type
		{
			final String cfg = loadHelper(config, variable);
			if(config.isInt(cfg)) { // <-------------------- type
				variable.setValue(config.getInt(cfg)); // <--- type
				continue;
			} else {
				if(!readonly)
					config.set(cfg, variable.getInt()); // <----------- type
				(list == null ? list = new ArrayList<YAMLInt>() : list).add(variable); // <-- type
			}
		}
		return list;
	}
	
	/**
	 * Attempts to load some {@link YAMLItemStack} from a {@link ConfigurationSection}.
	 * @param config the {@link ConfigurationSection} to read/write from/to. <b>Must be non-<code>null</code>!</b>
	 * @param readonly if a variable doesn't exist in the {@link ConfigurationSection} (or exists but isn't of
	 *  the expected type) and this is <code>false</code> the variable will be added to the config with the
	 *  current value of {@link YAMLItemStack#getItemStack() variable.getItemStack()} (good for default initialization!)
	 * @param variables some {@link YAMLItemStack} to load(/initialize) from(/to) the {@link ConfigurationSection}.
	 *  <b>Must be non-<code>null</code>!</b>
	 * @return <code>null</code> if all variables was loaded from the config, otherwise a {@link List} of all
	 *  variables that failed to load
	 * @throws IllegalArgumentException if any argument is <code>null</code> or empty
	 * @throws IllegalStateException if {@link YAMLVariable#getName() variable.getName()} is <code>null</code> or empty
	 */
	public static List<YAMLItemStack> load(final ConfigurationSection config, final boolean readonly, final YAMLItemStack... variables)
	{
		if(variables == null || variables.length == 0)
			throw new IllegalArgumentException("variables null or empty");
		ArrayList<YAMLItemStack> list = null; // <----- type
		for(YAMLItemStack variable : variables) // <----- type
		{
			final String cfg = loadHelper(config, variable);
			if(config.isItemStack(cfg)) { // <-------------------- type
				variable.setValue(config.getItemStack(cfg)); // <--- type
				continue;
			} else {
				if(!readonly)
					config.set(cfg, variable.getItemStack()); // <----------- type
				(list == null ? list = new ArrayList<YAMLItemStack>() : list).add(variable); // <-- type
			}
		}
		return list;
	}
	
	/**
	 * Attempts to load some {@link YAMLLong} from a {@link ConfigurationSection}.
	 * @param config the {@link ConfigurationSection} to read/write from/to. <b>Must be non-<code>null</code>!</b>
	 * @param readonly if a variable doesn't exist in the {@link ConfigurationSection} (or exists but isn't of
	 *  the expected type) and this is <code>false</code> the variable will be added to the config with the
	 *  current value of {@link YAMLLong#getLong() variable.getLong()} (good for default initialization!)
	 * @param variables some {@link YAMLLong} to load(/initialize) from(/to) the {@link ConfigurationSection}.
	 *  <b>Must be non-<code>null</code>!</b>
	 * @return <code>null</code> if all variables was loaded from the config, otherwise a {@link List} of all
	 *  variables that failed to load
	 * @throws IllegalArgumentException if any argument is <code>null</code> or empty
	 * @throws IllegalStateException if {@link YAMLVariable#getName() variable.getName()} is <code>null</code> or empty
	 */
	public static List<YAMLLong> load(final ConfigurationSection config, final boolean readonly, final YAMLLong... variables)
	{
		if(variables == null || variables.length == 0)
			throw new IllegalArgumentException("variables null or empty");
		ArrayList<YAMLLong> list = null; // <----- type
		for(YAMLLong variable : variables) // <----- type
		{
			final String cfg = loadHelper(config, variable);
			if(config.isLong(cfg)) { // <-------------------- type
				variable.setValue(config.getLong(cfg)); // <--- type
				continue;
			} else {
				if(!readonly)
					config.set(cfg, variable.getLong()); // <----------- type
				(list == null ? list = new ArrayList<YAMLLong>() : list).add(variable); // <-- type
			}
		}
		return list;
	}
	
	/**
	 * Attempts to load some {@link YAMLString} from a {@link ConfigurationSection}.
	 * @param config the {@link ConfigurationSection} to read/write from/to. <b>Must be non-<code>null</code>!</b>
	 * @param readonly if a variable doesn't exist in the {@link ConfigurationSection} (or exists but isn't of
	 *  the expected type) and this is <code>false</code> the variable will be added to the config with the
	 *  current value of {@link YAMLString#getString() variable.getString()} (good for default initialization!)
	 * @param variables some {@link YAMLString} to load(/initialize) from(/to) the {@link ConfigurationSection}.
	 *  <b>Must be non-<code>null</code>!</b>
	 * @return <code>null</code> if all variables was loaded from the config, otherwise a {@link List} of all
	 *  variables that failed to load
	 * @throws IllegalArgumentException if any argument is <code>null</code> or empty
	 * @throws IllegalStateException if {@link YAMLVariable#getName() variable.getName()} is <code>null</code> or empty
	 */
	public static List<YAMLString> load(final ConfigurationSection config, final boolean readonly, final YAMLString... variables)
	{
		if(variables == null || variables.length == 0)
			throw new IllegalArgumentException("variables null or empty");
		ArrayList<YAMLString> list = null; // <----- type
		for(YAMLString variable : variables) // <----- type
		{
			final String cfg = loadHelper(config, variable);
			if(config.isString(cfg)) { // <-------------------- type
				variable.setValue(config.getString(cfg)); // <--- type
				continue;
			} else {
				if(!readonly)
					config.set(cfg, variable.getString()); // <----------- type
				(list == null ? list = new ArrayList<YAMLString>() : list).add(variable); // <-- type
			}
		}
		return list;
	}
	
	/**
	 * Attempts to load some {@link YAMLVector} from a {@link ConfigurationSection}.
	 * @param config the {@link ConfigurationSection} to read/write from/to. <b>Must be non-<code>null</code>!</b>
	 * @param readonly if a variable doesn't exist in the {@link ConfigurationSection} (or exists but isn't of
	 *  the expected type) and this is <code>false</code> the variable will be added to the config with the
	 *  current value of {@link YAMLVector#getVector() variable.getVector()} (good for default initialization!)
	 * @param variables some {@link YAMLVector} to load(/initialize) from(/to) the {@link ConfigurationSection}.
	 *  <b>Must be non-<code>null</code>!</b>
	 * @return <code>null</code> if all variables was loaded from the config, otherwise a {@link List} of all
	 *  variables that failed to load
	 * @throws IllegalArgumentException if any argument is <code>null</code> or empty
	 * @throws IllegalStateException if {@link YAMLVariable#getName() variable.getName()} is <code>null</code> or empty
	 */
	public static List<YAMLVector> load(final ConfigurationSection config, final boolean readonly, final YAMLVector... variables)
	{
		if(variables == null || variables.length == 0)
			throw new IllegalArgumentException("variables null or empty");
		ArrayList<YAMLVector> list = null; // <----- type
		for(YAMLVector variable : variables) // <----- type
		{
			final String cfg = loadHelper(config, variable);
			if(config.isVector(cfg)) { // <-------------------- type
				variable.setValue(config.getVector(cfg)); // <--- type
				continue;
			} else {
				if(!readonly)
					config.set(cfg, variable.getVector()); // <----------- type
				(list == null ? list = new ArrayList<YAMLVector>() : list).add(variable); // <-- type
			}
		}
		return list;
	}
	
	/**
	 * Attempts to load some {@link YAMLFloat} from a {@link ConfigurationSection}.
	 * @param config the {@link ConfigurationSection} to read/write from/to. <b>Must be non-<code>null</code>!</b>
	 * @param readonly if a variable doesn't exist in the {@link ConfigurationSection} (or exists but isn't of
	 *  the expected type) and this is <code>false</code> the variable will be added to the config with the
	 *  current value of {@link YAMLFloat#getFloat() variable.getFloat()} (good for default initialization!)
	 * @param variables some {@link YAMLFloat} to load(/initialize) from(/to) the {@link ConfigurationSection}.
	 *  <b>Must be non-<code>null</code>!</b>
	 * @return <code>null</code> if all variables was loaded from the config, otherwise a {@link List} of all
	 *  variables that failed to load (a number that results in infinity will count as a failure)
	 * @throws IllegalArgumentException if any argument is <code>null</code> or empty
	 * @throws IllegalStateException if {@link YAMLVariable#getName() variable.getName()} is <code>null</code> or empty
	 */
	public static List<YAMLFloat> load(final ConfigurationSection config, final boolean readonly, final YAMLFloat... variables)
	{
		if(variables == null || variables.length == 0)
			throw new IllegalArgumentException("variables null or empty");
		ArrayList<YAMLFloat> list = null; // <----- type
		for(YAMLFloat variable : variables) // <----- type
		{
			final String cfg = loadHelper(config, variable);
			final float value; // <----------- type
			if(config.isDouble(cfg) && !Float.isInfinite(value = (float) config.getDouble(cfg))) { // <-- type
				variable.setValue(value);
				continue;
			} else {
				if(!readonly)
					config.set(cfg, variable.getFloat()); // <----------- type
				(list == null ? list = new ArrayList<YAMLFloat>() : list).add(variable); // <-- type
			}
		}
		return list;
	}
	
	/**
	 * Attempts to load some {@link YAMLPermissionDefault} from a {@link ConfigurationSection}.
	 * @param config the {@link ConfigurationSection} to read/write from/to. <b>Must be non-<code>null</code>!</b>
	 * @param readonly if a variable doesn't exist in the {@link ConfigurationSection} (or exists but isn't of
	 *  the expected type) and this is <code>false</code> the variable will be added to the config with the
	 *  current value of {@link YAMLPermissionDefault#getPermissionDefault() variable.getPermissionDefault()},
	 *  (unless the current value is <code>null</code>) (good for default initialization!)
	 * @param variables some {@link YAMLPermissionDefault} to load(/initialize) from(/to) the {@link ConfigurationSection}.
	 *  <b>Must be non-<code>null</code>!</b>
	 * @return <code>null</code> if all variables was loaded from the config, otherwise a {@link List} of all
	 *  variables that failed to load
	 * @throws IllegalArgumentException if any argument is <code>null</code> or empty
	 * @throws IllegalStateException if {@link YAMLVariable#getName() variable.getName()} is <code>null</code> or empty
	 */
	public static ArrayList<YAMLPermissionDefault> load(final ConfigurationSection config, final boolean readonly, final YAMLPermissionDefault... variables)
	{
		if(variables == null || variables.length == 0)
			throw new IllegalArgumentException("variables null or empty");
		ArrayList<YAMLPermissionDefault> list = null; // <----- type
		for(YAMLPermissionDefault variable : variables) // <----- type
		{
			final String cfg = loadHelper(config, variable);
			PermissionDefault value; // <----------- type
			if(config.isString(cfg) && (value = PermissionDefault.getByName(config.getString(cfg))) != null) { // <-- type
				variable.setValue(value);
				continue;
			} else {
				if(!readonly && (value = variable.getPermissionDefault()) != null) // <-- type
					config.set(cfg, value.toString()); // <----------- type
				(list == null ? list = new ArrayList<YAMLPermissionDefault>() : list).add(variable); // <-- type
			}
		}
		return list;
	}
	
	/**
	 * Attempts to load some {@link YAMLMaterial} from a {@link ConfigurationSection}.
	 * @param config the {@link ConfigurationSection} to read/write from/to. <b>Must be non-<code>null</code>!</b>
	 * @param readonly if a variable doesn't exist in the {@link ConfigurationSection} (or exists but isn't of
	 *  the expected type) and this is <code>false</code> the variable will be added to the config with the
	 *  current value of {@link YAMLMaterial#getMaterial() variable.getMaterial()},
	 *  (unless the current value is <code>null</code>) (good for default initialization!)
	 * @param variables some {@link YAMLMaterial} to load(/initialize) from(/to) the {@link ConfigurationSection}.
	 *  <b>Must be non-<code>null</code>!</b>
	 * @return <code>null</code> if all variables was loaded from the config, otherwise a {@link List} of all
	 *  variables that failed to load
	 * @throws IllegalArgumentException if any argument is <code>null</code> or empty
	 * @throws IllegalStateException if {@link YAMLVariable#getName() variable.getName()} is <code>null</code> or empty
	 */
	public static ArrayList<YAMLMaterial> load(final ConfigurationSection config, final boolean readonly, final YAMLMaterial... variables)
	{
		if(variables == null || variables.length == 0)
			throw new IllegalArgumentException("variables null or empty");
		ArrayList<YAMLMaterial> list = null; // <----- type
		for(YAMLMaterial variable : variables) // <----- type
		{
			final String cfg = loadHelper(config, variable);
			Material value; // <----------- type
			if(config.isString(cfg) && (value = Material.getMaterial(config.getString(cfg))) != null) { // <-- type
				variable.setValue(value);
				continue;
			} else {
				if(!readonly && (value = variable.getMaterial()) != null) // <-- type
					config.set(cfg, value.toString()); // <----------- type
				(list == null ? list = new ArrayList<YAMLMaterial>() : list).add(variable); // <-- type
			}
		}
		return list;
	}
	
	/**
	 * Attempts to load some {@link YAMLChatColor} from a {@link ConfigurationSection}.
	 * @param config the {@link ConfigurationSection} to read/write from/to. <b>Must be non-<code>null</code>!</b>
	 * @param readonly if a variable doesn't exist in the {@link ConfigurationSection} (or exists but isn't of
	 *  the expected type) and this is <code>false</code> the variable will be added to the config with the
	 *  current value of {@link YAMLChatColor#getChatColor() variable.getChatColor()},
	 *  (unless the current value is <code>null</code>) (good for default initialization!)
	 * @param variables some {@link YAMLChatColor} to load(/initialize) from(/to) the {@link ConfigurationSection}.
	 *  <b>Must be non-<code>null</code>!</b>
	 * @return <code>null</code> if all variables was loaded from the config, otherwise a {@link List} of all
	 *  variables that failed to load
	 * @throws IllegalArgumentException if any argument is <code>null</code> or empty
	 * @throws IllegalStateException if {@link YAMLVariable#getName() variable.getName()} is <code>null</code> or empty
	 */
	public static ArrayList<YAMLChatColor> load(final ConfigurationSection config, final boolean readonly, final YAMLChatColor... variables)
	{
		if(variables == null || variables.length == 0)
			throw new IllegalArgumentException("variables null or empty");
		ArrayList<YAMLChatColor> list = null; // <----- type
		for(YAMLChatColor variable : variables) // <----- type
		{
			final String cfg = loadHelper(config, variable);
			boolean save;
			if(config.isString(cfg))
			{
				save = false;
				ChatColor value = null; // <----- type
				try {
					value = ChatColor.valueOf(config.getString(cfg)); // <-- type
				} catch(IllegalArgumentException ex) { //No such enum constant...
					save = true;
				}
				if (!save) {
					variable.setValue(value);
					continue;
				}
			}
			else
				save = true;
			if (save)
			{
				final ChatColor value; // <----- type
				if(!readonly && (value = variable.getChatColor()) != null) // <-- type
					config.set(cfg, value.name()); // <----------- type
				(list == null ? list = new ArrayList<YAMLChatColor>() : list).add(variable); // <-- type
			}
		}
		return list;
	}
}
