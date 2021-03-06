package nu.mine.obsidian.aztb.bukkit.loaders.v1_0;

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

import org.bukkit.Color;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

/**
 * Helper class to simplify loading of variables from a {@link YamlConfiguration}. 
 * </p><i>
 * ... For loading of the yaml-file itself see {@link YAMLLoader} )<br>
 * ... For String loading see also {@link StringLoader} )</i>
 * 
 * @author AnorZaken
 * @version 1.0
 */
public abstract class YAMLVariableLoader
{
	private static interface YAMLVariable
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
	
	// ====================
	
	
	private static String loadHelper(final YamlConfiguration config, final YAMLVariable variable)
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
	 * Attempts to load some {@link YAMLBool} from a {@link YamlConfiguration}.
	 * @param config the {@link YamlConfiguration} to read/write from/to. <b>Must be non-<code>null</code>!</b>
	 * @param readonly if a variable doesn't exist in the {@link YamlConfiguration} (or exists but isn't of
	 *  the expected type) and this is <code>false</code> the variable will be added to the config with the
	 *  current value of {@link YAMLBool#getBool() variable.getBool()} (good for default initialization!)
	 * @param variables some {@link YAMLBool} to load(/initialize) from(/to) the {@link YamlConfiguration}.
	 *  <b>Must be non-<code>null</code>!</b>
	 * @return <code>null</code> if all variables was loaded from the config, otherwise a {@link List} of all
	 *  variables that failed to load
	 * @throws IllegalArgumentException if any argument is <code>null</code> or empty
	 * @throws IllegalStateException if {@link YAMLVariable#getName() variable.getName()} is <code>null</code> or empty
	 */
	public static List<YAMLBool> load(final YamlConfiguration config, final boolean readonly, final YAMLBool... variables)
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
	 * Attempts to load some {@link YAMLColor} from a {@link YamlConfiguration}.
	 * @param config the {@link YamlConfiguration} to read/write from/to. <b>Must be non-<code>null</code>!</b>
	 * @param readonly if a variable doesn't exist in the {@link YamlConfiguration} (or exists but isn't of
	 *  the expected type) and this is <code>false</code> the variable will be added to the config with the
	 *  current value of {@link YAMLColor#getColor() variable.getColor()} (good for default initialization!)
	 * @param variables some {@link YAMLColor} to load(/initialize) from(/to) the {@link YamlConfiguration}.
	 *  <b>Must be non-<code>null</code>!</b>
	 * @return <code>null</code> if all variables was loaded from the config, otherwise a {@link List} of all
	 *  variables that failed to load
	 * @throws IllegalArgumentException if any argument is <code>null</code> or empty
	 * @throws IllegalStateException if {@link YAMLVariable#getName() variable.getName()} is <code>null</code> or empty
	 */
	public static List<YAMLColor> load(final YamlConfiguration config, final boolean readonly, final YAMLColor... variables)
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
	 * Attempts to load some {@link YAMLDouble} from a {@link YamlConfiguration}.
	 * @param config the {@link YamlConfiguration} to read/write from/to. <b>Must be non-<code>null</code>!</b>
	 * @param readonly if a variable doesn't exist in the {@link YamlConfiguration} (or exists but isn't of
	 *  the expected type) and this is <code>false</code> the variable will be added to the config with the
	 *  current value of {@link YAMLDouble#getDouble() variable.getDouble()} (good for default initialization!)
	 * @param variables some {@link YAMLDouble} to load(/initialize) from(/to) the {@link YamlConfiguration}.
	 *  <b>Must be non-<code>null</code>!</b>
	 * @return <code>null</code> if all variables was loaded from the config, otherwise a {@link List} of all
	 *  variables that failed to load
	 * @throws IllegalArgumentException if any argument is <code>null</code> or empty
	 * @throws IllegalStateException if {@link YAMLVariable#getName() variable.getName()} is <code>null</code> or empty
	 */
	public static List<YAMLDouble> load(final YamlConfiguration config, final boolean readonly, final YAMLDouble... variables)
	{
		if(variables == null || variables.length == 0)
			throw new IllegalArgumentException("variables null or empty");
		ArrayList<YAMLDouble> list = null; // <----- type
		for(YAMLDouble variable : variables) // <----- type
		{
			final String cfg = loadHelper(config, variable);
			if(config.isDouble(cfg)) { // <-------------------- type
				variable.setValue(config.getDouble(cfg)); // <--- type
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
	 * Attempts to load some {@link YAMLInt} from a {@link YamlConfiguration}.
	 * @param config the {@link YamlConfiguration} to read/write from/to. <b>Must be non-<code>null</code>!</b>
	 * @param readonly if a variable doesn't exist in the {@link YamlConfiguration} (or exists but isn't of
	 *  the expected type) and this is <code>false</code> the variable will be added to the config with the
	 *  current value of {@link YAMLInt#getInt() variable.getInt()} (good for default initialization!)
	 * @param variables some {@link YAMLInt} to load(/initialize) from(/to) the {@link YamlConfiguration}.
	 *  <b>Must be non-<code>null</code>!</b>
	 * @return <code>null</code> if all variables was loaded from the config, otherwise a {@link List} of all
	 *  variables that failed to load
	 * @throws IllegalArgumentException if any argument is <code>null</code> or empty
	 * @throws IllegalStateException if {@link YAMLVariable#getName() variable.getName()} is <code>null</code> or empty
	 */
	public static List<YAMLInt> load(final YamlConfiguration config, final boolean readonly, final YAMLInt... variables)
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
	 * Attempts to load some {@link YAMLItemStack} from a {@link YamlConfiguration}.
	 * @param config the {@link YamlConfiguration} to read/write from/to. <b>Must be non-<code>null</code>!</b>
	 * @param readonly if a variable doesn't exist in the {@link YamlConfiguration} (or exists but isn't of
	 *  the expected type) and this is <code>false</code> the variable will be added to the config with the
	 *  current value of {@link YAMLItemStack#getItemStack() variable.getItemStack()} (good for default initialization!)
	 * @param variables some {@link YAMLItemStack} to load(/initialize) from(/to) the {@link YamlConfiguration}.
	 *  <b>Must be non-<code>null</code>!</b>
	 * @return <code>null</code> if all variables was loaded from the config, otherwise a {@link List} of all
	 *  variables that failed to load
	 * @throws IllegalArgumentException if any argument is <code>null</code> or empty
	 * @throws IllegalStateException if {@link YAMLVariable#getName() variable.getName()} is <code>null</code> or empty
	 */
	public static List<YAMLItemStack> load(final YamlConfiguration config, final boolean readonly, final YAMLItemStack... variables)
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
	 * Attempts to load some {@link YAMLLong} from a {@link YamlConfiguration}.
	 * @param config the {@link YamlConfiguration} to read/write from/to. <b>Must be non-<code>null</code>!</b>
	 * @param readonly if a variable doesn't exist in the {@link YamlConfiguration} (or exists but isn't of
	 *  the expected type) and this is <code>false</code> the variable will be added to the config with the
	 *  current value of {@link YAMLLong#getLong() variable.getLong()} (good for default initialization!)
	 * @param variables some {@link YAMLLong} to load(/initialize) from(/to) the {@link YamlConfiguration}.
	 *  <b>Must be non-<code>null</code>!</b>
	 * @return <code>null</code> if all variables was loaded from the config, otherwise a {@link List} of all
	 *  variables that failed to load
	 * @throws IllegalArgumentException if any argument is <code>null</code> or empty
	 * @throws IllegalStateException if {@link YAMLVariable#getName() variable.getName()} is <code>null</code> or empty
	 */
	public static List<YAMLLong> load(final YamlConfiguration config, final boolean readonly, final YAMLLong... variables)
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
	 * Attempts to load some {@link YAMLString} from a {@link YamlConfiguration}.
	 * @param config the {@link YamlConfiguration} to read/write from/to. <b>Must be non-<code>null</code>!</b>
	 * @param readonly if a variable doesn't exist in the {@link YamlConfiguration} (or exists but isn't of
	 *  the expected type) and this is <code>false</code> the variable will be added to the config with the
	 *  current value of {@link YAMLString#getString() variable.getString()} (good for default initialization!)
	 * @param variables some {@link YAMLString} to load(/initialize) from(/to) the {@link YamlConfiguration}.
	 *  <b>Must be non-<code>null</code>!</b>
	 * @return <code>null</code> if all variables was loaded from the config, otherwise a {@link List} of all
	 *  variables that failed to load
	 * @throws IllegalArgumentException if any argument is <code>null</code> or empty
	 * @throws IllegalStateException if {@link YAMLVariable#getName() variable.getName()} is <code>null</code> or empty
	 */
	public static List<YAMLString> load(final YamlConfiguration config, final boolean readonly, final YAMLString... variables)
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
	 * Attempts to load some {@link YAMLVector} from a {@link YamlConfiguration}.
	 * @param config the {@link YamlConfiguration} to read/write from/to. <b>Must be non-<code>null</code>!</b>
	 * @param readonly if a variable doesn't exist in the {@link YamlConfiguration} (or exists but isn't of
	 *  the expected type) and this is <code>false</code> the variable will be added to the config with the
	 *  current value of {@link YAMLVector#getVector() variable.getVector()} (good for default initialization!)
	 * @param variables some {@link YAMLVector} to load(/initialize) from(/to) the {@link YamlConfiguration}.
	 *  <b>Must be non-<code>null</code>!</b>
	 * @return <code>null</code> if all variables was loaded from the config, otherwise a {@link List} of all
	 *  variables that failed to load
	 * @throws IllegalArgumentException if any argument is <code>null</code> or empty
	 * @throws IllegalStateException if {@link YAMLVariable#getName() variable.getName()} is <code>null</code> or empty
	 */
	public static List<YAMLVector> load(final YamlConfiguration config, final boolean readonly, final YAMLVector... variables)
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
}
