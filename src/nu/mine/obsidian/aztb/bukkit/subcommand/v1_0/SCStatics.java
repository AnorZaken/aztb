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

import java.util.Locale;


import org.bukkit.ChatColor;

/**
 * Collection of static constants and methods used by the {@link SubCommand}-framework.
 * @author AnorZaken
 * @version 1.0
 */
public final class SCStatics
{
	private SCStatics()
	{} //This is a "static" class
	
	// =======================
	
	/**
	 * A {@link ChatColor#RESET} followed by a space.
	 */
	public static final String RESET_SPACE = ChatColor.RESET + " ";
	
	/**
	 * The special chars used in interpreting a raw usage-string for the purpose of formating / coloring.
	 * <p/>uREQ# is for required parameters.
	 * <br>uOPT# is for optional parameters.
	 * <br>uDEP# is for parameters that depend on the caller: required from console but optional for players.
	 * <br>The number# is: 1 for the start symbol and 2 for the closing symbol.
	 */
	public static final char
		uREQ1 = '<', uREQ2 = '>',
		uOPT1 = '[', uOPT2 = ']',
		uDEP1 = '{', uDEP2 = '}';
	
	// =======================

	static Locale locale = Locale.getDefault();

	/**
	 * Set the {@link Locale} that should be used when converting names and aliases to lowercase.
	 */
	public static final void setLocale(Locale locale) {
		if (locale == null)
			throw new IllegalArgumentException("locale can not be null!");
		SCStatics.locale = locale;
	}

	/**
	 * Get the {@link Locale} that will be used when converting names and aliases to lowercase.
	 */
	public static final Locale getLocale() {
		return locale;
	}

	// =======================
	
	static final Markup mParent 	= new Markup(ChatColor.AQUA); //For parent command names
	static final Markup mName 		= new Markup(ChatColor.AQUA, false, false, true); //For subcommand names
	static final Markup mRequired 	= new Markup(ChatColor.GREEN);
	static final Markup mOptional 	= new Markup(ChatColor.DARK_GREEN);
	static final Markup mUsage 		= new Markup(ChatColor.DARK_AQUA); //For "Usage:"-prefix
	static final Markup mAliasPre 	= new Markup(ChatColor.GOLD); //For "Aliases:"-prefix
	static final Markup mAlias 		= new Markup(ChatColor.LIGHT_PURPLE); //For the aliases themselves
	static final Markup mDescShort 	= new Markup();
	static final Markup mDescFull 	= new Markup(ChatColor.ITALIC);
	static final Markup mText 		= new Markup(ChatColor.YELLOW); //For other generic text
	
	public static final Markup getMarkupParent() 	{ return mParent; 	} //TODO: javadoc 			UH2
	public static final Markup getMarkupName() 		{ return mName; 	} //TODO: javadoc 	UH1	U
	public static final Markup getMarkupRequired() 	{ return mRequired; } //TODO: javadoc 	UH1	U
	public static final Markup getMarkupOptional() 	{ return mOptional; } //TODO: javadoc 	UH1	U
	public static final Markup getMarkupUsage() 	{ return mUsage; 	} //TODO: javadoc 			UH2
	public static final Markup getMarkupAliasPre() 	{ return mAliasPre; } //TODO: javadoc 			UH2	A
	public static final Markup getMarkupAlias() 	{ return mAlias; 	} //TODO: javadoc 			UH2	A
	public static final Markup getMarkupDescShort() { return mDescShort;} //TODO: javadoc 		U
	public static final Markup getMarkupDescFull() 	{ return mDescFull; } //TODO: javadoc
	public static final Markup getMarkupText() 		{ return mText; 	} //TODO: javadoc 	UH1		UH2
	
	// =======================
	
