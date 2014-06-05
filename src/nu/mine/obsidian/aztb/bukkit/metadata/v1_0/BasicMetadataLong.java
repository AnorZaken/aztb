package nu.mine.obsidian.aztb.bukkit.metadata.v1_0;

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

import java.lang.ref.WeakReference;
import java.util.List;

import org.bukkit.metadata.MetadataValue;
import org.bukkit.metadata.Metadatable;
import org.bukkit.plugin.Plugin;

/**
 * A basic implementation of {@link MetadataValue} that stores the primitive type {@code long}.
 * @author AnorZaken
 * @version 1.0
 */
public final class BasicMetadataLong implements MetadataValue
{
	private final WeakReference<Plugin> owningPlugin;
	private long mLong;
	
	public BasicMetadataLong(Plugin owningPlugin)
	{
		if (owningPlugin == null)
			throw new IllegalArgumentException("owningPlugin can't be null");
		this.owningPlugin = new WeakReference<Plugin>(owningPlugin);
		mLong = 0L;
	}
	
	public BasicMetadataLong(Plugin owningPlugin, long value)
	{
		if (owningPlugin == null)
			throw new IllegalArgumentException("owningPlugin can't be null");
		this.owningPlugin = new WeakReference<Plugin>(owningPlugin);
		mLong = value;
	}
	
	
	public void set(long value) {
		mLong = value;
	}
	
	// -----
	
	@Override
	public Plugin getOwningPlugin() {
		return owningPlugin.get();
	}
	
	@Override
	public void invalidate()
	{}
	
	// -----
	
	@Override
	public long asLong() {
		return mLong;
	}
	
	@Override
	public boolean asBoolean() {
		return mLong != 0L;
	}
	
	@Override
	public double asDouble() {
		return mLong;
	}
	
	@Override
	public float asFloat() {
		return mLong;
	}
	
	@Override
	public Object value() {
		return Long.valueOf(mLong);
	}
	
	@Override
	public String asString() {
		return Long.toString(mLong);
	}
	
	// -----
	
	/**
	 * Clamps to {@code Byte.MIN_VALUE : Byte.MAX_VALUE}.
	 */
	@Override
	public byte asByte() {
		return mLong <= Byte.MAX_VALUE ? mLong >= Byte.MIN_VALUE ? (byte)mLong : Byte.MIN_VALUE : Byte.MAX_VALUE; 
	}
	
	/**
	 * Clamps to {@code Short.MIN_VALUE : Short.MAX_VALUE}.
	 */
	@Override
	public short asShort() {
		return mLong <= Short.MAX_VALUE ? mLong >= Short.MIN_VALUE ? (short)mLong : Short.MIN_VALUE : Short.MAX_VALUE;
	}
	
	/**
	 * Clamps to {@code Integer.MIN_VALUE : Integer.MAX_VALUE}.
	 */
	@Override
	public int asInt() {
		return mLong <= Integer.MAX_VALUE ? mLong >= Integer.MIN_VALUE ? (int)mLong : Integer.MIN_VALUE : Integer.MAX_VALUE; 
	}
	
	// -----
	
	/**
	 * Fetches a {@link BasicMetadataLong} from {@link Metadatable} with the specified {@link String metaKey} (and optionally
	 * owned by {@link Plugin}).
	 * @param metadatable The {@link Metadatable} holding metadata.
	 * @param metaKey The key the data was stored with.
	 * @param plugin If {@code plugin} is non-<code>null</code> will narrow down the search to metadata created by that {@link Plugin}.
	 * @return A {@link BasicMetadataLong} or <code>null</code> if no {@link BasicMetadataLong} with the specified {@code metaKey} and
	 *  {@link Plugin} was found. If {@code plugin} is <code>null</code> and {@code metadatable} has multiple {@link BasicMetadataLong}
	 *  stored the method will return the first one it finds.
	 */
	public static BasicMetadataLong getMetadataValue(final Metadatable metadatable, final String metaKey, final Plugin plugin)
	{
		if(metadatable == null || metaKey == null)
			return null;
		
		List<MetadataValue> dlist = metadatable.getMetadata(metaKey);
		if (dlist.size() == 0)
		{
			return null;
		}
		else if (dlist.size() == 1)
		{
			MetadataValue mv = dlist.get(0);
			if (plugin == null || mv.getOwningPlugin() == plugin)
				return (mv instanceof BasicMetadataLong) ? (BasicMetadataLong) mv : null;
			else
				return null;	
		}
		else if (plugin == null)
		{
			for (MetadataValue mv : dlist)
				if (mv instanceof BasicMetadataLong)
					return (BasicMetadataLong) mv;
			return null;
		}
		else
		{
			for (MetadataValue mv : dlist)
				if (mv.getOwningPlugin() == plugin && mv instanceof BasicMetadataLong)
					return (BasicMetadataLong) mv;
			return null;
		}
	}
}
