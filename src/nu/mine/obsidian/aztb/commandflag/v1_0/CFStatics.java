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

import java.util.Locale;

/**
 * Collection of static constants and methods used by the {@link CommandFlag}-framework.
 * @author AnorZaken
 * @version 1.0
 */
public final class CFStatics
{
	private CFStatics()
	{} //This is a "static" class
	
	// =======================

	static Locale locale = Locale.getDefault();

	/**
	 * Set the {@link Locale} that should be used when converting names and aliases to lowercase.
	 */
	public static final void setLocale(Locale locale) {
		if (locale == null)
			throw new IllegalArgumentException("locale can not be null!");
		CFStatics.locale = locale;
	}

	/**
	 * Get the {@link Locale} that will be used when converting names and aliases to lowercase.
	 */
	public static final Locale getLocale() {
		return locale;
	}
}