	//appends to StringBuilders (null-safe)
	public static boolean formatUsageString(final String raw, final StringBuilder useCons, final StringBuilder usePlay) //TODO: javadoc
	{
		if (raw == null)
			return false;
		
		final StringBuilder uniform;
		final boolean isCon;
		
		if (useCons == null) {
			if (usePlay == null)
				return false;
			else {
				uniform = usePlay;
				isCon = false;
			}
		}
		else if (usePlay == null) {
			uniform = useCons;
			isCon = true;
		}
//		else if (raw.indexOf(cCRP1) == -1) {
//			uniform = ucons;
//			isCon = true;
//			uplay.setLength(0);
//		}
		else //both sb's are non-null //double case begin
		{
			for (int i = 0; i < raw.length(); ++i)
			{
				char c = raw.charAt(i);
				switch (c)
				{
				case uREQ1:
					useCons.append(mRequired.toString()).append(uREQ1);
					usePlay.append(mRequired.toString()).append(uREQ1);
					break;
				case uREQ2:
					useCons.append(uREQ2).append(ChatColor.RESET.toString());
					usePlay.append(uREQ2).append(ChatColor.RESET.toString());
					break;
				case uOPT1:
					useCons.append(mOptional.toString()).append(uOPT1);
					usePlay.append(mOptional.toString()).append(uOPT1);
					break;
				case uOPT2:
					useCons.append(uOPT2).append(ChatColor.RESET.toString());
					usePlay.append(uOPT2).append(ChatColor.RESET.toString());
					break;
				case uDEP1:
					useCons.append(mRequired.toString()).append(uREQ1);
					usePlay.append(mOptional.toString()).append(uOPT1);
					break;
				case uDEP2:
					useCons.append(uREQ2).append(ChatColor.RESET.toString());
					usePlay.append(uOPT2).append(ChatColor.RESET.toString());
					break;
				case '-':
					if (++i < raw.length()) {
						if ((c = raw.charAt(i)) == ' ') {
							useCons.append(mDescShort.toString()).append('-').append(' ');
							usePlay.append(mDescShort.toString()).append('-').append(' ');
							for (++i; i < raw.length(); ++i) { //optimization
								c = raw.charAt(i);
								useCons.append(c);
								usePlay.append(c);
							}
						}
						else {
							useCons.append('-');
							usePlay.append('-');
							--i;
						}	
					}
					else {
						useCons.append('-');
						usePlay.append('-');
					}
					break;
				default:
					useCons.append(c);
					usePlay.append(c);
					break;
				}
			}
			
			return true; //double case end
		}
		
		//single case begin
		for (int i = 0; i < raw.length(); ++i)
		{
			char c = raw.charAt(i);
			switch (c)
			{
			case uREQ1:
				uniform.append(mRequired.toString()).append(uREQ1);
				break;
			case uREQ2:
				uniform.append(uREQ2).append(ChatColor.RESET.toString());
				break;
			case uOPT1:
				uniform.append(mOptional.toString()).append(uOPT1);
				break;
			case uOPT2:
				uniform.append(uOPT2).append(ChatColor.RESET.toString());
				break;
			case uDEP1:
				if(isCon) uniform.append(mRequired.toString()).append(uREQ1);
				else 	  uniform.append(mOptional.toString()).append(uOPT1);
				break;
			case uDEP2:
				if(isCon) uniform.append(uREQ2).append(ChatColor.RESET.toString());
				else 	  uniform.append(uOPT2).append(ChatColor.RESET.toString());
				break;
			case '-':
				if (++i < raw.length()) {
					if ((c = raw.charAt(i)) == ' ') {
						uniform.append(mDescShort.toString()).append('-').append(' ');
						for (++i; i < raw.length(); ++i) { //optimization
							c = raw.charAt(i);
							uniform.append(c);
						}
					}
					else {
						uniform.append('-');
						--i;
					}	
				}
				else {
					uniform.append('-');
				}
				break;
			default:
				uniform.append(c);
				break;
			}
		}
		
		return true; //single case end
	}
	
	// =======================
	
	public static String formatAliasString(final String aliasPrefix, final String[] aliases) //TODO: javadoc
	{
		if(aliasPrefix == null || aliases == null || aliases.length == 0)
			return null;
		else if (aliases.length == 1)
			return SCStatics.mAliasPre + aliasPrefix + SCStatics.RESET_SPACE
					+ SCStatics.mAlias + aliases[0];
		else if (aliases.length == 2)
			return SCStatics.mAliasPre + aliasPrefix + SCStatics.RESET_SPACE
			+ SCStatics.mAlias + aliases[0] + SCStatics.RESET_SPACE
			+ SCStatics.mAlias + aliases[1];
		else
		{
			final String s2 = SCStatics.RESET_SPACE + SCStatics.mAlias;
			StringBuilder sb = (new StringBuilder(32))
					.append(SCStatics.mAliasPre.toString())
					.append(aliasPrefix).append(s2)
					.append(aliases[0]).append(s2)
					.append(aliases[1]).append(s2)
					.append(aliases[2]);
			for (int i = 3; i < aliases.length; ++i)
				sb.append(s2).append(aliases[i]);
			return sb.toString();
		}
	}
	
	// =======================
	
	public static boolean formatDescriptionString(final String raw, final StringBuilder output) //TODO: javadoc
	{
		if (raw == null || output == null)
			return false;
		else for (int i = 0; i < raw.length(); ++i)
		{
			char c = raw.charAt(i);
			switch (c)
			{
				default:
					output.append(c);
					break;
				//TODO: add special syntax support
			}
		}
		return true; //TODO
	}
}
