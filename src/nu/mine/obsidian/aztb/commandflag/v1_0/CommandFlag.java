package nu.mine.obsidian.aztb.commandflag.v1_0;

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

/**
 * Flag-class used to describe a flag in the CommandFlag-framework. (javadoc wip)
 * @author AnorZaken
 * @version 1.0
 */
public class CommandFlag //"Immutable" (except maybe if sub-classed!) //TODO: all the javadoc!
{
	
	public static class FlagValues
	{
		private final String[] values;
		
		public FlagValues(final List<String> values)
		{
			if (values == null)
				throw new IllegalArgumentException("values is null");
			this.values = values.toArray(new String[values.size()]);
		}
		
		public int size() {
			return values.length;
		}
		
		public String getValue(final int index) {
			return values[index];
		}
		
		public CommandFlag getFlag() {
			return null;
		}
	}
	
	// -----
	
	protected class ValueInner extends FlagValues //IMMUTABLE
	{
		public ValueInner(final List<String> values) {
			super(values);
		}
		
		@Override
		public final CommandFlag getFlag() {
			return CommandFlag.this;
		}
	}
	
	// =====================
	
	protected final String flag;
	protected final boolean isCaseSensitive;
	protected final Character separator;
	//protected final boolean isWhiteSpaceEnd;
	
	
	public CommandFlag(final String flag) {
		this(flag, false, null);
	}
	
	public CommandFlag(final String flag, final boolean isCaseSensitive, final Character separator)
	{
		if (flag == null || flag.length() == 0)
			throw new IllegalArgumentException("flag is null or zero-length");
		if (flag.charAt(0) == ' ' || flag.indexOf("  ", 1) != -1)
			throw new IllegalArgumentException("flag has whitespace issue");
		
		this.isCaseSensitive = isCaseSensitive;
		this.flag = isCaseSensitive ? flag : flag.toLowerCase(CFStatics.locale);
		this.separator = separator;
	}
	
	// =====================
	
	public String getFlag() {
		return flag;
	}
	
	public Character getSeparator() {
		return separator;
	}
	
	public boolean isCaseSensitive() {
		return isCaseSensitive;
	}
}
